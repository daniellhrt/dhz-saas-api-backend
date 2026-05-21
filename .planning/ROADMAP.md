# Roadmap: DHZ SaaS Infra & Testing

**Status:** IN PROGRESS
**Progress:** 0 / 2 phases complete (0%)
**Total Plans:** 0 executed

## Phase 1: Configuração Segura & Dockerização
**Goal:** Fechar brechas de segurança com segredos hardcoded, organizar arquivos para ignorar lixo/arquivos sensíveis, e deixar a API "containerizada" e documentada para outros desenvolvedores.
**Success Criteria:**
1. Variáveis de ambiente configuradas no código (sem senhas e segredos soltos).
2. `Dockerfile` consegue fazer o build da imagem sem erros de compilação.
3. `compose.yaml` levanta banco de dados e aplicação via imagem Docker.
4. `README.md` documentando passo a passo.

**Requirements:** SEC-01, SEC-02, SEC-03, SEC-04, DOCK-01, DOCK-02, DOCK-03

## Phase 2: Base de Testes e CI Workflow
**Goal:** Implementar infraestrutura de testes automatizados nas três camadas principais (Service, Controller e Repository) e garantir que as execuções aconteçam no GitHub.
**Success Criteria:**
1. Pelo menos um teste de Service roda com sucesso cobrindo lógica (ex: double-booking).
2. Pelo menos um teste de Controller roda usando MockMvc.
3. Testes do repositório validam queries de multi-tenancy localmente com H2.
4. O GitHub Actions finaliza o job como verde ao empurrar código para a branch `main`.

**Requirements:** TEST-01, TEST-02, TEST-03, TEST-04, TEST-05, CICD-01

---
*Generated: 2026-05-21*
