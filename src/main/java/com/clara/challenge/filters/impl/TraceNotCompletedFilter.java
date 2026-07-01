package com.clara.challenge.filters.impl;

import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilter;
import com.clara.challenge.filters.EventIngestionFilterChain;
import org.springframework.stereotype.Component;

@Component
public class TraceNotCompletedFilter implements EventIngestionFilter {

    @Override
    public void doFilter(EventIngestionContext context, EventIngestionFilterChain chain) {
        TraceStatus status = context.getNewEvent().getTrace().getStatus();

        if (status == TraceStatus.COMPLETED || status == TraceStatus.TTL_EXPIRED_FOR_EVENT || status == TraceStatus.ERROR) {
            context.reject("Trace is already in terminal state: " + status);
            return;
        }

        chain.doFilter(context);
    }
}
