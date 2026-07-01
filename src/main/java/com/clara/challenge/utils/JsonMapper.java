package com.clara.challenge.utils;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.TraceTransition;
import com.clara.challenge.entities.json.TraceJson;
import com.clara.challenge.entities.misc.EventDTO;

import java.util.Optional;

public interface JsonMapper {

    private static TraceJson toJson(Trace trace, Event event, TraceTransition transition, int validEvents, int invalidEvents) {
        var builder = TraceJson.builder()
                .traceId(trace.getTraceId())
                .status(trace.getStatus().name())
                .lastEventName(event.getEventName())
                .lastEventResult(event.getEventResult().name())
                .nextExpectedEvent(event.getNextExpectedEvent())
                .validEvents(validEvents)
                .invalidEvents(invalidEvents);

        Optional.ofNullable(transition)
                .ifPresent(t -> builder.nextExpectedBefore(t.getExpectedBefore()));

        return builder.build();
    }

    static TraceJson toJson(TraceTransition transition, int validEvents, int invalidEvents) {
        var event = transition.getEvent();
        var trace = event.getTrace();

        return toJson(trace, event, transition, validEvents, invalidEvents);
    }

    static TraceJson toJson(Trace trace, Event event, int validEvents, int invalidEvents) {
        return toJson(trace, event, null, validEvents, invalidEvents);
    }

    static TraceJson toJson(final EventDTO dto) {
        return toJson(dto.event().getTrace(), dto.event(), dto.transition(), 0, 0);
    }
}
