package com.clara.challenge.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidEventException extends RuntimeException {

  public InvalidEventException(String reason) {
    super(reason);
  }
}
