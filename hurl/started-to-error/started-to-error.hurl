# Flow: STARTED -> ERROR
# A trace is created implicitly by its first event. That first event is
# final and reports a FAILURE result, so the trace goes straight to the
# terminal ERROR status.

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "se-event-1-{{run_id}}",
  "traceId": "trace-started-error-{{run_id}}",
  "eventName": "APPLICATION_RECEIVED",
  "result": "FAILURE",
  "occurredAt": "2026-01-01T05:00:00Z",
  "finalEvent": true
}

HTTP 200

GET http://localhost:8080/api/traces/trace-started-error-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-started-error-{{run_id}}"
jsonpath "$.status" == "ERROR"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "ERROR"
jsonpath "$.nextExpectedEvent" == null
jsonpath "$.nextExpectedBefore" == null
