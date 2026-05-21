# Codebase Concerns & Tech Debt

This document outlines technical debt, security concerns, performance bottlenecks, and code quality issues identified in the codebase.

## 1. Architecture & Design (Multi-Tenancy)
- **Manual Tenant Isolation**: The application implements Multi-Tenancy by manually fetching the `tenantId` from `TenantContext` and explicitly passing it to Repository methods (e.g., `findByEmailAndTenantId`).
  - **Risk**: High risk of **IDOR (Insecure Direct Object Reference)** and cross-tenant data leaks. If a developer forgets to include the `tenantId` parameter in a query, data from other tenants could be exposed or modified.
  - **Recommendation**: Refactor to use Hibernate Filters (`@FilterDef` and `@Filter`) so the tenant isolation is enforced transparently and globally at the ORM level.

## 2. Security Concerns
- **Improper HTTP Status on Login Failure**: In `AuthService`, invalid credentials throw an `IllegalArgumentException("Credenciais inválidas.")`. The `GlobalExceptionHandler` maps this to an HTTP `400 Bad Request`.
  - **Recommendation**: Create a specific exception (e.g., `UnauthorizedException`) and map it to HTTP `401 Unauthorized` to correctly reflect authentication failures.
- **Missing Rate Limiting**: The public authentication endpoint `/api/v1/auth/login` is exposed without any rate limiting, leaving it vulnerable to brute-force or credential stuffing attacks.

## 3. Observability & Error Handling
- **Swallowed Internal Server Errors**: The `GlobalExceptionHandler` catches generic `Exception.class` to return a safe 500 error, but it fails to actually log the exception. It contains a placeholder comment: `// Em um cenário real, aqui entraria um log.error("Erro interno", ex);`.
  - **Risk**: Stack traces and critical application errors are completely lost, making production debugging nearly impossible.
  - **Recommendation**: Integrate a logging framework (e.g., SLF4J/Logback via `@Slf4j`) and log the exception details before returning the generic response.

## 4. Test Coverage Gaps
- **Missing Critical Tests**: While the `Appointment` domain and `SecurityFilter` have test coverage, several core domains are entirely missing unit and integration tests:
  - `AuthService` and `TokenService` (Handling critical security logic).
  - `Client` domain (Service, Repository, Controller).
  - `Catalog` domain (Service, Repository, Controller).
  - **Recommendation**: Prioritize adding unit and integration tests for authentication and remaining business domains.

## 5. Performance Bottlenecks
- **Lack of Pagination**: Methods such as `ClientService.listAllClients()` and `CatalogService.listActiveServices()` fetch all records for a tenant into memory at once using `findAllByTenantId`.
  - **Risk**: As the database grows, this will cause high memory consumption, slow response times, and potential OutOfMemory errors.
  - **Recommendation**: Update endpoints and repositories to accept `Pageable` and return paginated data (`Page<T>`).

## 6. API Documentation
- **No Swagger/OpenAPI**: The codebase lacks standard API documentation. Given this is a SaaS API backend, having an interactive API contract is crucial for frontend integration.
  - **Recommendation**: Include `springdoc-openapi-starter-webmvc-ui` in the `pom.xml` to automatically generate the OpenAPI spec and Swagger UI.
