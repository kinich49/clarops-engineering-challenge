package com.clara.challenge.services.impl;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.TraceTransition;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilter;
import com.clara.challenge.filters.EventIngestionFilterChain;
import com.clara.challenge.repositories.EventRepository;
import com.clara.challenge.repositories.TraceRepository;
import com.clara.challenge.repositories.TraceTransitionRepository;
import com.clara.challenge.services.internal.TraceIngestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventIngestionServiceImplTest {

    @Mock
    private TraceIngestionService traceIngestionService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TraceRepository traceRepository;

    @Mock
    private TraceTransitionRepository transitionRepository;

    @Test
    void shouldPersistEventAndTransition_WhenFilterChainAccepts() {
        var acceptingFilter = continuingFilter();
        var subject = buildSubject(acceptingFilter);
        var event = buildEvent("trace-1", 60);
        when(transitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.empty());
        when(eventRepository.save(event)).thenReturn(event);
        when(transitionRepository.save(any(TraceTransition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var before = Instant.now();
        var result = subject.ingestEvent(event);
        var after = Instant.now();

        assertThat(event.isAccepted()).isTrue();
        assertThat(result.event()).isSameAs(event);
        assertThat(result.transition()).isNotNull();
        assertThat(result.transition().getTrace()).isSameAs(event.getTrace());
        assertThat(result.transition().getEvent()).isSameAs(event);
        assertThat(result.transition().getExpectedBefore()).isBetween(before.plusSeconds(60), after.plusSeconds(60));
        assertThat(result.transition().getRegistrationDatetime()).isBetween(before, after);
        verify(eventRepository).save(event);
        verify(transitionRepository).save(any(TraceTransition.class));
    }

    @Test
    void shouldPersistEventOnly_WhenFilterChainRejects() {
        var rejectingFilter = rejectingFilter("not eligible for ingestion");
        var subject = buildSubject(rejectingFilter);
        var event = buildEvent("trace-1", 60);
        when(transitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.empty());
        when(eventRepository.save(event)).thenReturn(event);

        var result = subject.ingestEvent(event);

        assertThat(event.isAccepted()).isFalse();
        assertThat(result.event()).isSameAs(event);
        assertThat(result.transition()).isNull();
        verify(eventRepository).save(event);
        verify(transitionRepository, never()).save(any());
    }

    @Test
    void shouldStopAtRejectingFilter_WhenChainHasMultipleFilters() {
        var firstFilter = continuingFilter();
        var secondFilter = rejectingFilter("out of sequence");
        var thirdFilter = mock(EventIngestionFilter.class);
        var subject = buildSubject(firstFilter, secondFilter, thirdFilter);
        var event = buildEvent("trace-1", 60);
        when(transitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.empty());
        when(eventRepository.save(event)).thenReturn(event);

        var result = subject.ingestEvent(event);

        assertThat(result.transition()).isNull();
        verify(eventRepository).save(event);
        verify(transitionRepository, never()).save(any());
        verifyNoInteractions(thirdFilter);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0})
    void shouldNotSetExpectedBefore_WhenNextEventTtlSecondsIsNullOrZero(Integer ttlSeconds) {
        var subject = buildSubject(continuingFilter());
        var event = buildEvent("trace-1", ttlSeconds);
        when(transitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.empty());
        when(eventRepository.save(event)).thenReturn(event);
        when(transitionRepository.save(any(TraceTransition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = subject.ingestEvent(event);

        assertThat(result.transition().getExpectedBefore()).isNull();
    }

    @Test
    void shouldPassLatestTransitionForTrace_ToTheFilterChainContext() {
        var trace = new Trace();
        trace.setTraceId("trace-1");
        trace.setStatus(TraceStatus.WAITING_OTHER_EVENT);
        var priorEvent = new Event();
        priorEvent.setEventId("event-prior");
        priorEvent.setTrace(trace);
        var priorTransition = new TraceTransition();
        priorTransition.setTrace(trace);
        priorTransition.setEvent(priorEvent);
        var filter = mock(EventIngestionFilter.class);
        var subject = buildSubject(filter);
        var event = buildEvent("trace-1", 60);
        when(transitionRepository.findLatestByTraceId("trace-1")).thenReturn(Optional.of(priorTransition));
        when(eventRepository.save(event)).thenReturn(event);

        subject.ingestEvent(event);

        var captor = ArgumentCaptor.forClass(EventIngestionContext.class);
        verify(filter).doFilter(captor.capture(), any());
        assertThat(captor.getValue().getNewEvent()).isSameAs(event);
        assertThat(captor.getValue().getTransition()).isSameAs(priorTransition);
    }

    private EventIngestionServiceImpl buildSubject(EventIngestionFilter... filters) {
        return new EventIngestionServiceImpl(
                List.of(filters), traceIngestionService, eventRepository, traceRepository, transitionRepository);
    }

    private EventIngestionFilter continuingFilter() {
        var filter = mock(EventIngestionFilter.class);
        doAnswer(invocation -> {
            EventIngestionContext context = invocation.getArgument(0);
            EventIngestionFilterChain chain = invocation.getArgument(1);
            chain.doFilter(context);
            return null;
        }).when(filter).doFilter(any(), any());
        return filter;
    }

    private EventIngestionFilter rejectingFilter(String reason) {
        var filter = mock(EventIngestionFilter.class);
        doAnswer(invocation -> {
            EventIngestionContext context = invocation.getArgument(0);
            context.reject(reason);
            return null;
        }).when(filter).doFilter(any(), any());
        return filter;
    }

    private Event buildEvent(String traceId, Integer nextEventTtlSeconds) {
        var trace = new Trace();
        trace.setTraceId(traceId);
        trace.setStatus(TraceStatus.STARTED);
        var event = new Event();
        event.setEventId("event-1");
        event.setEventName("APPLICATION_RECEIVED");
        event.setTrace(trace);
        event.setNextEventTtlSeconds(nextEventTtlSeconds);
        return event;
    }
}
