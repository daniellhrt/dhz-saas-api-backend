# DHZ SaaS API Backend

## What This Is

Sistema de backend multilocatário (multi-tenant) para gestão de barbearias. A API fornece funcionalidades essenciais como cadastro de clientes, catálogo de serviços e agendamentos com cálculo automático e prevenção de choques de horário, servindo como base sólida e escalável para clientes SaaS.

## Core Value

Isolamento seguro de dados entre barbearias (multi-tenancy) e estabilidade da API, garantindo que as operações essenciais (agendamentos e catálogo) ocorram de forma previsível e sem falhas de concorrência.

## Current State

**Shipped:** v1.0 Infra & Testing (2026-05-22)
- ~3.000 LOC Java 21 / Spring Boot 3.2.5
- 31 classes Java, 5 classes de teste, 4 migrations Flyway
- API multi-tenant com JWT, rate limiting, Docker, CI via GitHub Actions
- 13/13 requisitos cobertos e verificados via UAT

**Shipped:** v1.1 Evolução da API (2026-05-22)
- CRUD de barbeiros com dois níveis de acesso (ADMIN/USER)
- Gestão de status de agendamentos (confirmar, cancelar, concluir)
- Rate limiting distribuído com Redis + Bucket4j
- Testes para Client e Catalog
- 6 novas classes, 4 migrations Flyway, 16+ testes
- 11/11 UAT fase 3, security verified (9/9 threats closed)

## Requirements

### Validated

- ✓ Autenticação de barbeiros via JWT com isolamento por tenant — v1.0
- ✓ Gestão de clientes (CRUD com validações e isolamento) — v1.0
- ✓ Gestão do catálogo de serviços (preço, duração e desativação lógica) — v1.0
- ✓ Sistema de agendamentos com validação de double-booking — v1.0
- ✓ Segredos externalizados via variáveis de ambiente (JWT, DB) — v1.0
- ✓ Dockerfile multi-stage + compose.yaml — v1.0
- ✓ Testes unitários (Service), integração (Controller), repositório (H2) — v1.0
- ✓ GitHub Actions CI funcional — v1.0
- ✓ Registro de barbeiros com roles ADMIN/USER (CRUD) — v1.1
- ✓ Gestão de status de agendamentos (confirmar, cancelar, concluir) — v1.1
- ✓ Rate limiting distribuído via Redis com Bucket4j — v1.1
- ✓ Testes unitários e de integração para Client e Catalog — v1.1

### Active

- [ ] Hospedagem do Backend em nuvem (Railway/Render) com Postgres e Redis — v1.2
- [ ] Configurações de Produção (CORS, JWT Secret forte, Env Vars) — v1.2
- [ ] Hospedagem do Frontend (Vercel) e apontamento para a API — v1.2

### Out of Scope

- Deploy contínuo (CD) automatizado — O ambiente alvo final ainda não está provisionado
- Novos domínios de negócio (ex: financeiro, relatórios) — Foco em evoluir a base atual

## Context

- Projeto foi iniciado com Spring Boot 3.2.5 e Java 21, utilizando Spring Security para autenticação.
- Já possui banco de dados PostgreSQL orquestrado via Docker Compose e migrações Flyway.
- O código atual não possui cobertura de testes de negócio, representando um risco para evolução.
- Segredos de segurança estão *hardcoded*, o que é um bloqueio para publicação em nuvem.

## Constraints

- **Segurança**: Segredos não podem estar no código fonte — Precisam ser gerenciados via variáveis de ambiente.
- **Isolamento de Dados (Anti-IDOR)**: O `tenant_id` deve continuar sendo validado rigorosamente a cada request.
- **Qualidade**: Modificações e adições devem possuir cobertura de testes.

## Key Decisions

<!-- Decisions that constrain future work. Add throughout project lifecycle. -->

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Priorização de Testes e Infra | A API básica está funcional, mas precisa de redes de segurança antes de ir para produção ou receber novas funcionalidades. | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state
---
*Last updated: 2026-05-26 after completing v1.1 milestone*
