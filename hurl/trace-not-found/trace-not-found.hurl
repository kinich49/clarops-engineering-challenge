# Flow: GET a trace-id that never received any event -> 404

GET http://localhost:8080/api/traces/trace-that-does-not-exist/status

HTTP 404
[Asserts]
jsonpath "$.status" == 404
jsonpath "$.error" == "Not Found"
