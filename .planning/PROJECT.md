# DHZ SaaS API Backend

## What This Is

Sistema de backend multilocatário (multi-tenant) para gestão de barbearias. A API fornece funcionalidades essenciais como cadastro de clientes, catálogo de serviços e agendamentos com cálculo automático e prevenção de choques de horário, servindo como base sólida e escalável para clientes SaaS.

## Core Value

Isolamento seguro de dados entre barbearias (multi-tenancy) e estabilidade da API, garantindo que as operações essenciais (agendamentos e catálogo) ocorram de forma previsível e sem falhas de concorrência.

## Requirements

### Validated

<!-- Shipped and confirmed valuable. -->

- ✓ Autenticação de barbeiros via JWT com isolamento por tenant — existing
- ✓ Gestão de clientes (CRUD com validações e isolamento) — existing
- ✓ Gestão do catálogo de serviços (preço, duração e desativação lógica) — existing
- ✓ Sistema de agendamentos com validação de double-booking — existing

### Active

<!-- Current scope. Building toward these. -->

- [ ] Implementar cobertura completa de testes (unitários e integração) para Services e Controllers.
- [ ] Externalizar segredos de infraestrutura (JWT secret, senhas do banco) para uso em múltiplos ambientes via variáveis de ambiente.
- [ ] Preparar infraestrutura de deploy containerizada (Docker) para publicação na nuvem (AWS/DigitalOcean/Render).
- [ ] Configurar versionamento no GitHub, preparando o terreno para futuros pipelines de CI.
- [ ] Corrigir dívidas técnicas identificadas no código (ex: bug de validação no DTO de serviço).

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- Adição de novos fluxos de negócio na V1 — O foco atual deve ser na estabilização, testes e infraestrutura para garantir que a base atual suporte crescimento seguro.
- CI/CD complexo no momento — Apenas a base de infra (Docker/env) e repositório serão configurados; automações avançadas de deploy ficam para quando o ambiente alvo for definitivamente escolhido.

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
*Last updated: 2026-05-21 after initialization*
