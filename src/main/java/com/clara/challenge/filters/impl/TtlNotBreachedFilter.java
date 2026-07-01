package com.clara.challenge.filters.impl;

import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.entities.misc.SafeguardProperties;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilter;
import com.clara.challenge.filters.EventIngestionFilterChain;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;

//@Component
public class TtlNotBreachedFilter implements EventIngestionFilter {

    private final SafeguardProperties safeguard;

    public TtlNotBreachedFilter(SafeguardProperties safeguard) {
        this.safeguard = safeguard;
    }

    @Override
    public void doFilter(EventIngestionContext context, EventIngestionFilterChain chain) {
        var trace = context.getNewEvent().getTrace();
        var transition = context.getTransition();

        if (trace.getStatus() != TraceStatus.WAITING_OTHER_EVENT) {
            chain.doFilter(context);
            return;
        }

        long offsetSeconds = safeguard.enabled() ? safeguard.offsetSeconds() : 0L;
        Instant effectiveDeadline = transition.getExpectedBefore().plusSeconds(offsetSeconds);

        if (Instant.now().isAfter(effectiveDeadline)) {
            context.reject("TTL breached for expected event: " + transition.getEvent().getNextExpectedEvent());
            return;
        }

        chain.doFilter(context);
    }
}