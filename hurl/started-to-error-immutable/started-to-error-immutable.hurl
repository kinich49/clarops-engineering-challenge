# Flow: STARTED -> ERROR -> a new event arrives, trace status remains the same
# ERROR is terminal (set only by a FINAL failing event), so a subsequent
# event is persisted but rejected by ingestion: no new transition is
# created, and the trace's reported status/last-event still reflect the
# original ERROR transition.

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "sei-event-1-{{run_id}}",
  "traceId": "trace-started-error-immutable-{{run_id}}",
  "eventName": "APPLICATION_RECEIVED",
  "result": "FAILURE",
  "occurredAt": "2026-01-01T05:00:00Z",
  "finalEvent": true
}

HTTP 200

GET http://localhost:8080/api/traces/trace-started-error-immutable-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-started-error-immutable-{{run_id}}"
jsonpath "$.status" == "ERROR"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "ERROR"
jsonpath "$.nextExpectedEvent" == null
jsonpath "$.nextExpectedBefore" == null

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "sei-event-2-{{run_id}}",
  "traceId": "trace-started-error-immutable-{{run_id}}",
  "eventName": "SHOULD_BE_IGNORED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:05:00Z",
  "finalEvent": false
}

HTTP 200

GET http://localhost:8080/api/traces/trace-started-error-immutable-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-started-error-immutable-{{run_id}}"
jsonpath "$.status" == "ERROR"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "ERROR"
jsonpath "$.nextExpectedEvent" == null
jsonpath "$.nextExpectedBefore" == null
