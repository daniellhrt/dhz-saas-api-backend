# TESTING.md

## Overview
This document describes the testing strategy, frameworks used, file locations, and how to execute tests in the `dhz-saas-api-backend` project.

## Testing Strategy
The project relies on automated testing to ensure the correctness of the domain logic, controllers, and data access layer.
- **Unit Testing (Services)**: Focused on business rules, anti-IDOR checks, conflict validations (e.g., double-booking), and state transformations. External dependencies like repositories are mocked.
- **Web Layer Testing (Controllers)**: Focused on HTTP request validation (e.g., `@Valid`), JSON serialization/deserialization, and correct HTTP status code returns. The service layer is mocked, and security filters are explicitly disabled (`@AutoConfigureMockMvc(addFilters = false)`) to isolate the web layer behavior.
- **Data Access Testing**: Includes repository layer testing with sliced contexts.

## Frameworks & Libraries
- **JUnit 5 (Jupiter)**: The primary test execution framework.
- **Mockito**: Used extensively for mocking dependencies via annotations (`@Mock`, `@InjectMocks`, `@MockBean`).
- **Spring Boot Test**: Provides annotations and utilities for slicing the application context (e.g., `@WebMvcTest`).
- **MockMvc**: Used to perform mock HTTP requests and assert responses in controller tests (`status()`, `jsonPath()`).

## Conventions & Structure
- **Location**: Test files mirror the package structure of `src/main/java` and are located under `src/test/java/br/com/dht/apibackend/`.
- **Structure**: Tests strictly follow the **AAA (Arrange, Act, Assert)** pattern, often explicitly separated by code comments (`// Arrange`, `// Act`, `// Assert`).
- **Naming Convention**: Test methods describe the expected behavior and conditions using the pattern `should[Action/ExpectedResult]When[Condition]` (e.g., `shouldScheduleAppointmentSuccessfully`, `shouldThrowExceptionWhenClientNotFoundInTenant`, `shouldReturn400WhenDateIsInThePast`).
- **Context Management**: Multitenant context is manually set up before tests and cleared after execution to prevent state leakage between tests. This is done using JUnit lifecycle hooks:
  ```java
  @BeforeEach
  void setUp() {
      TenantContext.setTenantId(TENANT_ID);
  }

  @AfterEach
  void tearDown() {
      TenantContext.clear();
  }
  ```

## How to Run Tests
Since this is a standard Maven project, tests can be executed from the command line using the included Maven wrapper.

To run all tests:
```bash
./mvnw test
```
*(On Windows Command Prompt or PowerShell, use `mvnw.cmd test`)*

To run a specific test class:
```bash
./mvnw test -Dtest=AppointmentServiceTest
```
*(On Windows Command Prompt or PowerShell, use `mvnw.cmd test -Dtest=AppointmentServiceTest`)*
