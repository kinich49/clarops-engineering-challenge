package com.clara.challenge.services.impl;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.TraceTransition;
import com.clara.challenge.entities.misc.EventDTO;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilter;
import com.clara.challenge.filters.impl.DefaultEventIngestionFilterChain;
import com.clara.challenge.repositories.EventRepository;
import com.clara.challenge.repositories.TraceRepository;
import com.clara.challenge.repositories.TraceTransitionRepository;
import com.clara.challenge.services.internal.EventIngestionService;
import com.clara.challenge.services.internal.TraceIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventIngestionServiceImpl implements EventIngestionService {

    private final List<EventIngestionFilter> filters;
    private final TraceIngestionService traceIngestionService;
    private final EventRepository eventRepository;
    private final TraceRepository traceRepository;
    private final TraceTransitionRepository transitionRepository;

    // find current trace or create new one
    // decide if ingest event
    // ingest if:
    // - current trace is not completed
    // - expected next event matches current event
    // - ttl is not breached
    // if ingest then persist TraceTransition
    // persist event anyway


    @Override
    public EventDTO ingestEvent(final Event event) {
        var filterChain = new DefaultEventIngestionFilterChain(filters);
        var currentTransition = transitionRepository
                .findLatestByTraceId(event.getTrace().getTraceId())
                .orElse(null);
        var context = new EventIngestionContext(event, currentTransition);
        filterChain.doFilter(context);

        if (context.shouldIngest()) {
            event.setAccepted(true);
            var persistedEvent = eventRepository.save(event);
            var newTransition = buildTraceTransition(persistedEvent);
            transitionRepository.save(newTransition);
            return new EventDTO(persistedEvent, newTransition);
        } else {
            var persistedEvent = eventRepository.save(event);
            return new EventDTO(persistedEvent, null);
        }
    }

    private TraceTransition buildTraceTransition(final Event event) {
        final var trace = event.getTrace();
        Instant now = Instant.now();
        var transition = new TraceTransition();
        transition.setTrace(trace);
        transition.setEvent(event);
        Optional.ofNullable(event.getNextEventTtlSeconds())
                .filter(ttl -> 0 != ttl)
                .ifPresent(ttl -> transition.setExpectedBefore(now.plusSeconds(ttl)));

        transition.setRegistrationDatetime(now);

        return transition;
    }
}
