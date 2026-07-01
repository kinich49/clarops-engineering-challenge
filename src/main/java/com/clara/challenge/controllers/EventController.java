package com.clara.challenge.controllers;

import com.clara.challenge.entities.json.EventJson;
import com.clara.challenge.services.api.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

  private final EventService eventService;

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public void postEvent(@RequestBody EventJson eventJson) {
    eventService.acceptEvent(eventJson).orElseThrow(RuntimeException::new);
  }
}
