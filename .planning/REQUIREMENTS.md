# Requirements: DHZ SaaS API Backend — v1.1

**Defined:** 2026-05-22
**Core Value:** Isolamento seguro de dados entre barbearias (multi-tenancy) e estabilidade da API

## v1.1 Requirements

### Registro de Barbeiros

- [x] **REG-01**: Barbeiro pode criar conta com nome, email e senha
- [x] **REG-02**: Barbeiro pode listar todos os barbeiros da sua barbearia (tenant)
- [x] **REG-03**: Barbeiro pode atualizar seus próprios dados
- [x] **REG-04**: Barbeiro pode deletar sua conta

### Gestão de Agendamentos

- [x] **STATUS-01**: Barbeiro pode confirmar um agendamento pendente
- [x] **STATUS-02**: Barbeiro pode cancelar um agendamento (com motivo opcional)
- [x] **STATUS-03**: Barbeiro pode concluir um agendamento

### Rate Limiting Distribuído

- [x] **RL-01**: Adicionar dependência do Redis (Bucket4j Redis + Lettuce)
- [x] **RL-02**: Substituir ConcurrentHashMap por bucket distribuído no Redis com fallback em memória
- [x] **RL-03**: Adicionar Redis ao compose.yaml

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
| REG-01 | 3 | Done |
| REG-02 | 3 | Done |
| REG-03 | 3 | Done |
| REG-04 | 3 | Done |
| STATUS-01 | 4 | Done |
| STATUS-02 | 4 | Done |
| STATUS-03 | 4 | Done |
| RL-01 | 5 | Done |
| RL-02 | 5 | Done |
| RL-03 | 5 | Done |
| TEST-06 | - | Pending |
| TEST-07 | - | Pending |
| TEST-08 | - | Pending |
| TEST-09 | - | Pending |

**Coverage:**
- v1.1 requirements: 14 total
- Mapped to phases: 7
- Unmapped: 7 ⚠️

---
*Requirements defined: 2026-05-22*
*Last updated: 2026-05-22 after v1.1 milestone start*
