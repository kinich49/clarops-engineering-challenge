package com.clara.challenge.filters;

public interface EventIngestionFilterChain {

  void doFilter(EventIngestionContext context);
}
