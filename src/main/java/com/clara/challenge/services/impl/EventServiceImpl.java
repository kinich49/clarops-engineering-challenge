package com.clara.challenge.services.impl;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.json.EventJson;
import com.clara.challenge.entities.json.TraceJson;
import com.clara.challenge.exceptions.DuplicateEventException;
import com.clara.challenge.repositories.EventRepository;
import com.clara.challenge.services.api.EventService;
import com.clara.challenge.services.internal.EventIngestionService;
import com.clara.challenge.services.internal.TraceIngestionService;
import com.clara.challenge.utils.EntityMapper;
import com.clara.challenge.utils.JsonMapper;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

  private final TraceIngestionService traceIngestionService;
  private final EventIngestionService eventIngestionService;
  private final EventRepository eventRepository;

  @Override
  public Optional<TraceJson> acceptEvent(EventJson eventJson) {
    if (eventRepository.existsById(eventJson.getEventId())) {
      throw new DuplicateEventException(eventJson.getEventId());
    }

    var trace = traceIngestionService.findOrCreate(eventJson.getTraceId());
    var event = buildEvent(eventJson);
    event.setTrace(trace);
    var dto = eventIngestionService.ingestEvent(event);

    return Optional.of(JsonMapper.toJson(dto));
  }

  private Event buildEvent(EventJson json) {
    var event = new Event();
    event.setEventId(json.getEventId());
    event.setEventName(json.getEventName());
    event.setEventResult(EntityMapper.toEventResult(json.getResult()));
    event.setFinalEvent(json.isFinalEvent());
    event.setMetadata(json.getMetadata());
    event.setNextExpectedEvent(json.getNextExpectedEvent());
    event.setNextEventTtlSeconds(json.getNextEventTtlSeconds());
    event.setOccurredAt(json.getOccurredAt());
    event.setReceivedAt(Instant.now());

    return event;
  }
}
