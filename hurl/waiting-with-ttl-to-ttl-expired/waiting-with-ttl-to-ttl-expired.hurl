# Flow: WAITING_OTHER_EVENT (with ttl) -> TTL_EXPIRED_FOR_EVENT
#
# The first event sets a short TTL (2s) on the expectation. The second
# request that would otherwise satisfy that expectation is deliberately
# delayed (4s, via [Options] delay) so the deadline has genuinely elapsed
# server-side before it's processed, causing the ingestion path to mark
# the trace TTL_EXPIRED_FOR_EVENT instead of advancing it normally.
#
# nextExpectedBefore is server-generated (registration time + ttl), so it
# can't be asserted with an exact value. We assert it matches a valid
# ISO-8601 instant while the expectation is still open, and that it's
# null once the trace has expired.

POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "tte-event-1-{{run_id}}",
  "traceId": "trace-waiting-ttl-expired-{{run_id}}",
  "eventName": "APPLICATION_RECEIVED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:00:00Z",
  "finalEvent": false,
  "nextExpectedEvent": "RULES_EVALUATED",
  "nextEventTtlSeconds": 2
}

HTTP 200

GET http://localhost:8080/api/traces/trace-waiting-ttl-expired-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-waiting-ttl-expired-{{run_id}}"
jsonpath "$.status" == "WAITING_OTHER_EVENT"
jsonpath "$.lastEventName" == "APPLICATION_RECEIVED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == "RULES_EVALUATED"
jsonpath "$.nextExpectedBefore" matches "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z$"

POST http://localhost:8080/api/events
Content-Type: application/json
[Options]
delay: 4000
{
  "eventId": "tte-event-2-{{run_id}}",
  "traceId": "trace-waiting-ttl-expired-{{run_id}}",
  "eventName": "RULES_EVALUATED",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:05:00Z",
  "finalEvent": false
}

HTTP 200

GET http://localhost:8080/api/traces/trace-waiting-ttl-expired-{{run_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "trace-waiting-ttl-expired-{{run_id}}"
jsonpath "$.status" == "TTL_EXPIRED_FOR_EVENT"
jsonpath "$.lastEventName" == "RULES_EVALUATED"
jsonpath "$.lastEventResult" == "SUCCESS"
jsonpath "$.nextExpectedEvent" == null
jsonpath "$.nextExpectedBefore" == null
