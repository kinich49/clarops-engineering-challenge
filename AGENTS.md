# Project Overview

A microservice that will act as a Watchdog for events and will keep track of them.
Each event will be associated to a trace-id in a one-to-many relationship
Each event will act as a finite state machine, basic states are:
- STARTED
- WAITING_OTHER_EVENT
- COMPLETED (terminal state)
- TTL_EXPIRED_FOR_EVENT (terminal state)

The trace-id will keep track of the current state. An event might move the state from one event to another

## Architecture

The project will use a basic architecture of Controllers for entrypoints, Services for data manipulation, and Repositories
for data fetching.

There are two different types of services, one intended to be used only by Controllers, under api/ package, and
another one to be used in Services, under internal/

Controllers will communicate only with Services under api. These services will return data classes annotated with Jackson

Preferred package structure will be:

- com.clara.challenge.controllers
- com.clara.challenge.services
- com.clara.challenge.services.api
- com.clara.challenge.services.impl
- com.clara.challenge.services.internal
- com.clara.challenge.entities
- com.clara.challenge.entities.json

# Guardrails

## When developing new features

- The JSON is the public contract, do not modify it without explicit permission and the reason why it is necessary
- The schema is negotiable. Avoid modifying an Entity without explicit permission and the reason why it is necessary
- If a dependency is missing, propose a version but wait for confirmation.
- Prefer interfaces even if they only have one method and one implementation
- Controllers should only communicate with a Service.

## When adding tests

- Test classes must be in the test counterpart, i.e. src/main/com/example/MyClass goes to src/test/com/example/MyClassTest
- The class to be tested will use subject as variable name.
- Avoid Testing empty Spring Data repositories. Only test them when there are custom methods or custom implementations

### Unit Tests

- Test classes have the Test suffix i.e. MyClass - MyClassTest
- Test method names should follow: shouldExpectedBehavior_WhenCondition.
- Use Arrange / Act / Assert structure but do not add the structure as comment
- Test classes must be in the test counterpart, i.e. src/main/com/example/MyClass goes to src/test/com/example/MyClassTest
- Analyze inputs and expected outputs for the methods to be tested. Best case scenario, an interface will define the public contract of the test
- Prefer @ParameterizedTests to test different inputs
- libs to leverage:
  - JUnit 5
  - Mockito

### Integration Tests

- Test classes should have 'IT' suffix i.e. MyClass - MyClassIT
- ITs should be a SpringBootTest
- libs to leverage:
  - MockMVC
  - assertJ
  - RestTemplate

