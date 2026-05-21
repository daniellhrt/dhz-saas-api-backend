# Preocupações — dhz-saas-api-backend

> Mapeado em: 2026-05-21

## Problemas Críticos 🔴

### 1. JWT Secret Hardcoded

**Arquivo:** `src/main/resources/application.yml`

```yaml
app:
  security:
    jwt:
      secret: 413F4428472B4B6250655368566D5970337336763979244226452948404D6351
```

O segredo JWT está hardcoded no arquivo de configuração. Não existe override para perfil de produção. Se este código for para produção, qualquer pessoa com acesso ao repositório pode forjar tokens válidos.

**Correção:** Usar variável de ambiente (`${JWT_SECRET}`) com fallback para dev.

### 2. Zero Cobertura de Testes

Apenas o teste smoke do Spring Initializr existe. Nenhum teste unitário, de integração ou de segurança.

**Risco:** Mudanças podem quebrar funcionalidades sem detecção.

## Problemas Altos 🟠

### 3. Credenciais de DB Hardcoded

**Arquivo:** `compose.yaml`

```yaml
POSTGRES_USER: admin
POSTGRES_PASSWORD: adminpassword
```

Aceitável para dev local, mas sem padrão `.env` para facilitar a transição para produção.

### 4. Sem Pipeline CI/CD

Nenhuma configuração de CI/CD encontrada. Sem build automatizado, sem testes automatizados, sem deploy.

### 5. Sem README.md

Projeto sem documentação de onboarding. Novos desenvolvedores não têm guia de como configurar e rodar o projeto.

## Problemas Médios 🟡

### 6. Sem Checkstyle / EditorConfig

Nenhuma ferramenta de enforcement de estilo de código configurada. Consistência depende de disciplina manual.

### 7. HELP.md Desatualizado

`HELP.md` referencia Spring Boot 4.0.6 mas o `pom.xml` usa 3.2.5. Documentação gerada está inconsistente.

### 8. Bug de Validação no ServiceItemDTO

`@Min(value = 5)` no campo `durationMinutes` mas a mensagem diz "duração mínima é de 15 minutos". Valor e mensagem inconsistentes.

**Arquivo:** `domain/catalog/ServiceItemDTO.java`

### 9. Sem Logging Estruturado

O `GlobalExceptionHandler` tem um comentário placeholder: "Em um cenário real, aqui entraria um log.error". Nenhum framework de logging está configurado além do padrão Spring Boot.

### 10. Verificação de TenantContext Inconsistente

`ClientService` verifica se `tenantId` é null, mas `CatalogService` e `AppointmentService` não fazem a mesma verificação. Pode causar NPE ou vazamento de dados entre tenants.

## Problemas Baixos 🟢

### 11. Sem Configuração CORS

Nenhuma configuração CORS encontrada. Requisições de um frontend em domínio diferente serão bloqueadas pelo browser.

### 12. `.planning/` Não Está no .gitignore

Artefatos do GSD podem ser commitados acidentalmente ao repositório.

### 13. Sem Endpoint de Registro de Barbeiro

Apenas login existe (`POST /api/v1/auth/login`). Não há endpoint para criar novos barbeiros via API. Provavelmente requer seed manual no banco.

### 14. Verificação de Conflito de Agendamento Incompleta

A validação de sobreposição de horários é por tenant mas não por barbeiro. Barbearias com múltiplos barbeiros podem ter falsos conflitos — um horário marcado com barbeiro A bloquearia o mesmo horário para barbeiro B.

## Dívida Técnica

| Item | Impacto | Esforço |
|---|---|---|
| Externalizar JWT secret | Alto | Baixo |
| Adicionar testes | Alto | Alto |
| Configurar CI/CD | Médio | Médio |
| Criar README.md | Baixo | Baixo |
| Corrigir bug validação DTO | Baixo | Baixo |
| Configurar CORS | Médio | Baixo |
| Consistência do TenantContext | Médio | Baixo |

---
*Mapeado: 2026-05-21 via gsd-map-codebase*
