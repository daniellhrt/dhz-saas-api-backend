# Technology Stack

## Core
- **Language**: Java 21
- **Framework**: Spring Boot 3.2.5
- **Build Tool**: Maven 3.x

## Spring Ecosystem
- **Spring Web**: For building RESTful APIs.
- **Spring Data JPA**: For Object-Relational Mapping (ORM) and data access.
- **Spring Validation**: For request body and bean validation.
- **Spring Security**: For securing endpoints, authentication, and authorization.
- **Spring Boot DevTools**: For rapid development cycles.

## Security
- **Authentication**: JSON Web Tokens (JWT) using the `jjwt` library (v0.12.5) for stateless, token-based authentication.

## Database & Migrations
- **Production Database**: PostgreSQL
- **Development/Test Database**: H2 (In-memory database)
- **Schema Management**: Flyway Core for version-controlled database migrations.

## Utilities & Tooling
- **Code Generation**: Lombok (reduces boilerplate like getters, setters, constructors).
- **Containerization**: Docker and Docker Compose (integrated natively via `spring-boot-docker-compose`).

## Testing
- **Frameworks**: Spring Boot Starter Test, Spring Security Test.
