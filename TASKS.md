## Task 1

Implement Trace end-to-end

## Subtask 1

Add a Spring Data Repository for Trace entity. This repository should fetch Traces by trace-id,

## Subtask 2

Add a Json entity for Trace using Jackson annotations.
Expected json schema is:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": [
    "traceId",
    "status",
    "lastEventName",
    "lastEventResult",
    "nextExpectedEvent",
    "nextExpectedBefore",
    "eventsReceived"
  ],
  "properties": {
    "traceId": {
      "type": "string"
    },
    "status": {
      "type": "string",
      "enum": ["WAITING_OTHER_EVENT", "COMPLETED", "FAILED"]
    },
    "lastEventName": {
      "type": "string",
      "enum": ["APPLICATION_RECEIVED", "RULES_EVALUATED"]
    },
    "lastEventResult": {
      "type": "string",
      "enum": ["SUCCESS", "FAILURE"]
    },
    "nextExpectedEvent": {
      "type": "string",
      "enum": ["RULES_EVALUATED"]
    },
    "nextExpectedBefore": {
      "type": "string",
      "format": "date-time"
    },
    "eventsReceived": {
      "type": "integer",
      "minimum": 0
    }
  },
  "additionalProperties": false
}
```

## Subtask 3

Add an api Service that will return a Json from a trace_id. Throw a TraceNotFoundException if not found

## Subtask 4

Add unit tests for the TraceService.

## Subtask 5

Add a TraceController and expose `GET /traces/{traceId}/status`

## Subtask 6

Add IntegrationTests for the TraceController

