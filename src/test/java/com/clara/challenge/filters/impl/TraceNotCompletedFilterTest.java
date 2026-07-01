package com.clara.challenge.filters.impl;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilterChain;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TraceNotCompletedFilterTest {

    private final TraceNotCompletedFilter subject = new TraceNotCompletedFilter();

    @ParameterizedTest
    @EnumSource(value = TraceStatus.class, names = {"COMPLETED", "TTL_EXPIRED_FOR_EVENT", "ERROR"})
    void shouldRejectEvent_WhenTraceStatusIsTerminal(TraceStatus status) {
        var chain = mock(EventIngestionFilterChain.class);
        var context = buildContext(status);

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isFalse();
        assertThat(context.getRejectionReason()).contains(status.toString());
        verifyNoInteractions(chain);
    }

    @ParameterizedTest
    @EnumSource(value = TraceStatus.class, names = {"STARTED", "WAITING_OTHER_EVENT", "IN_PROGRESS"})
    void shouldContinueChain_WhenTraceStatusIsNotTerminal(TraceStatus status) {
        var chain = mock(EventIngestionFilterChain.class);
        var context = buildContext(status);

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isTrue();
        verify(chain).doFilter(context);
    }

    private EventIngestionContext buildContext(TraceStatus status) {
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(status);
        var event = new Event();
        event.setEventId("event-1");
        event.setTrace(trace);
        return new EventIngestionContext(event, null);
    }
}
