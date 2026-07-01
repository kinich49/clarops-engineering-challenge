POST http://localhost:8080/api/events
Content-Type: application/json
{
  "eventId": "{{event_id}}",
  "traceId": "{{trace_id}}",
  "eventName": "{{event_name}}",
  "result": "SUCCESS",
  "occurredAt": "2026-01-01T05:00:00Z",
  "finalEvent": false,
  "metadata": {
    "country": "MX",
    "entityId": "company-123"
  }
}

HTTP 200

GET http://localhost:8080/api/traces/{{trace_id}}/status

HTTP 200
[Asserts]
jsonpath "$.traceId" == "{{trace_id}}"
jsonpath "$.status" == "STARTED"
jsonpath "$.lastEventName" == "{{event_name}}"

