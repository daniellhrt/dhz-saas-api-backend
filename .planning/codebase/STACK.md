# Stack — dhz-saas-api-backend

> Mapeado em: 2026-05-21

## Linguagem & Runtime

- **Java 21** (LTS)
- **Maven** (wrapper incluído: `mvnw` / `mvnw.cmd`)

## Framework Principal

- **Spring Boot 3.2.5** (via `spring-boot-starter-parent`)
  - Spring Web (REST APIs)
  - Spring Data JPA (persistência)
  - Spring Security (autenticação/autorização)
  - Spring Validation (Bean Validation)
  - Spring Boot DevTools (hot reload em dev)
  - Spring Boot Docker Compose (gerenciamento automático de containers)

## Banco de Dados

- **PostgreSQL 16** (Alpine) — banco principal em produção/dev
- **H2** — disponível como dependência runtime (provável uso em testes)
- **Flyway** — migrações de schema versionadas (`V1` a `V4`)
- **Hibernate** — modo `validate` (schema gerenciado exclusivamente pelo Flyway)

## Segurança & Autenticação

- **Spring Security** — filtro stateless, CSRF desabilitado
- **JJWT 0.12.5** (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
  - JWT com HMAC-SHA signing
  - Claims: `subject` (email), `tenantId` (claim customizado)
  - Expiração: 24 horas
- **BCrypt** — hash de senhas

## Bibliotecas Utilitárias

- **Lombok** — redução de boilerplate (getters, setters, builders)

## Infraestrutura

- **Docker Compose** — orquestra PostgreSQL 16 Alpine
  - Container: `barbersaas-postgres`
  - DB: `barberdb`, user: `admin`
  - Spring Boot gerencia lifecycle automaticamente (`start-and-stop`)

## Build & Plugins

- **spring-boot-maven-plugin** — empacotamento com exclusão de Lombok
- **Maven Wrapper** — garante versão consistente do Maven

## Configuração

| Propriedade | Valor | Notas |
|---|---|---|
| `spring.profiles.active` | `dev` | Perfil padrão |
| `app.security.jwt.secret` | hex hardcoded | Apenas para dev |
| `app.security.jwt.expiration-ms` | 86400000 (24h) | — |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Flyway controla schema |
| `spring.jpa.show-sql` | `true` | SQL logging em dev |

## Schema do Banco (Flyway)

4 tabelas, todas multi-tenant via coluna `tenant_id`:

| Tabela | PK | Constraints | Índices |
|---|---|---|---|
| `clients` | UUID | `uk_clients_tenant_email` (tenant_id, email) | `idx_clients_tenant_id` |
| `barbers` | UUID | `email` UNIQUE (global) | `idx_barbers_tenant_id` |
| `service_items` | UUID | `uk_service_items_tenant_name` (tenant_id, name) | `idx_service_items_tenant` |
| `appointments` | UUID | FK → clients, FK → service_items | `idx_appointments_tenant_time`, `idx_appointments_client` |

---
*Mapeado: 2026-05-21 via gsd-map-codebase*
