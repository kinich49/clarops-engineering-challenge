package com.clara.challenge.entities.db.enums;

public enum TraceStatus {
  STARTED,
  WAITING_OTHER_EVENT,
  IN_PROGRESS,
  TTL_EXPIRED_FOR_EVENT,
  COMPLETED,
  ERROR
}
