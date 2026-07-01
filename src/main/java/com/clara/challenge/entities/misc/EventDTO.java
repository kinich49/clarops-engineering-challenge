package com.clara.challenge.entities.misc;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.TraceTransition;

public record EventDTO(
        Event event,
        TraceTransition transition
) {
}
