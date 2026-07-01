package com.clara.challenge.filters.impl;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.TraceTransition;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.entities.misc.SafeguardProperties;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilterChain;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TtlNotBreachedFilterTest {

    @Test
    void shouldContinueChain_WhenTraceStatusIsNotWaitingOtherEvent() {
        var subject = new TtlNotBreachedFilter(new SafeguardProperties(false, 0L));
        var chain = mock(EventIngestionFilterChain.class);
        var context = buildContext(TraceStatus.STARTED, null);

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isTrue();
        verify(chain).doFilter(context);
    }

    @Test
    void shouldContinueChain_WhenDeadlineNotReached() {
        var subject = new TtlNotBreachedFilter(new SafeguardProperties(false, 0L));
        var chain = mock(EventIngestionFilterChain.class);
        var transition = buildTransition(Instant.now().plusSeconds(100));
        var context = buildContext(TraceStatus.WAITING_OTHER_EVENT, transition);

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isTrue();
        verify(chain).doFilter(context);
    }

    @Test
    void shouldRejectEvent_WhenDeadlineBreachedAndSafeguardDisabled() {
        var subject = new TtlNotBreachedFilter(new SafeguardProperties(false, 999L));
        var chain = mock(EventIngestionFilterChain.class);
        var transition = buildTransition(Instant.now().minusSeconds(100));
        var context = buildContext(TraceStatus.WAITING_OTHER_EVENT, transition);

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isFalse();
        assertThat(context.getRejectionReason()).contains("RULES_EVALUATED");
        verifyNoInteractions(chain);
    }

    @Test
    void shouldRejectEvent_WhenDeadlineBreachedEvenWithSafeguardOffset() {
        var subject = new TtlNotBreachedFilter(new SafeguardProperties(true, 10L));
        var chain = mock(EventIngestionFilterChain.class);
        var transition = buildTransition(Instant.now().minusSeconds(1000));
        var context = buildContext(TraceStatus.WAITING_OTHER_EVENT, transition);

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isFalse();
        verifyNoInteractions(chain);
    }

    @Test
    void shouldContinueChain_WhenSafeguardOffsetKeepsDeadlineInFuture() {
        var subject = new TtlNotBreachedFilter(new SafeguardProperties(true, 60L));
        var chain = mock(EventIngestionFilterChain.class);
        var transition = buildTransition(Instant.now().minusSeconds(5));
        var context = buildContext(TraceStatus.WAITING_OTHER_EVENT, transition);

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isTrue();
        verify(chain).doFilter(context);
    }

    private EventIngestionContext buildContext(TraceStatus status, TraceTransition transition) {
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(status);
        var newEvent = new Event();
        newEvent.setEventId("event-new");
        newEvent.setTrace(trace);
        return new EventIngestionContext(newEvent, transition);
    }

    private TraceTransition buildTransition(Instant expectedBefore) {
        var priorEvent = new Event();
        priorEvent.setEventId("event-prior");
        priorEvent.setNextExpectedEvent("RULES_EVALUATED");
        var transition = new TraceTransition();
        transition.setEvent(priorEvent);
        transition.setExpectedBefore(expectedBefore);
        return transition;
    }
}
