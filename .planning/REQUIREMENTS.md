# Requirements: DHZ SaaS Infra & Testing

**Defined:** 2026-05-21
**Core Value:** Isolamento seguro de dados entre barbearias (multi-tenancy) e estabilidade da API, garantindo que as operações essenciais ocorram de forma previsível.

## v1 Requirements

### Testes (Qualidade)
- [ ] **TEST-01**: Configurar testes unitários para Services validando as regras de negócio.
- [ ] **TEST-02**: Configurar testes de integração para Controllers via MockMvc.
- [ ] **TEST-03**: Configurar testes de Repository isolados com banco em memória (H2).
- [ ] **TEST-04**: Testar explicitamente a prevenção de double-booking no AppointmentService.
- [ ] **TEST-05**: Testar explicitamente o isolamento de tenant no SecurityFilter e TenantContext.

### Segurança e Configuração (Infra)
- [ ] **SEC-01**: Remover segredo JWT hardcoded e injetar via variável de ambiente.
- [ ] **SEC-02**: Remover senhas do banco hardcoded e usar variáveis de ambiente.
- [ ] **SEC-03**: Atualizar `.gitignore` para não expor arquivos `.env` nem a pasta `.planning/`.
- [ ] **SEC-04**: Criar arquivo `application-prod.yml` para override de configurações de produção.

### Deploy e Docker (Infra)
- [ ] **DOCK-01**: Criar `Dockerfile` multi-stage otimizado para a API.
- [ ] **DOCK-02**: Ajustar `compose.yaml` (ou criar `docker-compose.yml`) para simular o ambiente usando o Dockerfile.
- [ ] **DOCK-03**: Escrever um `README.md` com instruções claras para rodar o projeto localmente.

### CI/CD
- [ ] **CICD-01**: Configurar GitHub Actions workflow para fazer build e rodar os testes automaticamente em cada Push/PR.

## v2 Requirements
(Nenhum no momento)

## Out of Scope

| Feature | Reason |
|---------|--------|
| Novas funcionalidades de negócio | Foco atual é estabilizar e criar redes de segurança |
| Deploy contínuo (CD) automatizado | O ambiente alvo final ainda não está provisionado |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| TEST-01 | Phase 2 | Pending |
| TEST-02 | Phase 2 | Pending |
| TEST-03 | Phase 2 | Pending |
| TEST-04 | Phase 2 | Pending |
| TEST-05 | Phase 2 | Pending |
| SEC-01 | Phase 1 | Pending |
| SEC-02 | Phase 1 | Pending |
| SEC-03 | Phase 1 | Pending |
| SEC-04 | Phase 1 | Pending |
| DOCK-01 | Phase 1 | Pending |
| DOCK-02 | Phase 1 | Pending |
| DOCK-03 | Phase 1 | Pending |
| CICD-01 | Phase 2 | Pending |

**Coverage:**
- v1 requirements: 13 total
- Mapped to phases: 13
- Unmapped: 0 ✓

---
*Requirements defined: 2026-05-21*
*Last updated: 2026-05-21 after initialization*
