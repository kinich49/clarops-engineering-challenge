# Flow: STARTED -> COMPLETED -> a new event arrives, trace status remains the same
# COMPLETED is terminal, so a subsequent event is persisted but rejected
# by ingestion: no new transition is created, and the trace's reported
# status/last-event still reflect the original COMPLETED transition.

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "sci-event-1-{{run_id}}",
  "traceId": "trace-started-completed-immutable-{{run_id}}",
  "eventName": "APPLICATION_RECEIVED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:00:00Z",
  "finalEvent": true
}

HTTP 200

GET http://localhost:8080/api/traces/trace-started-completed-immutable-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-started-completed-immutable-{{run_id}}"
jsonpath "$.status" == "COMPLETED"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == null
jsonpath "$.nextExpectedBefore" == null

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "sci-event-2-{{run_id}}",
  "traceId": "trace-started-completed-immutable-{{run_id}}",
  "eventName": "SHOULD_BE_IGNORED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:05:00Z",
  "finalEvent": false
}

HTTP 200

GET http://localhost:8080/api/traces/trace-started-completed-immutable-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-started-completed-immutable-{{run_id}}"
jsonpath "$.status" == "COMPLETED"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == null
jsonpath "$.nextExpectedBefore" == null
