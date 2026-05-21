# Integrations & External Services

## Databases
- **PostgreSQL**: The primary relational database system used in production (version `16-alpine` via Docker). The application connects to it using Spring Data JPA and standard JDBC.
- **H2 Database**: Used as an in-memory database fallback for local development and automated testing.

## Infrastructure & Orchestration
- **Docker & Docker Compose**: The project relies on a `compose.yaml` file to orchestrate the API and the PostgreSQL database. The application leverages `spring-boot-docker-compose` to automatically manage the database lifecycle during development.

## Third-Party APIs
- *No external third-party HTTP APIs (such as payment gateways like Stripe, or communication services like Twilio/SendGrid) are currently integrated into this repository.*

## Identity & Access Management
- *No external Identity Provider (IdP) such as Keycloak, Auth0, or AWS Cognito is used.* 
- Authentication is handled internally by the application using custom-signed JWTs (JSON Web Tokens) with a secret key provided via environment variables (`JWT_SECRET`).
