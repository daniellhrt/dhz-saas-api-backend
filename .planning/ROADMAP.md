# Roadmap: DHZ SaaS API Backend

## Milestones

- ✅ **v1.0 Infra & Testing** — Phases 1–2 (shipped 2026-05-22)
- 🚧 **v1.1 Evolução da API** — Phases 3–6 (in progress)

## Phases

<details>
<summary>✅ v1.0 Infra & Testing (Phases 1–2) — SHIPPED 2026-05-22</summary>

- [x] Phase 1: Configuração Segura & Dockerização — completed 2026-05-22
- [x] Phase 2: Base de Testes e CI Workflow — completed 2026-05-22

</details>

### 🚧 v1.1 Evolução da API (In Progress)

- [x] **Phase 3: Registro de Barbeiros** — Criar CRUD completo de barbeiros com endpoint de cadastro
- [x] **Phase 4: Gestão de Status de Agendamentos** — Endpoints para confirmar, cancelar e concluir agendamentos
- [x] **Phase 5: Rate Limiting Distribuído** — Substituir cache local por Redis + Bucket4j
- [ ] **Phase 6: Testes para Client e Catalog** — Cobertura de testes nos domínios faltantes

## Progress

| Phase | Milestone | Requirements | Status |
|-------|-----------|-------------|--------|
| 1. Configuração Segura & Dockerização | v1.0 | SEC/DOCK | Complete |
| 2. Base de Testes e CI Workflow | v1.0 | TEST/CICD | Complete |
| 3. Registro de Barbeiros | v1.1 | REG-01..04 | Complete |
| 4. Gestão de Status de Agendamentos | v1.1 | STATUS-01..03 | Complete |
| 5. Rate Limiting Distribuído | v1.1 | RL-01..03 | Complete |
| 6. Testes para Client e Catalog | v1.1 | TEST-06..09 | Not started |

---

## Detalhamento das Fases

### Phase 3: Registro de Barbeiros
**Goal:** Implementar CRUD completo de barbeiros (hoje só existe login)
**Requirements:** REG-01, REG-02, REG-03, REG-04
**Success Criteria:**
1. POST /api/v1/barbers cria um novo barbeiro com hash de senha
2. GET /api/v1/barbers lista barbeiros do tenant logado
3. PUT /api/v1/barbers/{id} atualiza dados do barbeiro (com validação de tenant)
4. DELETE /api/v1/barbers/{id} remove barbeiro (com validação de tenant)

### Phase 4: Gestão de Status de Agendamentos
**Goal:** Adicionar transições de status nos agendamentos
**Requirements:** STATUS-01, STATUS-02, STATUS-03
**Success Criteria:**
1. PATCH /api/v1/appointments/{id}/confirm altera status para CONFIRMED
2. PATCH /api/v1/appointments/{id}/cancel altera status para CANCELLED
3. PATCH /api/v1/appointments/{id}/complete altera status para COMPLETED

### Phase 5: Rate Limiting Distribuído
**Goal:** Substituir rate limiting em memória por Redis distribuído
**Requirements:** RL-01, RL-02, RL-03
**Success Criteria:**
1. Redis adicionado ao compose.yaml
2. Bucket4j configurado para usar Redis como backend
3. Rate limiting funciona com múltiplas instâncias da API

### Phase 6: Testes para Client e Catalog
**Goal:** Expandir cobertura de testes para todos os domínios
**Requirements:** TEST-06, TEST-07, TEST-08, TEST-09
**Success Criteria:**
1. ClientServiceTest testa criação, listagem e validação de tenant
2. ClientControllerTest testa endpoints via MockMvc
3. CatalogServiceTest testa criação, desativação e listagem
4. CatalogControllerTest testa endpoints via MockMvc

---

*See archived details: `.planning/milestones/v1.0-ROADMAP.md`*
