# Estrutura — dhz-saas-api-backend

> Mapeado em: 2026-05-21

## Layout do Diretório

```
dhz-saas-api-backend/
├── .gitattributes                        # Line endings para Maven wrapper
├── .gitignore                            # Spring Initializr padrão
├── .mvn/                                 # Maven Wrapper
├── compose.yaml                          # Docker Compose (PostgreSQL 16)
├── HELP.md                               # Docs gerados pelo Spring Initializr
├── mvnw / mvnw.cmd                       # Maven Wrapper scripts
├── pom.xml                               # Maven POM (Spring Boot 3.2.5, Java 21)
├── src/
│   ├── main/
│   │   ├── java/br/com/dht/apibackend/
│   │   │   ├── DhzSaasApiBackendApplication.java     # Entry point
│   │   │   ├── config/
│   │   │   │   ├── JwtProperties.java                # @ConfigurationProperties JWT
│   │   │   │   └── TenantContext.java                # ThreadLocal tenant holder
│   │   │   ├── domain/
│   │   │   │   ├── appointment/
│   │   │   │   │   ├── Appointment.java              # Entity
│   │   │   │   │   ├── AppointmentController.java    # POST /api/v1/appointments
│   │   │   │   │   ├── AppointmentDTO.java           # Request/Response records
│   │   │   │   │   ├── AppointmentRepository.java    # JPA + overlap query
│   │   │   │   │   ├── AppointmentService.java       # Agendamento + anti-IDOR
│   │   │   │   │   └── AppointmentStatus.java        # Enum (PENDING/CONFIRMED/COMPLETED/CANCELED)
│   │   │   │   ├── barber/
│   │   │   │   │   ├── Barber.java                   # Entity (tenant owner)
│   │   │   │   │   └── BarberRepository.java         # findByEmail
│   │   │   │   ├── catalog/
│   │   │   │   │   ├── CatalogController.java        # CRUD /api/v1/catalog
│   │   │   │   │   ├── CatalogService.java           # Lógica de catálogo
│   │   │   │   │   ├── ServiceItem.java              # Entity (preço, duração, ativo)
│   │   │   │   │   ├── ServiceItemDTO.java           # Request/Response records
│   │   │   │   │   └── ServiceItemRepository.java    # Queries com tenant
│   │   │   │   └── client/
│   │   │   │       ├── Client.java                   # Entity
│   │   │   │       ├── ClientController.java         # CRUD /api/v1/clients
│   │   │   │       ├── ClientDTO.java                # Request/Response records
│   │   │   │       ├── ClientRepository.java         # Queries com tenant
│   │   │   │       └── ClientService.java            # Lógica + null check tenant
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java       # @RestControllerAdvice
│   │   │   │   └── StandardError.java                # Error response DTO
│   │   │   └── security/
│   │   │       ├── AuthController.java               # POST /api/v1/auth/login
│   │   │       ├── AuthService.java                  # Login + JWT generation
│   │   │       ├── SecurityConfig.java               # Spring Security config
│   │   │       ├── SecurityFilter.java               # JWT filter + TenantContext
│   │   │       ├── TokenService.java                 # JWT create/validate
│   │   │       └── dto/
│   │   │           └── AuthDTO.java                  # LoginRequest/TokenResponse
│   │   └── resources/
│   │       ├── application.yml                       # Config (JWT, profiles, JPA)
│   │       ├── db/migration/
│   │       │   ├── V1__create_table_clients.sql
│   │       │   ├── V2__create_table_barbers.sql
│   │       │   ├── V3__create_table_service_items.sql
│   │       │   └── V4__create_table_appointments.sql
│   │       ├── static/                               # (vazio)
│   │       └── templates/                            # (vazio)
│   └── test/
│       └── java/br/com/dht/apibackend/
│           └── DhzSaasApiBackendApplicationTests.java  # Smoke test apenas
└── target/                                           # Build output
```

## Inventário de Arquivos

| Tipo | Quantidade | Localização |
|---|---|---|
| Java (main) | 26 | `src/main/java/br/com/dht/apibackend/` |
| Java (test) | 1 | `src/test/java/br/com/dht/apibackend/` |
| SQL Migrations | 4 | `src/main/resources/db/migration/` |
| Config | 1 | `src/main/resources/application.yml` |
| Docker | 1 | `compose.yaml` |
| Maven | 3 | `pom.xml`, `mvnw`, `mvnw.cmd` |
| **Total** | **36** | — |

## Locais Chave

| O que procurar | Onde encontrar |
|---|---|
| Entry point | `DhzSaasApiBackendApplication.java` |
| Configuração JWT | `config/JwtProperties.java` + `application.yml` |
| Tenant isolation | `config/TenantContext.java` + `security/SecurityFilter.java` |
| Nova entity/domínio | `domain/[nome]/` (seguir padrão existente) |
| Novo endpoint | `domain/[nome]/[Nome]Controller.java` |
| Regras de negócio | `domain/[nome]/[Nome]Service.java` |
| Erros globais | `exception/GlobalExceptionHandler.java` |
| Migrações de banco | `src/main/resources/db/migration/V[N]__*.sql` |
| Segurança | `security/SecurityConfig.java` |
| Docker | `compose.yaml` |

## Convenções de Nomes

| Elemento | Padrão | Exemplo |
|---|---|---|
| Package | `domain.[domínio]` | `domain.appointment` |
| Entity | `[Nome]` | `Appointment.java` |
| Controller | `[Nome]Controller` | `AppointmentController.java` |
| Service | `[Nome]Service` | `AppointmentService.java` |
| Repository | `[Nome]Repository` | `AppointmentRepository.java` |
| DTO | `[Nome]DTO` (wrapper) | `AppointmentDTO.java` |
| DTO Request | `[Nome]DTO.Request` (record) | `AppointmentDTO.Request` |
| DTO Response | `[Nome]DTO.Response` (record) | `AppointmentDTO.Response` |
| Migration | `V[N]__[descrição].sql` | `V4__create_table_appointments.sql` |
| API path | `/api/v1/[recurso]` | `/api/v1/appointments` |
| Tabela SQL | plural lowercase | `appointments` |
| Coluna SQL | snake_case | `tenant_id`, `start_time` |

---
*Mapeado: 2026-05-21 via gsd-map-codebase*
