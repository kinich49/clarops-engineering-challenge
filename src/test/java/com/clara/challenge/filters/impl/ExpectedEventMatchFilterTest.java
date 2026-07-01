package com.clara.challenge.filters.impl;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.TraceTransition;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ExpectedEventMatchFilterTest {

    private final ExpectedEventMatchFilter subject = new ExpectedEventMatchFilter();

    @Test
    void shouldContinueChain_WhenTraceStatusIsNotWaitingOtherEvent() {
        var chain = mock(EventIngestionFilterChain.class);
        var context = buildContext(TraceStatus.STARTED, null, "RULES_EVALUATED");

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isTrue();
        verify(chain).doFilter(context);
    }

    @Test
    void shouldContinueChain_WhenThereIsNoPriorTransition() {
        var chain = mock(EventIngestionFilterChain.class);
        var context = buildContext(TraceStatus.WAITING_OTHER_EVENT, null, "RULES_EVALUATED");

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isTrue();
        verify(chain).doFilter(context);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldContinueChain_WhenPriorTransitionHasNoExpectedEvent(String nextExpectedEvent) {
        var chain = mock(EventIngestionFilterChain.class);
        var transition = buildTransition(nextExpectedEvent);
        var context = buildContext(TraceStatus.WAITING_OTHER_EVENT, transition, "RULES_EVALUATED");

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isTrue();
        verify(chain).doFilter(context);
    }

    @ParameterizedTest
    @ValueSource(strings = {"RULES_EVALUATED", "rules_evaluated", "Rules_Evaluated"})
    void shouldContinueChain_WhenNewEventMatchesExpectedEventCaseInsensitively(String receivedEventName) {
        var chain = mock(EventIngestionFilterChain.class);
        var transition = buildTransition("RULES_EVALUATED");
        var context = buildContext(TraceStatus.WAITING_OTHER_EVENT, transition, receivedEventName);

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isTrue();
        verify(chain).doFilter(context);
    }

    @Test
    void shouldRejectEvent_WhenNewEventDoesNotMatchExpectedEvent() {
        var chain = mock(EventIngestionFilterChain.class);
        var transition = buildTransition("RULES_EVALUATED");
        var context = buildContext(TraceStatus.WAITING_OTHER_EVENT, transition, "APPLICATION_RECEIVED");

        subject.doFilter(context, chain);

        assertThat(context.shouldIngest()).isFalse();
        assertThat(context.getRejectionReason())
                .contains("RULES_EVALUATED")
                .contains("APPLICATION_RECEIVED");
        verifyNoInteractions(chain);
    }

    private EventIngestionContext buildContext(TraceStatus status, TraceTransition transition, String newEventName) {
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(status);
        var newEvent = new Event();
        newEvent.setEventId("event-new");
        newEvent.setEventName(newEventName);
        newEvent.setTrace(trace);
        return new EventIngestionContext(newEvent, transition);
    }

    private TraceTransition buildTransition(String nextExpectedEvent) {
        var priorEvent = new Event();
        priorEvent.setEventId("event-prior");
        priorEvent.setEventName("APPLICATION_RECEIVED");
        priorEvent.setNextExpectedEvent(nextExpectedEvent);
        var transition = new TraceTransition();
        transition.setEvent(priorEvent);
        return transition;
    }
}
