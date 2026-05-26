# Milestone v1.2 Requirements

### Infraestrutura Backend
- [ ] **DEPLOY-01**: Configurar provedor Cloud (ex: Railway/Render) para a API Spring Boot
- [ ] **DEPLOY-02**: Configurar banco de dados PostgreSQL em produção
- [ ] **DEPLOY-03**: Configurar Redis em produção
- [ ] **DEPLOY-04**: Ajustar variáveis de ambiente de produção (JWT Secret forte, DB URL, Redis URL)
- [ ] **DEPLOY-05**: Configurar CORS no backend para aceitar requisições do frontend de produção

### Infraestrutura Frontend
- [ ] **FRONT-01**: Configurar variáveis de ambiente do Frontend para apontar para a API de produção
- [ ] **FRONT-02**: Realizar o deploy do frontend (ex: Vercel ou Netlify)

### Validação End-to-End
- [ ] **E2E-01**: Garantir login e operações via frontend (celular) comunicando-se perfeitamente com a API em produção

## Future Requirements
- CI/CD automatizado via GitHub Actions para produção
- Monitoramento, métricas (Actuator/Prometheus) e Logs centralizados

## Out of Scope
- Mudanças nas regras de negócio (Foco exclusivo em infraestrutura e deploy)
- Novas integrações de terceiros (Gateways de pagamento, envio de emails, etc.)

## Traceability
| Requirement | Phase |
|-------------|-------|
| DEPLOY-01 | 7 |
| DEPLOY-02 | 7 |
| DEPLOY-03 | 7 |
| DEPLOY-04 | 7 |
| DEPLOY-05 | 7 |
| FRONT-01 | 8 |
| FRONT-02 | 8 |
| E2E-01 | 8 |
