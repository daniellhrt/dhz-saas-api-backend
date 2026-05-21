# Testes — dhz-saas-api-backend

> Mapeado em: 2026-05-21

## Estado Atual

🔴 **Cobertura de testes: praticamente zero.**

### Arquivo Único de Teste

`src/test/java/br/com/dht/apibackend/DhzSaasApiBackendApplicationTests.java`

```java
@SpringBootTest
class DhzSaasApiBackendApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

Este é o teste smoke gerado automaticamente pelo Spring Initializr. Verifica apenas que o contexto Spring carrega sem erros.

## Framework de Testes Disponível

| Dependência | Versão | Escopo |
|---|---|---|
| `spring-boot-starter-test` | BOM (3.2.5) | test |
| `spring-security-test` | BOM | test |

**Inclui:** JUnit 5, Mockito, AssertJ, Spring Test, MockMvc, `@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`

## O Que Não Existe

- ❌ Testes unitários para Services
- ❌ Testes unitários para Controllers (MockMvc)
- ❌ Testes de integração para Repositories
- ❌ Testes de segurança (autenticação JWT)
- ❌ Testes de validação de DTOs
- ❌ Testes de multi-tenancy (isolamento de dados)
- ❌ Testes de conflito de agendamento
- ❌ CI/CD pipeline para rodar testes
- ❌ Configuração de cobertura (JaCoCo, etc.)

## Recomendações Prioritárias

1. **Testes de Service** — Lógica de negócio (agendamentos, conflitos, validações de tenant)
2. **Testes de Controller** — MockMvc para endpoints REST, validação de DTOs
3. **Testes de Segurança** — JWT validation, acesso não autorizado, isolamento de tenant
4. **Testes de Repository** — Queries customizadas com `@DataJpaTest` + H2

---
*Mapeado: 2026-05-21 via gsd-map-codebase*
