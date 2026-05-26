# Roadmap: DHZ SaaS API Backend

## Milestones

- ✅ **v1.0 Infra & Testing** — Phases 1–2 (shipped 2026-05-22)
- ✅ **v1.1 Evolução da API** — Phases 3–6 (shipped 2026-05-26)
- 🚧 **v1.2 Deploy MVP e Homologação** — Phases 7–8 (in progress)

## Phases

<details>
<summary>✅ v1.0 Infra & Testing (Phases 1–2) — SHIPPED 2026-05-22</summary>

- [x] Phase 1: Configuração Segura & Dockerização — completed 2026-05-22
- [x] Phase 2: Base de Testes e CI Workflow — completed 2026-05-22

</details>

<details>
<summary>✅ v1.1 Evolução da API (Phases 3–6) — SHIPPED 2026-05-26</summary>

- [x] Phase 3: Registro de Barbeiros — completed 2026-05-26
- [x] Phase 4: Gestão de Status de Agendamentos — completed 2026-05-26
- [x] Phase 5: Rate Limiting Distribuído — completed 2026-05-26
- [x] Phase 6: Testes para Client e Catalog — completed 2026-05-26

</details>

### 🚧 v1.2 Deploy MVP (In Progress)

- [ ] **Phase 7: Backend Deploy & Cloud Config** — Hospedar API, Postgres e Redis na nuvem
- [ ] **Phase 8: Frontend Deploy & Integração** — Hospedar frontend no Vercel e conectar à API de produção

## Progress

| Phase | Milestone | Requirements | Status |
|-------|-----------|-------------|--------|
| 1. Configuração Segura & Dockerização | v1.0 | SEC/DOCK | Complete |
| 2. Base de Testes e CI Workflow | v1.0 | TEST/CICD | Complete |
| 3. Registro de Barbeiros | v1.1 | REG-01..04 | Complete |
| 4. Gestão de Status de Agendamentos | v1.1 | STATUS-01..03 | Complete |
| 5. Rate Limiting Distribuído | v1.1 | RL-01..03 | Complete |
| 6. Testes para Client e Catalog | v1.1 | TEST-06..09 | Complete |
| 7. Backend Deploy & Cloud Config | v1.2 | DEPLOY-01..05 | Pending |
| 8. Frontend Deploy & Integração | v1.2 | FRONT-01..02, E2E-01 | Pending |

---

## Detalhamento das Fases

### Phase 7: Backend Deploy & Cloud Config
**Goal:** Hospedar a API de forma acessível na internet junto ao banco de dados e Redis.
**Requirements:** DEPLOY-01, DEPLOY-02, DEPLOY-03, DEPLOY-04, DEPLOY-05
**Success Criteria:**
1. A API retorna 200 OK na URL pública da nuvem.
2. Banco de Dados e Redis operacionais e acessíveis pela API.
3. CORS configurado corretamente para o domínio do frontend (ou liberado em homologação).

### Phase 8: Frontend Deploy & Integração
**Goal:** Hospedar o Frontend e permitir o uso real via celular (integração E2E).
**Requirements:** FRONT-01, FRONT-02, E2E-01
**Success Criteria:**
1. Frontend acessível publicamente via Vercel/Netlify.
2. Login pelo frontend de produção funciona e persiste a sessão corretamente.
3. As operações na API através do frontend mobile ocorrem sem erros de CORS ou conexão.

---

*See archived details: `.planning/milestones/v1.0-ROADMAP.md` e `.planning/milestones/v1.1-ROADMAP.md`*
