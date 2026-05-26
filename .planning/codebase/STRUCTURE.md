# Structure

> Last mapped: 2026-05-26

## Directory Layout

```
dhz-saas-api-backend/
├── src/
│   ├── main/
│   │   ├── java/br/com/dht/apibackend/
│   │   │   ├── DhzSaasApiBackendApplication.java      # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java                     # CORS filter (configurable origins)
│   │   │   │   ├── JwtProperties.java                  # @ConfigurationProperties for JWT
│   │   │   │   ├── RedisConfig.java                    # Lettuce ProxyManager for Bucket4j
│   │   │   │   └── TenantContext.java                  # ThreadLocal<String> tenant isolation
│   │   │   ├── domain/
│   │   │   │   ├── appointment/                        # Scheduling bounded context
│   │   │   │   │   ├── Appointment.java                # Entity (JPA)
│   │   │   │   │   ├── AppointmentController.java      # REST endpoints
│   │   │   │   │   ├── AppointmentDTO.java             # Request/Response/Block/Cancel DTOs
│   │   │   │   │   ├── AppointmentRepository.java      # JPA + custom overlap query
│   │   │   │   │   ├── AppointmentService.java         # State machine + double-booking
│   │   │   │   │   └── AppointmentStatus.java          # Enum: PENDING/CONFIRMED/IN_PROGRESS/COMPLETED/CANCELED
│   │   │   │   ├── barber/                             # Barber management bounded context
│   │   │   │   │   ├── Barber.java                     # Entity with role + tenantId
│   │   │   │   │   ├── BarberController.java           # CRUD endpoints
│   │   │   │   │   ├── BarberDTO.java                  # Register/Create/Update/Response DTOs
│   │   │   │   │   ├── BarberRepository.java           # findByEmail, findByIdAndTenantId
│   │   │   │   │   ├── BarberRole.java                 # Enum: ADMIN/USER
│   │   │   │   │   └── BarberService.java              # ADMIN role enforcement
│   │   │   │   ├── catalog/                            # Service catalog bounded context
│   │   │   │   │   ├── CatalogController.java          # CRUD + soft-delete
│   │   │   │   │   ├── CatalogService.java             # Soft-delete (active=false)
│   │   │   │   │   ├── ServiceItem.java                # Entity with price/duration
│   │   │   │   │   ├── ServiceItemDTO.java             # Request/Response DTOs
│   │   │   │   │   └── ServiceItemRepository.java      # findAllByTenantIdAndActiveTrue
│   │   │   │   ├── client/                             # Client management bounded context
│   │   │   │   │   ├── Client.java                     # Entity with phone/cpf/birthDate
│   │   │   │   │   ├── ClientController.java           # CRUD endpoints
│   │   │   │   │   ├── ClientDTO.java                  # Request/Response DTOs
│   │   │   │   │   ├── ClientRepository.java           # Tenant-scoped queries
│   │   │   │   │   └── ClientService.java              # Email uniqueness per tenant
│   │   │   │   ├── product/                            # Product inventory bounded context
│   │   │   │   │   ├── Product.java                    # Entity
│   │   │   │   │   ├── ProductController.java          # CRUD endpoints
│   │   │   │   │   ├── ProductDTO.java                 # Request/Response DTOs
│   │   │   │   │   ├── ProductRepository.java          # Tenant-scoped queries
│   │   │   │   │   └── ProductService.java             # Business logic
│   │   │   │   └── sale/                               # Sales bounded context
│   │   │   │       ├── Sale.java                       # Entity (header)
│   │   │   │       ├── SaleController.java             # CRUD endpoints
│   │   │   │       ├── SaleDTO.java                    # Request/Response DTOs
│   │   │   │       ├── SaleItem.java                   # Entity (line item)
│   │   │   │       ├── SaleRepository.java             # Tenant-scoped queries
│   │   │   │       └── SaleService.java                # Business logic
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java         # @RestControllerAdvice → StandardError
│   │   │   │   ├── InvalidCredentialsException.java    # 401 Unauthorized
│   │   │   │   └── StandardError.java                  # { timestamp, status, error, message, path }
│   │   │   └── security/
│   │   │       ├── AuthController.java                 # /auth/login, /auth/register
│   │   │       ├── AuthService.java                    # Credential validation + JWT issuance
│   │   │       ├── RateLimitingFilter.java             # Bucket4j 5 req/min per IP on /login
│   │   │       ├── SecurityConfig.java                 # SecurityFilterChain, BCrypt, CORS
│   │   │       ├── SecurityFilter.java                 # JWT extraction + TenantContext
│   │   │       ├── TokenService.java                   # JWT create/validate/extract
│   │   │       └── dto/
│   │   │           └── AuthDTO.java                    # LoginRequest / TokenResponse
│   │   └── resources/
│   │       ├── application.yml                         # Main config (dev/prod profiles)
│   │       └── db/migration/                           # Flyway V1-V9
│   └── test/
│       ├── java/br/com/dht/apibackend/
│       │   ├── DhzSaasApiBackendApplicationTests.java  # Context loads test
│       │   ├── domain/
│       │   │   ├── BaseIntegrationTest.java            # @SpringBootTest + integration profile
│       │   │   ├── MultiTenantIntegrationTest.java     # Cross-tenant isolation E2E
│       │   │   ├── appointment/
│       │   │   │   ├── AppointmentControllerTest.java  # MockMvc controller test
│       │   │   │   ├── AppointmentIntegrationTest.java # Full workflow E2E
│       │   │   │   └── AppointmentServiceTest.java     # 25 unit tests
│       │   │   ├── barber/
│       │   │   │   ├── BarberControllerTest.java       # MockMvc controller test
│       │   │   │   ├── BarberIntegrationTest.java      # Register+CRUD E2E
│       │   │   │   └── BarberServiceTest.java          # 12 unit tests
│       │   │   ├── catalog/
│       │   │   │   ├── CatalogControllerTest.java      # MockMvc controller test
│       │   │   │   ├── CatalogIntegrationTest.java     # Create+soft-delete E2E
│       │   │   │   └── CatalogServiceTest.java         # 10 unit tests
│       │   │   └── client/
│       │   │       ├── ClientControllerTest.java        # MockMvc controller test
│       │   │       ├── ClientIntegrationTest.java       # CRUD+uniqueness E2E
│       │   │       └── ClientServiceTest.java           # 10 unit tests
│       │   └── security/
│       │       ├── AuthorizationTest.java              # ADMIN vs USER role E2E
│       │       ├── InputValidationTest.java            # JSR-380 + StandardError format
│       │       ├── RateLimitingTest.java               # Bucket4j 5/min unit test
│       │       └── SecurityFilterTest.java             # JWT + TenantContext unit test
│       └── resources/
│           └── application-integration.yml             # Integration test profile
├── .env / .env.example                                 # Environment variables
├── compose.yaml                                        # Docker Compose (postgres + redis + api)
├── Dockerfile                                          # Multi-stage build
├── pom.xml                                             # Maven + dependencies
└── mvnw / mvnw.cmd                                    # Maven Wrapper
```

## Naming Conventions

### Packages
- Domain packages: `domain/{context}` (e.g., `domain/barber`, `domain/appointment`)
- Each context contains: `Entity`, `Controller`, `Service`, `Repository`, `DTO`, optional `Enum`

### Files
- Entities: `{Name}.java` (e.g., `Barber.java`, `Appointment.java`)
- Controllers: `{Name}Controller.java` (e.g., `BarberController.java`)
- Services: `{Name}Service.java` (e.g., `BarberService.java`)
- Repositories: `{Name}Repository.java` (e.g., `BarberRepository.java`)
- DTOs: `{Name}DTO.java` with nested records (e.g., `BarberDTO.RegisterRequest`, `BarberDTO.Response`)
- Tests: `{Name}ServiceTest.java`, `{Name}ControllerTest.java`, `{Name}IntegrationTest.java`

### REST Endpoints
- Base: `/api/v1/{resource}` (e.g., `/api/v1/barbers`, `/api/v1/clients`)
- Auth: `/api/v1/auth/login`, `/api/v1/auth/register`
