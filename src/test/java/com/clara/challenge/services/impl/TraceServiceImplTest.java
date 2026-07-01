package com.clara.challenge.services.impl;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.TraceTransition;
import com.clara.challenge.entities.db.enums.EventResult;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.entities.misc.SafeguardProperties;
import com.clara.challenge.exceptions.TraceNotFoundException;
import com.clara.challenge.repositories.EventRepository;
import com.clara.challenge.repositories.TraceRepository;
import com.clara.challenge.repositories.TraceTransitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceServiceImplTest {

    @Mock
    private TraceRepository traceRepository;

    @Mock
    private TraceTransitionRepository traceTransitionRepository;

    @Mock
    private EventRepository eventRepository;

    @Test
    void shouldReturnTraceJson_WhenLatestTransitionExists() {
        var subject = buildSubject();
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(TraceStatus.WAITING_OTHER_EVENT);
        var expectedBefore = Instant.now().plusSeconds(120);
        var transition = buildTransition(trace, "APPLICATION_RECEIVED", EventResult.SUCCESS, "RULES_EVALUATED", expectedBefore);
        when(traceTransitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.of(transition));
        when(eventRepository.countByTraceId("trace-1")).thenReturn(buildEventCounts(3, 1));

        var result = subject.getTrace("trace-1");

        assertThat(result.getTraceId()).isEqualTo("trace-1");
        assertThat(result.getStatus()).isEqualTo("WAITING_OTHER_EVENT");
        assertThat(result.getLastEventName()).isEqualTo("APPLICATION_RECEIVED");
        assertThat(result.getLastEventResult()).isEqualTo("SUCCESS");
        assertThat(result.getNextExpectedEvent()).isEqualTo("RULES_EVALUATED");
        assertThat(result.getNextExpectedBefore()).isEqualTo(expectedBefore);
        assertThat(result.getValidEvents()).isEqualTo(3);
        assertThat(result.getInvalidEvents()).isEqualTo(1);
        verify(traceRepository, never()).save(any());
    }

    @Test
    void shouldThrowTraceNotFoundException_WhenNeitherTransitionNorEventExists() {
        var subject = buildSubject();
        when(eventRepository.countByTraceId("missing")).thenReturn(buildEventCounts(0, 0));
        when(traceTransitionRepository.findLatestByTraceId("missing")).thenReturn(Optional.empty());
        when(eventRepository.findLatestByTraceId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subject.getTrace("missing"))
                .isInstanceOf(TraceNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void shouldReturnTraceJson_WhenNoTransitionExistsButAnEventDoes() {
        var subject = buildSubject();
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(TraceStatus.STARTED);
        var rejectedEvent = new Event();
        rejectedEvent.setEventId("event-1");
        rejectedEvent.setEventName("APPLICATION_RECEIVED");
        rejectedEvent.setEventResult(EventResult.SUCCESS);
        rejectedEvent.setAccepted(false);
        rejectedEvent.setTrace(trace);
        when(eventRepository.countByTraceId("trace-1")).thenReturn(buildEventCounts(0, 1));
        when(traceTransitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.empty());
        when(eventRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.of(rejectedEvent));

        var result = subject.getTrace("trace-1");

        assertThat(result.getTraceId()).isEqualTo("trace-1");
        assertThat(result.getStatus()).isEqualTo("STARTED");
        assertThat(result.getLastEventName()).isEqualTo("APPLICATION_RECEIVED");
        assertThat(result.getLastEventResult()).isEqualTo("SUCCESS");
        assertThat(result.getNextExpectedBefore()).isNull();
        assertThat(result.getValidEvents()).isEqualTo(0);
        assertThat(result.getInvalidEvents()).isEqualTo(1);
        verify(traceRepository, never()).save(any());
    }

    @Test
    void shouldUpdateTraceStatusToTtlExpiredForEvent_WhenDeadlineIsBreached() {
        var subject = buildSubject(new SafeguardProperties(false, 0L));
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(TraceStatus.WAITING_OTHER_EVENT);
        var expectedBefore = Instant.now().minusSeconds(100);
        var transition = buildTransition(trace, "APPLICATION_RECEIVED", EventResult.SUCCESS, "RULES_EVALUATED", expectedBefore);
        when(traceTransitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.of(transition));
        when(traceRepository.save(trace)).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventRepository.countByTraceId("trace-1")).thenReturn(buildEventCounts(1, 0));

        var result = subject.getTrace("trace-1");

        assertThat(result.getStatus()).isEqualTo("TTL_EXPIRED_FOR_EVENT");
        assertThat(trace.getStatus()).isEqualTo(TraceStatus.TTL_EXPIRED_FOR_EVENT);
        verify(traceRepository).save(trace);
    }

    @Test
    void shouldNotUpdateTraceStatus_WhenDeadlineBreachedButTraceIsNotWaitingOtherEvent() {
        var subject = buildSubject(new SafeguardProperties(false, 0L));
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(TraceStatus.COMPLETED);
        var expectedBefore = Instant.now().minusSeconds(100);
        var transition = buildTransition(trace, "APPLICATION_RECEIVED", EventResult.SUCCESS, null, expectedBefore);
        when(traceTransitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.of(transition));
        when(eventRepository.countByTraceId("trace-1")).thenReturn(buildEventCounts(1, 0));

        var result = subject.getTrace("trace-1");

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        verify(traceRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateTraceStatus_WhenSafeguardOffsetKeepsDeadlineInFuture() {
        var subject = buildSubject(new SafeguardProperties(true, 60L));
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(TraceStatus.WAITING_OTHER_EVENT);
        var expectedBefore = Instant.now().minusSeconds(5);
        var transition = buildTransition(trace, "APPLICATION_RECEIVED", EventResult.SUCCESS, "RULES_EVALUATED", expectedBefore);
        when(traceTransitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.of(transition));
        when(eventRepository.countByTraceId("trace-1")).thenReturn(buildEventCounts(1, 0));

        var result = subject.getTrace("trace-1");

        assertThat(result.getStatus()).isEqualTo("WAITING_OTHER_EVENT");
        verify(traceRepository, never()).save(any());
    }

    @Test
    void shouldReturnExistingTrace_WhenTraceAlreadyExists() {
        var subject = buildSubject();
        var existing = new Trace();
        existing.setTraceId("trace-1");
        existing.setStatus(TraceStatus.WAITING_OTHER_EVENT);
        when(traceRepository.findById("trace-1")).thenReturn(Optional.of(existing));

        var result = subject.findOrCreate("trace-1");

        assertThat(result).isSameAs(existing);
        verify(traceRepository, never()).save(any());
    }

    @Test
    void shouldCreateAndPersistNewTrace_WhenTraceDoesNotExist() {
        var subject = buildSubject();
        when(traceRepository.findById("trace-2")).thenReturn(Optional.empty());
        when(traceRepository.save(any(Trace.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var before = Instant.now();
        var result = subject.findOrCreate("trace-2");
        var after = Instant.now();

        assertThat(result.getTraceId()).isEqualTo("trace-2");
        assertThat(result.getStatus()).isEqualTo(TraceStatus.STARTED);
        assertThat(result.getRegistrationDatetime()).isBetween(before, after);
        verify(traceRepository).save(result);
    }

    @Test
    void shouldUpdateAndPersistTraceStatus_WhenUpdateStatusIsCalled() {
        var subject = buildSubject();
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(TraceStatus.STARTED);
        when(traceRepository.save(trace)).thenAnswer(invocation -> invocation.getArgument(0));

        var result = subject.updateStatus(trace, TraceStatus.WAITING_OTHER_EVENT);

        assertThat(result.getStatus()).isEqualTo(TraceStatus.WAITING_OTHER_EVENT);
        verify(traceRepository).save(trace);
    }

    private TraceServiceImpl buildSubject() {
        return buildSubject(new SafeguardProperties(false, 0L));
    }

    private TraceServiceImpl buildSubject(SafeguardProperties safeguard) {
        return new TraceServiceImpl(traceRepository, traceTransitionRepository, eventRepository, safeguard);
    }

    private EventRepository.EventCounts buildEventCounts(long validEvents, long invalidEvents) {
        return new EventRepository.EventCounts() {
            @Override
            public long getValidEvents() {
                return validEvents;
            }

            @Override
            public long getInvalidEvents() {
                return invalidEvents;
            }
        };
    }

    private TraceTransition buildTransition(Trace trace, String eventName, EventResult eventResult,
                                             String nextExpectedEvent, Instant expectedBefore) {
        var event = new Event();
        event.setEventId("event-1");
        event.setEventName(eventName);
        event.setEventResult(eventResult);
        event.setNextExpectedEvent(nextExpectedEvent);
        event.setTrace(trace);
        var transition = new TraceTransition();
        transition.setTrace(trace);
        transition.setEvent(event);
        transition.setExpectedBefore(expectedBefore);
        return transition;
    }
}
