package com.clara.challenge.services.impl;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.TraceTransition;
import com.clara.challenge.entities.db.enums.EventResult;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.entities.misc.EventDTO;
import com.clara.challenge.entities.misc.SafeguardProperties;
import com.clara.challenge.exceptions.InvalidEventException;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilter;
import com.clara.challenge.filters.impl.DefaultEventIngestionFilterChain;
import com.clara.challenge.repositories.EventRepository;
import com.clara.challenge.repositories.TraceTransitionRepository;
import com.clara.challenge.services.internal.EventIngestionService;
import com.clara.challenge.services.internal.TraceIngestionService;
import com.clara.challenge.utils.TtlEvaluator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class EventIngestionServiceImpl implements EventIngestionService {

  private final List<EventIngestionFilter> filters;
  private final EventRepository eventRepository;
  private final TraceTransitionRepository transitionRepository;
  private final TraceIngestionService traceIngestionService;
  private final SafeguardProperties safeguard;

  @Override
  public EventDTO ingestEvent(final Event event) {
    var filterChain = new DefaultEventIngestionFilterChain(filters);
    var currentTransition =
        transitionRepository.findLatestByTraceId(event.getTrace().getTraceId()).orElse(null);

    var context = new EventIngestionContext(event, currentTransition);
    filterChain.doFilter(context);

    if (context.shouldIngest()) {
      event.setAccepted(true);
      var newTraceStatus = nextTraceStatus(event, currentTransition);
      traceIngestionService.updateStatus(event.getTrace(), newTraceStatus);
      var persistedEvent = eventRepository.save(event);
      var newTransition = buildTraceTransition(persistedEvent, newTraceStatus);
      transitionRepository.save(newTransition);
      return new EventDTO(persistedEvent, newTransition);
    } else {
      var persistedEvent = eventRepository.save(event);
      if (context.isInvalid()) {
        throw new InvalidEventException(context.getRejectionReason());
      }
      return new EventDTO(persistedEvent, null);
    }
  }

  private TraceStatus nextTraceStatus(final Event event, final TraceTransition previousTransition) {
    if (isTtlBreached(event, previousTransition)) {
      return TraceStatus.TTL_EXPIRED_FOR_EVENT;
    }
    if (event.isFinalEvent()) {
      return event.getEventResult() == EventResult.ERROR
          ? TraceStatus.ERROR
          : TraceStatus.COMPLETED;
    }
    if (!ObjectUtils.isEmpty(event.getNextExpectedEvent())) {
      return TraceStatus.WAITING_OTHER_EVENT;
    }
    var currentStatus = event.getTrace().getStatus();
    if (currentStatus == TraceStatus.WAITING_OTHER_EVENT
        || currentStatus == TraceStatus.IN_PROGRESS) {
      return TraceStatus.IN_PROGRESS;
    }

    return TraceStatus.STARTED;
  }

  private boolean isTtlBreached(final Event event, final TraceTransition previousTransition) {
    if (event.getTrace().getStatus() != TraceStatus.WAITING_OTHER_EVENT
        || previousTransition == null) {
      return false;
    }

    return TtlEvaluator.isBreached(previousTransition.getExpectedBefore(), safeguard);
  }

  private TraceTransition buildTraceTransition(final Event event, final TraceStatus status) {
    final var trace = event.getTrace();
    Instant now = Instant.now();
    var transition = new TraceTransition();
    transition.setTrace(trace);
    transition.setEvent(event);
    transition.setStatus(status);
    Optional.ofNullable(event.getNextEventTtlSeconds())
        .filter(ttl -> 0 != ttl)
        .ifPresent(ttl -> transition.setExpectedBefore(now.plusSeconds(ttl)));

    transition.setRegistrationDatetime(now);

    return transition;
  }
}
