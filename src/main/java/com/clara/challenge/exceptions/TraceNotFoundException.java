package com.clara.challenge.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class TraceNotFoundException extends RuntimeException {

  public TraceNotFoundException(String traceId) {
    super("Trace not found: " + traceId);
  }
}
