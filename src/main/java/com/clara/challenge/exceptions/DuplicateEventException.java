package com.clara.challenge.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class DuplicateEventException extends RuntimeException {

  public DuplicateEventException(String eventId) {
    super("Event already exists: " + eventId);
  }
}
