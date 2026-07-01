package com.clara.challenge.services.impl;

import com.clara.challenge.config.SafeguardProperties;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.entities.json.TraceJson;
import com.clara.challenge.exceptions.TraceNotFoundException;
import com.clara.challenge.repositories.EventRepository;
import com.clara.challenge.repositories.TraceRepository;
import com.clara.challenge.repositories.TraceTransitionRepository;
import com.clara.challenge.services.api.TraceService;
import com.clara.challenge.services.internal.TraceIngestionService;
import com.clara.challenge.utils.JsonMapper;
import com.clara.challenge.utils.TtlEvaluator;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TraceServiceImpl implements TraceService, TraceIngestionService {

  private final TraceRepository traceRepository;
  private final TraceTransitionRepository traceTransitionRepository;
  private final EventRepository eventRepository;
  private final SafeguardProperties safeguard;

  @Override
  public TraceJson getTrace(String traceId) {
    var eventCounts = eventRepository.countByTraceId(traceId);
    var validEvents = (int) eventCounts.getValidEvents();
    var invalidEvents = (int) eventCounts.getInvalidEvents();

    var maybeTransition = traceTransitionRepository.findLatestByTraceId(traceId);
    if (maybeTransition.isPresent()) {
      var transition = maybeTransition.get();
      var trace = transition.getEvent().getTrace();
      if (trace.getStatus() == TraceStatus.WAITING_OTHER_EVENT
          && TtlEvaluator.isBreached(transition.getExpectedBefore(), safeguard)) {
        updateStatus(trace, TraceStatus.TTL_EXPIRED_FOR_EVENT);
      }
      return JsonMapper.toJson(transition, validEvents, invalidEvents);
    }

    var latestEvent =
        eventRepository
            .findLatestByTraceId(traceId)
            .orElseThrow(() -> new TraceNotFoundException(traceId));
    return JsonMapper.toJson(latestEvent.getTrace(), latestEvent, validEvents, invalidEvents);
  }

  @Override
  public Trace findOrCreate(String traceId) {
    return traceRepository
        .findById(traceId)
        .orElseGet(
            () -> {
              var trace = new Trace();
              trace.setTraceId(traceId);
              trace.setStatus(TraceStatus.STARTED);
              trace.setRegistrationDatetime(Instant.now());
              return traceRepository.save(trace);
            });
  }

  @Override
  public Trace updateStatus(Trace trace, TraceStatus status) {
    trace.setStatus(status);
    return traceRepository.save(trace);
  }
}
