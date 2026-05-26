# Phase 1 Context: Configuração Segura & Dockerização

## Domain
Implementação de práticas de segurança para externalização de segredos e infraestrutura local com Docker e Docker Compose, estabelecendo uma base sólida para deploy e desenvolvimento.

## Locked Requirements (from SPEC/REQUIREMENTS)
- **SEC-01**: Remover segredo JWT hardcoded e injetar via variável de ambiente.
- **SEC-02**: Remover senhas do banco hardcoded e usar variáveis de ambiente.
- **SEC-03**: Atualizar `.gitignore` para não expor arquivos `.env` nem a pasta `.planning/`.
- **SEC-04**: Criar arquivo `application-prod.yml` para override de configurações de produção.
- **DOCK-01**: Criar `Dockerfile` multi-stage otimizado para a API.
- **DOCK-02**: Ajustar `compose.yaml` (ou criar `docker-compose.yml`) para simular o ambiente usando o Dockerfile.
- **DOCK-03**: Escrever um `README.md` com instruções claras para rodar o projeto localmente.

## Canonical References
- `.planning/codebase/STACK.md`
- `.planning/codebase/CONCERNS.md`
- `.planning/codebase/ARCHITECTURE.md`

## Decisions Captured
### Gestão de Segredos e Ambiente
- **Estratégia de Variáveis**: O arquivo `application.yml` raiz e o `compose.yaml` devem ler de variáveis de ambiente. Usaremos arquivos `.env` ignorados no git para desenvolvimento local.
- **Perfis Spring**: 
  - `dev`: focado na experiência local do desenvolvedor usando Docker Compose.
  - `prod`: configurado via `application-prod.yml`, focado em segurança, onde todas as credenciais são estritamente obrigatórias.
- **Conteúdo Ignorado**: `.env`, `.env.local` e `.planning/` adicionados rigorosamente ao `.gitignore`.

### Dockerização
- **Base Image**: 
  - Build stage: `eclipse-temurin:21-jdk` (usando o Maven wrapper local).
  - Run stage: `eclipse-temurin:21-jre-alpine` para garantir uma imagem leve e menor superfície de ataque.
- **Orquestração Compose**: O `compose.yaml` atual será adaptado para poder iniciar tanto o Postgres quanto a imagem buildada da aplicação, referenciando o `.env`.

### Documentação
- **README.md**: Incluirá a stack, requisitos prévios (Docker), como configurar o `.env`, comandos para iniciar os containers, e exemplos de login/requisição.
