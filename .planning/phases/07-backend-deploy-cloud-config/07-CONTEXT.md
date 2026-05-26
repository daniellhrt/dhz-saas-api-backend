# Phase 7 Context

**Domain:** Hospedar a API de forma acessível na internet junto ao banco de dados e Redis.

## Decisions

### Infraestrutura Backend
- **Provedor Cloud:** Railway. A API Spring Boot será hospedada no Railway usando o GitHub integration.
- **Banco de Dados & Redis:** Managed resources do Railway. O PostgreSQL e o Redis serão provisionados dentro do próprio projeto no Railway por simplicidade.

### Segurança e Integração
- **Configuração de CORS:** Restringir para o domínio do frontend hospedado na Vercel (aceitar URLs geradas pela Vercel `*.vercel.app` ou a URL base específica do projeto). O frontend já está deployado no Vercel.

## Canonical Refs
- Nenhuma referência externa adicional especificada além da documentação padrão do Railway e Spring Boot.

## Deferred Ideas
- Nenhum item deferido.
