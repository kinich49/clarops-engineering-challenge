# Flow: an invalid event (finalEvent=true declaring a nextExpectedEvent)
# is rejected with 400, but is still persisted and doesn't break the
# trace's ability to progress normally afterward.

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "invalid-event-1-{{run_id}}",
  "traceId": "trace-invalid-event-400-{{run_id}}",
  "eventName": "APPLICATION_RECEIVED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:00:00Z",
  "finalEvent": true,
  "nextExpectedEvent": "RULES_EVALUATED"
}

HTTP 400
[Asserts]
jsonpath "$.status" == 400
jsonpath "$.error" == "Bad Request"

GET http://localhost:8080/api/traces/trace-invalid-event-400-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-invalid-event-400-{{run_id}}"
jsonpath "$.status" == "STARTED"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == "RULES_EVALUATED"
jsonpath "$.nextExpectedBefore" == null

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "invalid-event-2-{{run_id}}",
  "traceId": "trace-invalid-event-400-{{run_id}}",
  "eventName": "RULES_EVALUATED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:05:00Z",
  "finalEvent": false,
  "nextExpectedEvent": "SHIPMENT_DISPATCHED"
}

HTTP 200

GET http://localhost:8080/api/traces/trace-invalid-event-400-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-invalid-event-400-{{run_id}}"
jsonpath "$.status" == "WAITING_OTHER_EVENT"
jsonpath "$.lastEventName" == "RULES_EVALUATED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == "SHIPMENT_DISPATCHED"
jsonpath "$.nextExpectedBefore" == null
