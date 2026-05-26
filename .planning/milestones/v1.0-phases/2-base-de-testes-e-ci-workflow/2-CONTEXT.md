# Phase 2 Context: Base de Testes e CI Workflow

## Domain
Implementação de infraestrutura de testes automatizados abrangendo camadas de Service, Controller e Repository, e automação da execução (Integração Contínua) via GitHub Actions.

## Locked Requirements (from SPEC/REQUIREMENTS)
- **TEST-01**: Configurar testes unitários para Services validando as regras de negócio.
- **TEST-02**: Configurar testes de integração para Controllers via MockMvc.
- **TEST-03**: Configurar testes de Repository isolados com banco em memória (H2).
- **TEST-04**: Testar explicitamente a prevenção de double-booking no AppointmentService.
- **TEST-05**: Testar explicitamente o isolamento de tenant no SecurityFilter e TenantContext.
- **CICD-01**: Configurar GitHub Actions workflow para fazer build e rodar testes automaticamente em cada Push/PR.

## Canonical References
- `.planning/codebase/TESTING.md`
- `.planning/codebase/CONCERNS.md`

## Decisions Captured
### Estratégia de Banco de Dados para Testes
- **Banco H2 em Memória**: Em vez de configurar Testcontainers com Postgres (o que alongaria o build tempo), usaremos H2 configurado com sintaxe compatível com PostgreSQL (`MODE=PostgreSQL`). O Flyway já está executando migrações em H2 sem problemas na suite default.

### Mocking vs Context Loading
- **Services (TEST-01, TEST-04)**: Usarão testes verdadeiramente unitários sem subir o contexto do Spring (`@ExtendWith(MockitoExtension.class)`), mockando os Repositories para execução extremamente rápida.
- **Controllers (TEST-02)**: Usarão `@WebMvcTest` com `MockMvc` para não inicializar a camada JPA. O `SecurityFilter` e dependências de segurança devem ser mockados ou injetados como `@MockBean`.
- **Repositories (TEST-03)**: Usarão `@DataJpaTest`, rodando contra o H2.

### GitHub Actions
- **Workflow**: Um único job `build` em `ubuntu-latest`.
- **Estratégia de Cache**: Fazer o setup do Java (v21) e usar `cache: 'maven'` para agilizar execuções futuras.
- **Variáveis de Ambiente**: Nenhuma variável externa de banco ou JWT é necessária para CI pois os testes usarão o H2 e um segredo injetado nas anotações de teste.
