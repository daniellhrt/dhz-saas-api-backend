# Requirements: DHZ SaaS API Backend — v1.1

**Defined:** 2026-05-22
**Core Value:** Isolamento seguro de dados entre barbearias (multi-tenancy) e estabilidade da API

## v1.1 Requirements

### Registro de Barbeiros

- [ ] **REG-01**: Barbeiro pode criar conta com nome, email e senha
- [ ] **REG-02**: Barbeiro pode listar todos os barbeiros da sua barbearia (tenant)
- [ ] **REG-03**: Barbeiro pode atualizar seus próprios dados
- [ ] **REG-04**: Barbeiro pode deletar sua conta

### Gestão de Agendamentos

- [ ] **STATUS-01**: Barbeiro pode confirmar um agendamento pendente
- [ ] **STATUS-02**: Barbeiro pode cancelar um agendamento (com motivo opcional)
- [ ] **STATUS-03**: Barbeiro pode concluir um agendamento

### Rate Limiting Distribuído

- [ ] **RL-01**: Adicionar dependência do Redis (Spring Data Redis + Bucket4j Redis)
- [ ] **RL-02**: Substituir ConcurrentHashMap por bucket distribuído no Redis
- [ ] **RL-03**: Adicionar Redis ao compose.yaml

### Testes

- [ ] **TEST-06**: Testes unitários para ClientService (Mockito)
- [ ] **TEST-07**: Testes de integração para ClientController (MockMvc)
- [ ] **TEST-08**: Testes unitários para CatalogService (Mockito)
- [ ] **TEST-09**: Testes de integração para CatalogController (MockMvc)

## v2 Requirements (Deferred)

- Notificações em tempo real — alta complexidade, postergado

## Out of Scope

| Feature | Reason |
|---------|--------|
| Deploy contínuo (CD) | O ambiente alvo final ainda não está provisionado |
| Novos domínios de negócio | Foco em evoluir funcionalidades existentes |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| REG-01 | - | Pending |
| REG-02 | - | Pending |
| REG-03 | - | Pending |
| REG-04 | - | Pending |
| STATUS-01 | - | Pending |
| STATUS-02 | - | Pending |
| STATUS-03 | - | Pending |
| RL-01 | - | Pending |
| RL-02 | - | Pending |
| RL-03 | - | Pending |
| TEST-06 | - | Pending |
| TEST-07 | - | Pending |
| TEST-08 | - | Pending |
| TEST-09 | - | Pending |

**Coverage:**
- v1.1 requirements: 14 total
- Mapped to phases: 0
- Unmapped: 14 ⚠️

---
*Requirements defined: 2026-05-22*
*Last updated: 2026-05-22 after v1.1 milestone start*
