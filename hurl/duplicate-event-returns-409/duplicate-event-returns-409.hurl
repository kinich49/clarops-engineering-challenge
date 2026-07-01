# Flow: posting the same eventId twice is rejected with 409 on the
# second attempt, and the trace's state is unaffected by the duplicate.

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "dup-event-1-{{run_id}}",
  "traceId": "trace-duplicate-event-409-{{run_id}}",
  "eventName": "APPLICATION_RECEIVED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:00:00Z",
  "finalEvent": false,
  "nextExpectedEvent": "RULES_EVALUATED"
}

HTTP 200

GET http://localhost:8080/api/traces/trace-duplicate-event-409-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-duplicate-event-409-{{run_id}}"
jsonpath "$.status" == "WAITING_OTHER_EVENT"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == "RULES_EVALUATED"
jsonpath "$.nextExpectedBefore" == null

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "dup-event-1-{{run_id}}",
  "traceId": "trace-duplicate-event-409-{{run_id}}",
  "eventName": "APPLICATION_RECEIVED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:00:00Z",
  "finalEvent": false,
  "nextExpectedEvent": "RULES_EVALUATED"
}

HTTP 409
[Asserts]
jsonpath "$.status" == 409
jsonpath "$.error" == "Conflict"

GET http://localhost:8080/api/traces/trace-duplicate-event-409-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-duplicate-event-409-{{run_id}}"
jsonpath "$.status" == "WAITING_OTHER_EVENT"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == "RULES_EVALUATED"
jsonpath "$.nextExpectedBefore" == null
