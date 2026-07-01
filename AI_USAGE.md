# AI Tools used
Claude code
# Main prompts used
Im thinking about defining:

Events:

    event_id
    trace_id
    registration_datetime
    event_name

    result
    Traces:
    trace_id
    status
    registration_datetime

    completed_datetime
    TraceTransition
    id
    trace_id
    last_event_id
    ttl
    registration_datetime

This way I can keep adding events even if the trace is completed to future debugging.

Issues I can see:

I still need to query the TraceTransition and order by registration_date to know the current state
I dont like the status in the Trace. I feel it should be separated as we have all the information needed, i. e. if completed_datetime is not null, the state is completed. If the last entry in TraceTransition is still within the TTL, the status is waiting, same with started.

im thinking about adding a safeguard.

For example, if producers are having delays and we dont want to force failing all the events, we could add a little safeguard.

By adding a property in the app (safeguard.enabled=true, safeguard.factor = 1.1) we can easily give more time for the events to arrive. Once the issue upstream is fixed, we return to safeguard.enable = false)

Analyze if any mod to the entities is required as this will be calculated runtime

enhance EventIngestionServiceImpl to modify the Trace state:

If Event should be ingested:
- COMPLETED if event.isFinal is true
- WAITING_OTHER_EVENT if event.nextExpectedEvent is present
- STARTED if event.nextExpectedEvent is not present, and event.isFinal is false

# Prompt used to define the unit test standard.
Is in AGENS.md

# Prompts used to generate or refine Hurl tests.
write some basic hurl tests, all within hurl folder, and all on its own folder
- Test for basic flow: WAITING_OTHER_EVENT no ttl -> COMPLETED
- Test for flow: WAITING_OTHER_EVENT no ttl -> IN_PROGRES -> COMPLETED
- Test for flow: STARTED -> ERROR
- Test for flow: WAITING_OTHER_EVENT with ttl -> TTL_EXPIRED
- Test for flow: STARTED -> COMPLETED -> New event arrives, trace status remains the same
- Test for flow: WAITING_OTHER_EVENT with status error but not terminal -> COMPLETED
- Test for trace not found

For all tests:
Each validation of the flow should be:
- POST Event and validate the http status
- GET Trace and validate the http status and body

For each validation:
- Assert the expected http status 200
- Inspect the json body:
    - last_event should match the event name when applicable
    - nextExpected event should match the next even when applicable
    - expectedBefore should match when applicable (pay close attention about how to handle the date)
    - last event result should match

# What parts of the solution were generated or assisted by AI.
Unit Tests, Hurl tests, and Filter layer mainly. After the service layer was defined by me, I was able to tell Claude how to enhance the service.

#  Which AI suggestions you accepted.
The Filter implementations, how to preserve context in the filters (two boolean flags for example)

# Which AI suggestions you rejected and why.
The way to handle the Trace Status was overcomplicated for a MVP. Given more time I would have accepted it

# Any important correction you made to the AI output.
The DDL. I did not accept the proposed schema.

# Any part of the generated code that required manual review or adjustment.
The filters. 


