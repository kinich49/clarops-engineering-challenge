-- ============================================================
-- Clarops Challenge — Initial Schema
-- Distributed event tracking, TTL expiration, and operational
-- flow analysis.
-- Idempotent — safe to re-execute.
-- UUIDs must be provided by the application layer.
-- ============================================================
CREATE
  SCHEMA IF NOT EXISTS clarops_challenge_schema;
SET
search_path TO clarops_challenge_schema;

-- -------------------------
-- health
-- Single-row table used by the health endpoint.
-- -------------------------
CREATE
  TABLE
    IF NOT EXISTS health(
      id BIGSERIAL PRIMARY KEY,
      message VARCHAR(255) NOT NULL
    );

CREATE
  TABLE IF NOT EXISTS
    traces(
      trace_id VARCHAR PRIMARY KEY,
      status VARCHAR NOT NULL,
      registration_datetime TIMESTAMPTZ NOT NULL,
      completed_datetime TIMESTAMPTZ NULL
    );

CREATE
  TABLE IF NOT EXISTS
    events(
      event_id VARCHAR PRIMARY KEY,
      trace_id VARCHAR NOT NULL REFERENCES traces(trace_id),
      event_name VARCHAR NOT NULL,
      event_result VARCHAR(7) NOT NULL,
      occurred_at TIMESTAMPTZ NOT NULL,
      received_at TIMESTAMPTZ NOT NULL,
      next_expected_event VARCHAR NULL,
      next_event_ttl_seconds INT NULL,
      final_event BOOLEAN NOT NULL DEFAULT FALSE,
      metadata JSONB NULL
    );

CREATE
  TABLE IF NOT EXISTS
    trace_transitions(
      id BIGSERIAL PRIMARY KEY,
      trace_id VARCHAR NOT NULL REFERENCES traces(trace_id),
      event_id VARCHAR NOT NULL REFERENCES events(event_id),
      expected_event_name VARCHAR NOT NULL,
      ttl_seconds INT NOT NULL,
      expected_before TIMESTAMPTZ NOT NULL,
      registration_datetime TIMESTAMPTZ NOT NULL
    );

CREATE  IF NOT EXISTS
  INDEX idx_events_trace_id ON
  events(
    trace_id,
    occurred_at DESC
  );

CREATE
  INDEX idx_trace_transitions_trace_id ON
  trace_transitions(
    trace_id,
    id DESC
  );

CREATE
  INDEX idx_trace_transitions_expected_before ON
  trace_transitions(expected_before);

INSERT
  INTO
    health(message) SELECT
      'clarops sr engineer challenge'
    WHERE
      NOT EXISTS(
        SELECT
          1
        FROM
          health
      );
