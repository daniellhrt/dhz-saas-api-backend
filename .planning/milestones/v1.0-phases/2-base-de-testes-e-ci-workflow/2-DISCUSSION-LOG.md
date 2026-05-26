# Discussion Log: Phase 2 - Base de Testes e CI Workflow

*Decisões tomadas automaticamente no modo YOLO/Auto com base em melhores práticas.*

## Areas Discussed

### Escolha de Banco para Testes de Integração
- **Option Selected:** H2 in-memory mode com PostgreSQL compatibility.
- **Notes:** Configurar testcontainers traria fidelidade total, mas para este estágio inicial o H2 em memória permite rodar toda a suite em segundos, o que encoraja execuções frequentes.

### Escopo e Mocking dos Testes de Segurança (Tenant)
- **Option Selected:** Testar o `SecurityFilter` e `TenantContext` injetando mocks da requisição HTTP (`MockHttpServletRequest`).
- **Notes:** É mais fácil e unitário do que tentar forjar um JWT completo toda hora. Mockar o `TokenService` ou simular chamadas diretas garante que o tenant extraído vá corretamente para o `TenantContext`.

## Deferred Ideas
- Configuração de CD (Continuous Deployment) automático na nuvem (adiado pois a plataforma alvo ainda não foi escolhida).
