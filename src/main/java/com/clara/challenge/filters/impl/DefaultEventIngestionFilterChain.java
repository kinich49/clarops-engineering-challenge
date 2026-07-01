package com.clara.challenge.filters.impl;

import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilter;
import com.clara.challenge.filters.EventIngestionFilterChain;

import java.util.List;

public class DefaultEventIngestionFilterChain implements EventIngestionFilterChain {

    private final List<EventIngestionFilter> filters;

    public DefaultEventIngestionFilterChain(List<EventIngestionFilter> filters) {
        this.filters = filters;
    }

    @Override
    public void doFilter(EventIngestionContext context) {
        for (var filter : filters) {
            if (!context.shouldIngest()) break;
            filter.doFilter(context, this);
        }
    }
}
