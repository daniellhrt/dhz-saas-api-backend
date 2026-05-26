# Guia de Agentes de IA (dhz-saas-api-backend)

## Visao geral
- Backend SaaS multi-tenant para barbearias com Spring Boot 3 / Java 21; cada requisicao e escopada por `tenant_id` vindo do JWT (veja `src/main/java/br/com/dht/apibackend/security/SecurityFilter.java` + `src/main/java/br/com/dht/apibackend/config/TenantContext.java`).
- Os pacotes de dominio seguem contextos delimitados: `domain/barber`, `domain/client`, `domain/appointment`, `domain/catalog`, com Controllers/Services/Repositories em cada.
- O isolamento por tenant e row-level: services usam `TenantContext.getTenantId()` e repositories seguem `findByIdAndTenantId` / `findAllByTenantId` (exemplo: `src/main/java/br/com/dht/apibackend/domain/appointment/AppointmentService.java`).
- As regras de agendamento ficam em `AppointmentService` + `AppointmentRepository.hasOverlappingAppointment` (a query ignora `CANCELED`).

## Fluxo de auth e seguranca
- O JWT inclui claims `tenantId` e `role`, com `sub` = email (veja `src/main/java/br/com/dht/apibackend/security/TokenService.java`).
- `SecurityFilter` extrai o Bearer token, configura o auth do Spring Security + `TenantContext`, e limpa no `finally` para evitar vazamento de tenant.
- Rate limiting no login e aplicado em `RateLimitingFilter` (5 req/min por IP, Redis com fallback em memoria).
- Erros globais seguem `GlobalExceptionHandler` -> `StandardError` (bate com o formato de erro em `API.md`).

## Dados e migracoes
- PostgreSQL e o banco principal; schema via Flyway em `src/main/resources/db/migration/`.
- Profile ativo padrao e `dev` com integracao opcional do Spring Docker Compose; o profile `prod` e usado pelo Docker Compose (`compose.yaml`).

## Workflows locais (validado nos docs)
- Copie o template de env e rode tudo via Docker Compose (veja `README.md` / `API.md`):
  - `cp .env.example .env`
  - `docker compose up --build`
- Para debug em IDE, suba apenas o Postgres: `docker compose up postgres -d` e configure variaveis (JWT/DB/Redis) no run config.

## Convencoes a seguir
- Sempre escopo por tenant: use `TenantContext.getTenantId()` nos services e inclua `tenantId` nos repositories.
- Violacao de regra de negocio deve lançar `IllegalArgumentException` ou `IllegalStateException` para mapear em `400 Business Rule Violation`.
- Transicoes de agendamento sao estritas: `PENDING -> CONFIRMED -> COMPLETED`, `PENDING -> CANCELED` (veja `API.md`).
- Delete de catalogo e soft-delete (`active=false`); listagem retorna apenas ativos (veja `domain/catalog`).

## Integracoes e endpoints
- Dependencias externas: PostgreSQL, Redis, JWT (jjwt), Bucket4j, Springdoc OpenAPI (veja `pom.xml`).
- Swagger UI em `/swagger-ui/index.html` com a API rodando (veja `API.md`).
- Se precisar de CORS para o frontend, `API.md` tem um `CorsConfig` de exemplo em `config/`.
