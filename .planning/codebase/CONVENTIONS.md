# CONVENTIONS.md

## Overview
This document outlines the coding conventions, formatting rules, naming conventions, and patterns found in the `dhz-saas-api-backend` project.

## Architecture & Structure
- **Package by Feature/Domain**: The codebase is organized by domain in `src/main/java/br/com/dht/apibackend/domain/*` (e.g., `appointment`, `barber`, `catalog`, `client`) and cross-cutting concerns in `security`, `config`, and `exception`.
- **Layered Architecture**: Within a domain package, the application follows a layered approach: Controller -> Service -> Repository.
- **Multitenancy**: The application is designed as a SaaS with multitenancy. Tenant isolation is managed via a thread-local context (`TenantContext.getTenantId()`). Entities store a `tenantId`, and all repository queries and service validations explicitly check against this tenant ID to prevent IDOR (Insecure Direct Object Reference) and data leakage.

## Coding & Formatting Conventions
- **Language**: Java 21 with Spring Boot 3.2.5.
- **Documentation**: Javadoc style block comments are used at the top of key classes to define their purpose, responsibility, and architectural role. Comments are written in Portuguese.
    ```java
    /**
     * Propósito: [What the class does]
     * Responsabilidade: [Core responsibilities]
     * Papel na Arquitetura: [e.g., Domain / Service]
     */
    ```
- **Dependency Injection**: Constructor injection is exclusively used via Lombok's `@RequiredArgsConstructor` and `private final` fields.
- **Language**: Internal comments, business rule validation messages, and error responses are written in Portuguese.

## Naming Conventions
- **Classes**: PascalCase (e.g., `AppointmentService`, `AppointmentDTO`).
- **Methods/Variables**: camelCase (e.g., `scheduleAppointment`, `tenantId`).
- **Database Tables**: Pluralized, snake_case strings (e.g., `appointments`, `client_id`). Configured explicitly via JPA `@Table` and `@Column` annotations.
- **Endpoints**: Pluralized resource names with API versioning (e.g., `/api/v1/appointments`).

## Design Patterns & Practices
- **DTOs (Data Transfer Objects)**: DTOs are implemented using Java 21 `record`s. They are typically nested inside a wrapper class (e.g., `AppointmentDTO`) containing `Request` and `Response` records.
- **Static Factory Methods**: DTO responses often use a static factory method named `fromEntity(Entity e)` to map JPA entities to records.
- **Entity Guidelines**:
  - Annotated with `@Entity`, `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, and `@EqualsAndHashCode(of = "id")`.
  - Use `UUID` for primary keys with `GenerationType.UUID`.
  - Utilize explicit `@JoinColumn` and `FetchType.LAZY` for associations.
  - Creation timestamps are managed via Hibernate's `@CreationTimestamp` (`updatable = false`).
  - Entities often have custom constructors that initialize default state (like `status`) and do not accept auto-managed fields (like `id` or `createdAt`).
- **Exception Handling**: Handled globally via a `@RestControllerAdvice` class (`GlobalExceptionHandler`). 
  - Standardized JSON responses using a custom `StandardError` builder.
  - `IllegalArgumentException` and `IllegalStateException` represent business rule violations and return a 400 Bad Request.
  - Validation exceptions (`MethodArgumentNotValidException`) aggregate all field errors into a single, comma-separated string.
  - Generic exceptions return 500 without leaking stack traces.
- **Security**: Stateless JWT authentication. Routes are secured by default, except for specific public endpoints like login. Passwords are encrypted using BCrypt.
- **Defensive Programming**: Services explicitly check if related entities belong to the current tenant and if they are in an active/valid state before proceeding with mutations.
