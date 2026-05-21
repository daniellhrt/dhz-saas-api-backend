# Directory Structure

This document maps out the core directory structure of the application and the responsibilities of each module.

## High-Level Layout

```text
dhz-saas-api-backend/
├── .planning/                  # Project planning and architecture documents
├── src/main/java/              # Application source code
├── src/main/resources/         # Application properties and database migrations
├── src/test/                   # Unit and integration tests
├── pom.xml                     # Maven configuration and dependencies
└── compose.yaml                # Docker Compose file for local dependencies (PostgreSQL)
```

## Source Code Map (`src/main/java/br/com/dht/apibackend/`)

The source code is primarily organized by **Domain (Feature)**, keeping related controllers, services, repositories, and models together. Cross-cutting concerns are organized by technical function.

### `config/`
Cross-cutting configurations and context holders.
- **`TenantContext`**: A `ThreadLocal` wrapper that stores the current request's tenant ID, providing data isolation across the application.
- **`JwtProperties`**: Maps JWT configuration properties from `application.yml`.

### `domain/`
The core business capabilities of the application.

#### `domain/appointment/`
Manages the scheduling lifecycle.
- **Responsibilities**: Booking appointments, validating anti-IDOR checks (ensuring client/service belong to the tenant), calculating service end times, and preventing double-booking conflicts.
- **Key Components**: `Appointment` (Entity), `AppointmentController`, `AppointmentService`, `AppointmentRepository`, `AppointmentDTO`.

#### `domain/barber/`
Manages the barbershop owners (the users of the system).
- **Responsibilities**: Storing barber profiles and hashed credentials. The barber entity typically represents the tenant owner.
- **Key Components**: `Barber` (Entity), `BarberRepository`.

#### `domain/catalog/`
Manages the services offered by the barbershop.
- **Responsibilities**: Handling the catalog of services (e.g., Haircut, Beard Trim), including prices, durations, and active status.
- **Key Components**: `ServiceItem` (Entity), `CatalogController`, `CatalogService`, `ServiceItemRepository`.

#### `domain/client/`
Manages the barbershop's customers.
- **Responsibilities**: Storing and retrieving customer information (name, phone, email) specific to a tenant.
- **Key Components**: `Client` (Entity), `ClientController`, `ClientService`, `ClientRepository`.

### `exception/`
Global error handling mechanics.
- **Responsibilities**: Intercepting exceptions thrown by controllers or services and converting them into predictable, standardized HTTP responses.
- **Key Components**: `GlobalExceptionHandler`, `StandardError`.

### `security/`
Authentication and Authorization.
- **Responsibilities**: Handling login requests, validating passwords, issuing JWTs, and intercepting incoming requests to establish the Security and Tenant contexts.
- **Key Components**: 
  - `AuthController` & `AuthService`: Entry points for login and token generation.
  - `SecurityFilter`: Extracts the JWT, validates it, and injects the `tenantId` into the `TenantContext`.
  - `TokenService`: Utility for generating and parsing JWTs.
  - `SecurityConfig`: Spring Security filter chain configuration.

## Resources Map (`src/main/resources/`)

- **`application.yml` / `application-prod.yml`**: Spring Boot configuration profiles.
- **`db/migration/`**: Flyway SQL migration scripts that define the database schema (e.g., `V1__create_table_clients.sql`, `V2__create_table_barbers.sql`).
