# Architecture Overview

This document outlines the architectural decisions, structural patterns, and systemic behavior of the DHZ SaaS API Backend.

## System Overview

The application is a Multi-Tenant Software-as-a-Service (SaaS) backend for managing barbershops, built with:
- **Java 21**
- **Spring Boot 3.2.x**
- **PostgreSQL** (Production data store)
- **H2 Database** (Development/Testing)
- **Flyway** (Database migrations)

## Architectural Style

The application follows a **Layered Architecture** applied within a **Package-by-Feature (or Domain)** directory structure.

1. **Presentation Layer (`Controller`)**: Exposes RESTful endpoints, handles HTTP requests, and validates incoming payloads.
2. **Business Layer (`Service`)**: Orchestrates business rules, validations (e.g., anti-IDOR checks, double-booking prevention), and controls transaction boundaries using `@Transactional`.
3. **Persistence Layer (`Repository`)**: Interfaces with the database via Spring Data JPA. Queries are explicitly scoped to the current tenant.

## Multi-Tenancy Strategy

The system utilizes a **Shared Database, Shared Schema** approach for multi-tenancy.
Logical data isolation is enforced at the application level:

1. **Authentication**: Users (Barbers) authenticate and receive a JWT.
2. **Tenant ID Extraction**: The `SecurityFilter` intercepts requests, validates the JWT, and extracts the `tenantId`.
3. **Tenant Context**: The `tenantId` is injected into a `ThreadLocal` wrapper (`TenantContext`).
4. **Data Isolation**: Repositories explicitly use the `tenantId` in their queries (e.g., `WHERE tenantId = :tenantId`) to ensure data belonging to one tenant is never leaked to or modified by another.
5. **Cleanup**: The `SecurityFilter` strictly clears the `TenantContext` in a `finally` block to prevent tenant leakage across the application's thread pool.

## Security Architecture

- **Stateless Authentication**: Uses JWT (JSON Web Tokens). No session state is held on the server.
- **Password Hashing**: BCrypt is used to securely hash passwords before storing them in the database.
- **Filter Chain**: Custom `SecurityFilter` integrates with Spring Security to establish the security context (`UsernamePasswordAuthenticationToken`) per request.

## Key Design Patterns & Practices

- **DTO (Data Transfer Object)**: Decouples internal domain entities from the external API contract. DTOs are mapped to and from entities at the controller/service boundary (e.g., `AppointmentDTO`, `AuthDTO`).
- **Repository Pattern**: Abstracts data access using Spring Data JPA, providing a cleaner interface over JPA/Hibernate.
- **Dependency Injection**: Leverages Spring's IoC container. Dependencies are injected via constructors (mostly generated via Lombok's `@RequiredArgsConstructor`).
- **ThreadLocal Context**: Safe propagation of request-scoped context (like the tenant ID) down through the layers without explicitly passing it through method signatures.
- **Global Exception Handling**: `GlobalExceptionHandler` intercepts exceptions thrown across the application and standardizes them into structured JSON error responses (`StandardError`).
