package com.clara.challenge.filters.impl;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.TraceTransition;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilter;
import com.clara.challenge.filters.EventIngestionFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Comparator;

@Component
public class ExpectedEventMatchFilter implements EventIngestionFilter {

    @Override
    public void doFilter(EventIngestionContext context, EventIngestionFilterChain chain) {
        var trace = context.getNewEvent().getTrace();
        var transition = context.getTransition();
        var newEvent = context.getNewEvent();

        if (trace.getStatus() == TraceStatus.WAITING_OTHER_EVENT &&
                transition != null &&
                !ObjectUtils.isEmpty(transition.getEvent().getNextExpectedEvent()) &&
        transition.getEvent().getNextExpectedEvent().equalsIgnoreCase(newEvent.getEventName())) {
            context.reject("Expected event: " + transition.getEvent().getEventName()
                    + " but received: " + newEvent.getEventName());
            return;
        }

        chain.doFilter(context);
    }
}
