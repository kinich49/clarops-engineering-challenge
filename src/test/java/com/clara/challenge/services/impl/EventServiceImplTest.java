package com.clara.challenge.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.TraceTransition;
import com.clara.challenge.entities.db.enums.EventResult;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.entities.json.EventJson;
import com.clara.challenge.entities.json.EventResultJson;
import com.clara.challenge.entities.misc.EventDTO;
import com.clara.challenge.services.internal.EventIngestionService;
import com.clara.challenge.services.internal.TraceIngestionService;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

  @Mock private TraceIngestionService traceIngestionService;

  @Mock private EventIngestionService eventIngestionService;

  @InjectMocks private EventServiceImpl subject;

  @Test
  void shouldMapEventJsonAndReturnTraceJson_WhenEventIsAccepted() {
    var trace = new Trace();
    trace.setTraceId("trace-1");
    trace.setStatus(TraceStatus.WAITING_OTHER_EVENT);
    var expectedBefore = Instant.now().plusSeconds(60);
    when(traceIngestionService.findOrCreate("trace-1")).thenReturn(trace);
    when(eventIngestionService.ingestEvent(any()))
        .thenAnswer(
            invocation -> {
              Event persisted = invocation.getArgument(0);
              var transition = new TraceTransition();
              transition.setTrace(trace);
              transition.setEvent(persisted);
              transition.setExpectedBefore(expectedBefore);
              return new EventDTO(persisted, transition);
            });
    var eventJson = buildEventJson();

    var result = subject.acceptEvent(eventJson);

    assertThat(result).isPresent();
    var traceJson = result.get();
    assertThat(traceJson.getTraceId()).isEqualTo("trace-1");
    assertThat(traceJson.getStatus()).isEqualTo("WAITING_OTHER_EVENT");
    assertThat(traceJson.getLastEventName()).isEqualTo("APPLICATION_RECEIVED");
    assertThat(traceJson.getLastEventResult()).isEqualTo("SUCCESS");
    assertThat(traceJson.getNextExpectedEvent()).isEqualTo("RULES_EVALUATED");
    assertThat(traceJson.getNextExpectedBefore()).isEqualTo(expectedBefore);
  }

  @Test
  void shouldReturnTraceJsonWithoutNextExpectedBefore_WhenEventIsNotAccepted() {
    var trace = new Trace();
    trace.setTraceId("trace-1");
    trace.setStatus(TraceStatus.STARTED);
    when(traceIngestionService.findOrCreate("trace-1")).thenReturn(trace);
    when(eventIngestionService.ingestEvent(any()))
        .thenAnswer(
            invocation -> {
              Event persisted = invocation.getArgument(0);
              return new EventDTO(persisted, null);
            });
    var eventJson = buildEventJson();

    var result = subject.acceptEvent(eventJson);

    assertThat(result).isPresent();
    assertThat(result.get().getNextExpectedBefore()).isNull();
  }

  @Test
  void shouldBuildEventFromEventJson_AndAssociateItWithTheResolvedTrace() {
    var trace = new Trace();
    trace.setTraceId("trace-1");
    trace.setStatus(TraceStatus.STARTED);
    when(traceIngestionService.findOrCreate("trace-1")).thenReturn(trace);
    when(eventIngestionService.ingestEvent(any()))
        .thenAnswer(
            invocation -> {
              Event persisted = invocation.getArgument(0);
              return new EventDTO(persisted, null);
            });
    var eventJson = buildEventJson();

    var before = Instant.now();
    subject.acceptEvent(eventJson);
    var after = Instant.now();

    var captor = ArgumentCaptor.forClass(Event.class);
    verify(eventIngestionService).ingestEvent(captor.capture());
    var capturedEvent = captor.getValue();
    assertThat(capturedEvent.getEventId()).isEqualTo("event-1");
    assertThat(capturedEvent.getEventName()).isEqualTo("APPLICATION_RECEIVED");
    assertThat(capturedEvent.getEventResult()).isEqualTo(EventResult.SUCCESS);
    assertThat(capturedEvent.isFinalEvent()).isFalse();
    assertThat(capturedEvent.getMetadata()).isEqualTo(Map.of("key", "value"));
    assertThat(capturedEvent.getNextExpectedEvent()).isEqualTo("RULES_EVALUATED");
    assertThat(capturedEvent.getNextEventTtlSeconds()).isEqualTo(60);
    assertThat(capturedEvent.getTrace()).isSameAs(trace);
    assertThat(capturedEvent.getReceivedAt()).isBetween(before, after);
  }

  @ParameterizedTest
  @EnumSource(EventResultJson.class)
  void shouldMapEventResult_ForEachEventResultJsonValue(EventResultJson resultJson) {
    var trace = new Trace();
    trace.setTraceId("trace-1");
    trace.setStatus(TraceStatus.STARTED);
    when(traceIngestionService.findOrCreate("trace-1")).thenReturn(trace);
    when(eventIngestionService.ingestEvent(any()))
        .thenAnswer(
            invocation -> {
              Event persisted = invocation.getArgument(0);
              return new EventDTO(persisted, null);
            });
    var eventJson = buildEventJson();
    eventJson.setResult(resultJson);
    var expected = resultJson == EventResultJson.SUCCESS ? EventResult.SUCCESS : EventResult.ERROR;

    subject.acceptEvent(eventJson);

    var captor = ArgumentCaptor.forClass(Event.class);
    verify(eventIngestionService).ingestEvent(captor.capture());
    assertThat(captor.getValue().getEventResult()).isEqualTo(expected);
  }

  private EventJson buildEventJson() {
    var json = new EventJson();
    json.setEventId("event-1");
    json.setTraceId("trace-1");
    json.setEventName("APPLICATION_RECEIVED");
    json.setResult(EventResultJson.SUCCESS);
    json.setOccurredAt(Instant.now());
    json.setNextExpectedEvent("RULES_EVALUATED");
    json.setNextEventTtlSeconds(60);
    json.setFinalEvent(false);
    json.setMetadata(Map.of("key", "value"));
    return json;
  }
}
