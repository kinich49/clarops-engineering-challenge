## Task 1

Define the DDL schema.
Required tables:
- Events
- Traces
- TraceTransitions
-

### Considerations

An Event is tied to one Trace. A Trace is tied to many Events.
A Trace is tied to many TraceTransitions.
A TraceTransition is tied to one event.
TraceTransition will keep the Trace flow, and will bind Trace and the last Event. This will help persisting all Events without polluting the Trace flow
A Trace can be in the status:
- STARTED when an event arrives and declares no nextExpectedEvent
- WAITING_OTHER_EVENT when an event arrives and declares a nextExpectedEvent
- TTL_EXPIRED_FOR_EVENT when an event was expecting an event within a TTL window
- COMPLETED when an event is marked as isFinal

## Task 2

Generate Repositories
- EventRepository
- TraceRepository
- TranceTransitionRepository

They should extend JpaRepository.

## Task 3

Generate the DTO classes that will be returned from the controller
- EventJson
- TraceJson

Add these annotations to avoid serializing issues.

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
```

## Task 4

Two types of Service interfaces will be used
- api
- internal

api services will be used for controller - service communication. The return type, if any other than void, will be the jackson-annotated classes.
internal services will be used for service-to-service communication. The return type, if any other than void, will be the JPA-annotated classes.

Generate the internal EventIngestionService. Responsibilities:
- Validate an event. There will be events that should not affect the Trace flow. There will be events that should immediately terminate the request and return a bad request.
- All events will be persisted
-
- If the event is valid, create the TraceTransition object
- Update the trace status

Generate the api service EventService. Responsibilities:
- Get the current or a new trace from the TraceRepository
- Send the event to EventIngestionService for processing
- Map the EventIngestionService result to a Json class

Generate the api service TraceService. Responsibilities:
- Find the latest TraceTransition given a trace-id
- Map the result to the JSON class

# Task 5

Generate two controllers:
- EventController
- POST /api/events
- Send the request body to EventService
- TraceController
- GET /api/traces/{trace-id}/status
- Send the trace-id to TraceService
- Throw TraceNotFoundException if the Trace does not exists

# Task 6

Generate the unit tests for the service layer using the requirements from AGENTS.md

# Task 7

Add a filter layer that will be used in the EventIngestionService service.
This will follow a similar approach as the FilterChain used in Spring. Each filter will either decide to mark the event as not valid to process, or call the next filter in the chain

Two filters:
- ExpectedEventMatchFilter: Will mark the event as not processable if the event name doesnt match the expected event name
- TraceNotCompletedFilter: Will mark the event as not processable if the current trace is marked as completed

Generate Unit tests for these classes.

# Task 8

Update EventIngestionService to use the filters.
If the event is marked as not processable, do not create a TraceTransition. Create it otherwise.
Persist the event in any case.

# Task 9

Add a new filter that will throw InvalidEventException if the event state is in an incongruent state. Examples:
- Event is marked as final and declares a nextExpectedEvent
- Event declares a TTL but doesnt declare a nextExpectedEvent

Do not throw the exception if the other filters mark the event as invalid

# Task 10

Modify the TraceService to add the number of invalid events and valid events per filter.
The property processed in Events will define if an event is valid or not. Processed equals true means the event is valid

# Task 11

Add Trace status:
- IN_PROGRESS when an event was in WAITING_FOR_EVENT, the expected event arrived within the TTL window, but the new event is not final and does not delcare a nextExpectedEvent
- ERROR when an event result was failure and marked as isFinal = true

# Task 12

Modify EventIngestionService to allow a configurable buffer to the TTLs if any
This buffer:
- should be turned on and off in the properties
- the extended window should be in the properties
- If the buffer is turned on, the TTL is extended by the seconds it is configured with

