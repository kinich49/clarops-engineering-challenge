package com.clara.challenge.services.impl;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.TraceTransition;
import com.clara.challenge.entities.db.enums.EventResult;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.exceptions.TraceNotFoundException;
import com.clara.challenge.repositories.TraceRepository;
import com.clara.challenge.repositories.TraceTransitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private TraceServiceImpl subject;

    @Test
    void shouldReturnTraceJson_WhenLatestTransitionExists() {
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(TraceStatus.WAITING_OTHER_EVENT);
        var expectedBefore = Instant.now().plusSeconds(120);
        var transition = buildTransition(trace, "APPLICATION_RECEIVED", EventResult.SUCCESS, "RULES_EVALUATED", expectedBefore);
        when(traceTransitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.of(transition));

        var result = subject.getTrace("trace-1");

        assertThat(result.getTraceId()).isEqualTo("trace-1");
        assertThat(result.getStatus()).isEqualTo("WAITING_OTHER_EVENT");
        assertThat(result.getLastEventName()).isEqualTo("APPLICATION_RECEIVED");
        assertThat(result.getLastEventResult()).isEqualTo("SUCCESS");
        assertThat(result.getNextExpectedEvent()).isEqualTo("RULES_EVALUATED");
        assertThat(result.getNextExpectedBefore()).isEqualTo(expectedBefore);
    }

    @Test
    void shouldThrowTraceNotFoundException_WhenNoTransitionExists() {
        when(traceTransitionRepository.findLatestByTraceId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subject.getTrace("missing"))
                .isInstanceOf(TraceNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void shouldReturnExistingTrace_WhenTraceAlreadyExists() {
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
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(TraceStatus.STARTED);
        when(traceRepository.save(trace)).thenAnswer(invocation -> invocation.getArgument(0));

        var result = subject.updateStatus(trace, TraceStatus.WAITING_OTHER_EVENT);

        assertThat(result.getStatus()).isEqualTo(TraceStatus.WAITING_OTHER_EVENT);
        verify(traceRepository).save(trace);
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
