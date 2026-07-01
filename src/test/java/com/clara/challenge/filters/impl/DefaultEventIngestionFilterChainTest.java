package com.clara.challenge.filters.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilter;
import com.clara.challenge.filters.EventIngestionFilterChain;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class DefaultEventIngestionFilterChainTest {

  @Test
  void shouldInvokeAllFiltersInOrder_WhenEachContinuesTheChain() {
    var filter1 = continuingFilter();
    var filter2 = continuingFilter();
    var filter3 = mock(EventIngestionFilter.class);
    var subject = new DefaultEventIngestionFilterChain(List.of(filter1, filter2, filter3));
    var context = buildContext();

    subject.doFilter(context);

    InOrder order = inOrder(filter1, filter2, filter3);
    order.verify(filter1).doFilter(context, subject);
    order.verify(filter2).doFilter(context, subject);
    order.verify(filter3).doFilter(context, subject);
    assertThat(context.shouldIngest()).isTrue();
  }

  @Test
  void shouldInvokeEachFilterExactlyOnce_WhenTheyAllContinueTheChain() {
    var filter1 = continuingFilter();
    var filter2 = continuingFilter();
    var subject = new DefaultEventIngestionFilterChain(List.of(filter1, filter2));
    var context = buildContext();

    subject.doFilter(context);

    verify(filter1).doFilter(context, subject);
    verify(filter2).doFilter(context, subject);
  }

  @Test
  void shouldStopAdvancing_WhenAFilterDoesNotContinueTheChain() {
    var filter1 = mock(EventIngestionFilter.class);
    var filter2 = mock(EventIngestionFilter.class);
    var subject = new DefaultEventIngestionFilterChain(List.of(filter1, filter2));
    var context = buildContext();

    subject.doFilter(context);

    verify(filter1).doFilter(context, subject);
    verifyNoInteractions(filter2);
  }

  @Test
  void shouldStopInvokingRemainingFilters_WhenAFilterRejects() {
    var filter1 = mock(EventIngestionFilter.class);
    var filter2 = mock(EventIngestionFilter.class);
    var filter3 = mock(EventIngestionFilter.class);
    doAnswer(
            invocation -> {
              EventIngestionContext ctx = invocation.getArgument(0);
              ctx.reject("rejected by filter1");
              return null;
            })
        .when(filter1)
        .doFilter(any(), any());
    var subject = new DefaultEventIngestionFilterChain(List.of(filter1, filter2, filter3));
    var context = buildContext();

    subject.doFilter(context);

    verify(filter1).doFilter(context, subject);
    verifyNoInteractions(filter2);
    verifyNoInteractions(filter3);
    assertThat(context.shouldIngest()).isFalse();
    assertThat(context.getRejectionReason()).isEqualTo("rejected by filter1");
  }

  @Test
  void shouldNotFail_WhenFilterListIsEmpty() {
    var subject = new DefaultEventIngestionFilterChain(List.of());
    var context = buildContext();

    subject.doFilter(context);

    assertThat(context.shouldIngest()).isTrue();
  }

  private EventIngestionFilter continuingFilter() {
    var filter = mock(EventIngestionFilter.class);
    doAnswer(
            invocation -> {
              EventIngestionContext ctx = invocation.getArgument(0);
              EventIngestionFilterChain chain = invocation.getArgument(1);
              chain.doFilter(ctx);
              return null;
            })
        .when(filter)
        .doFilter(any(), any());
    return filter;
  }

  private EventIngestionContext buildContext() {
    var trace = new Trace();
    trace.setTraceId("trace-1");
    trace.setStatus(TraceStatus.STARTED);
    var event = new Event();
    event.setEventId("event-1");
    event.setTrace(trace);
    return new EventIngestionContext(event, null);
  }
}
