package com.clara.challenge.filters.impl;

import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilter;
import com.clara.challenge.filters.EventIngestionFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@Order(0)
public class ValidEventShapeFilter implements EventIngestionFilter {

    @Override
    public void doFilter(EventIngestionContext context, EventIngestionFilterChain chain) {
        var newEvent = context.getNewEvent();
        var hasNextExpectedEvent = !ObjectUtils.isEmpty(newEvent.getNextExpectedEvent());
        var hasTtl = newEvent.getNextEventTtlSeconds() != null && newEvent.getNextEventTtlSeconds() != 0;

        if (newEvent.isFinalEvent() && hasNextExpectedEvent) {
            context.rejectAsInvalid("Event is marked final but declares a nextExpectedEvent: "
                    + newEvent.getNextExpectedEvent());
            return;
        }

        if (hasTtl && !hasNextExpectedEvent) {
            context.rejectAsInvalid("Event declares a nextEventTtlSeconds but no nextExpectedEvent");
            return;
        }

        chain.doFilter(context);
    }
}
