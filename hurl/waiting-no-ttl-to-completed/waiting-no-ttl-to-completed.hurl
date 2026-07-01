# Flow: WAITING_OTHER_EVENT (no ttl) -> COMPLETED

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "wnc-event-1-{{run_id}}",
  "traceId": "trace-waiting-no-ttl-completed-{{run_id}}",
  "eventName": "APPLICATION_RECEIVED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:00:00Z",
  "finalEvent": false,
  "nextExpectedEvent": "RULES_EVALUATED"
}

HTTP 200

GET http://localhost:8080/api/traces/trace-waiting-no-ttl-completed-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-waiting-no-ttl-completed-{{run_id}}"
jsonpath "$.status" == "WAITING_OTHER_EVENT"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == "RULES_EVALUATED"
jsonpath "$.nextExpectedBefore" == null

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "wnc-event-2-{{run_id}}",
  "traceId": "trace-waiting-no-ttl-completed-{{run_id}}",
  "eventName": "RULES_EVALUATED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:05:00Z",
  "finalEvent": true
}

HTTP 200

GET http://localhost:8080/api/traces/trace-waiting-no-ttl-completed-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-waiting-no-ttl-completed-{{run_id}}"
jsonpath "$.status" == "COMPLETED"
jsonpath "$.lastEventName" == "RULES_EVALUATED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == null
jsonpath "$.nextExpectedBefore" == null
