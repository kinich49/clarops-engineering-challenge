# Flow: WAITING_OTHER_EVENT -> event result=FAILURE but not final (status
# stays non-terminal, IN_PROGRESS) -> COMPLETED
#
# This distinguishes an individual event's own result (lastEventResult,
# which can be ERROR) from the trace's overall status: the trace only
# moves to the terminal ERROR status when the *final* event fails.

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "went-event-1-{{run_id}}",
  "traceId": "trace-waiting-error-result-not-terminal-{{run_id}}",
  "eventName": "APPLICATION_RECEIVED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:00:00Z",
  "finalEvent": false,
  "nextExpectedEvent": "RULES_EVALUATED"
}

HTTP 200

GET http://localhost:8080/api/traces/trace-waiting-error-result-not-terminal-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-waiting-error-result-not-terminal-{{run_id}}"
jsonpath "$.status" == "WAITING_OTHER_EVENT"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == "RULES_EVALUATED"
jsonpath "$.nextExpectedBefore" == null

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "went-event-2-{{run_id}}",
  "traceId": "trace-waiting-error-result-not-terminal-{{run_id}}",
  "eventName": "RULES_EVALUATED",
  "result": "FAILURE",
  "occurredAt": "2026-01-01T05:05:00Z",
  "finalEvent": false
}

HTTP 200

GET http://localhost:8080/api/traces/trace-waiting-error-result-not-terminal-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-waiting-error-result-not-terminal-{{run_id}}"
jsonpath "$.status" == "IN_PROGRESS"
jsonpath "$.lastEventName" == "RULES_EVALUATED"
jsonpath "$.lastEventResult" == "ERROR"
jsonpath "$.nextExpectedEvent" == null
jsonpath "$.nextExpectedBefore" == null

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "went-event-3-{{run_id}}",
  "traceId": "trace-waiting-error-result-not-terminal-{{run_id}}",
  "eventName": "FINALIZED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:10:00Z",
  "finalEvent": true
}

HTTP 200

GET http://localhost:8080/api/traces/trace-waiting-error-result-not-terminal-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-waiting-error-result-not-terminal-{{run_id}}"
jsonpath "$.status" == "COMPLETED"
jsonpath "$.lastEventName" == "FINALIZED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == null
jsonpath "$.nextExpectedBefore" == null
