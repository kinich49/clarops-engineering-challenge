package com.clara.challenge.filters;

public interface EventIngestionFilter {

    void doFilter(EventIngestionContext context, EventIngestionFilterChain chain);

}
