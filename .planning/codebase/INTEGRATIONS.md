# Integrações — dhz-saas-api-backend

> Mapeado em: 2026-05-21

## Banco de Dados

### PostgreSQL 16 (Alpine)

- **Conexão:** Docker Compose local (porta 5432)
- **Database:** `barberdb`
- **Credenciais:** `admin` / `adminpassword` (apenas dev)
- **Driver:** `org.postgresql` (runtime)
- **ORM:** Spring Data JPA + Hibernate
- **Migrações:** Flyway (`src/main/resources/db/migration/`)

### H2 (Runtime)

- Disponível como dependência runtime
- Provável uso para testes ou fallback
- Sem configuração explícita encontrada

## Autenticação

### JWT (JJWT 0.12.5)

- **Tipo:** Bearer token no header `Authorization`
- **Signing:** HMAC-SHA com chave secreta
- **Claims:** email (subject) + tenantId (customizado)
- **Expiração:** 24 horas
- **Endpoint:** `POST /api/v1/auth/login`
- **Senha:** BCrypt hash

## Multi-Tenancy

### Row-Level Isolation

- **Estratégia:** Coluna `tenant_id` em todas as tabelas
- **Contexto:** `TenantContext` (ThreadLocal) populado a partir do JWT
- **Ciclo de vida:** Set no `SecurityFilter`, limpo no `finally` block
- **Sem tenant resolver de URL/header** — tenant vem exclusivamente do JWT

## Integrações Externas

**Nenhuma integração externa detectada.**

Não foram encontrados:
- ❌ APIs externas (REST clients, WebClient, Feign)
- ❌ Mensageria (Kafka, RabbitMQ, SQS)
- ❌ Cache (Redis, Caffeine)
- ❌ Storage (S3, GCS, Azure Blob)
- ❌ Email (SMTP, SendGrid, SES)
- ❌ Monitoramento (Actuator, Micrometer, Prometheus)
- ❌ Logging centralizado (ELK, CloudWatch)
- ❌ OAuth/SSO providers
- ❌ Webhooks

## Observações

O projeto está em estágio inicial — apenas infraestrutura básica (DB + Auth + CRUD). Não há integrações com serviços externos, o que é esperado para um MVP em desenvolvimento.

---
*Mapeado: 2026-05-21 via gsd-map-codebase*
