# 🎯 SPRINT 1: TESTES COMPLETOS (70%+ Cobertura)

**Status:** INICIANDO  
**Duração:** 2 semanas (80 horas estimadas)  
**Objetivo:** Atingir 70%+ cobertura de código nos Services críticos  
**Saída:** 75+ testes rodando, relatório Jacoco gerado

---

## 📋 TAREFAS SPRINT 1

### Tarefa 1.1: Testes Unitários BarberService (12 horas)

**Objetivo:** Validar criação, atualização, deleção, isolamento por tenant

**Testes a implementar:**
```
✅ 1. shouldRegisterAdminSuccessfully()
✅ 2. shouldThrowExceptionWhenAdminEmailDuplicated()
✅ 3. shouldCreateBarbearUserSuccessfully()
✅ 4. shouldThrowExceptionWhenBarbearEmailDuplicated()
✅ 5. shouldThrowExceptionWhenNonAdminTriesToCreateBarbear()
✅ 6. shouldUpdateBarbearNameSuccessfully()
✅ 7. shouldThrowExceptionWhenUpdatingToExistingEmail()
✅ 8. shouldThrowExceptionWhenBarbearTriesToUpdateOthers()
✅ 9. shouldDeleteBarbearSuccessfully()
✅ 10. shouldThrowExceptionWhenAdminTriesToDeleteSelf()
✅ 11. shouldThrowExceptionWhenNonAdminTriesToDelete()
✅ 12. shouldListAllBarbearsForTenant()
```

**Arquivo:** `src/test/java/br/com/dht/apibackend/domain/barber/BarberServiceTest.java`

---

### Tarefa 1.2: Testes Unitários ClientService (10 horas)

**Objetivo:** Validar CRUD de clientes, e-mail único por tenant

**Testes a implementar:**
```
✅ 1. shouldCreateClientSuccessfully()
✅ 2. shouldThrowExceptionWhenEmailDuplicated()
✅ 3. shouldThrowExceptionWhenTenantContextNull()
✅ 4. shouldUpdateClientSuccessfully()
✅ 5. shouldThrowExceptionWhenUpdatingToExistingEmail()
✅ 6. shouldListAllClientsWithPagination()
✅ 7. shouldThrowExceptionWhenClientNotFound()
✅ 8. shouldIsolateTenantData()
✅ 9. shouldValidatePhoneFormat()
✅ 10. shouldAllowOptionalFields()
```

**Arquivo:** `src/test/java/br/com/dht/apibackend/domain/client/ClientServiceTest.java`
(Expandir os 4 testes existentes)

---

### Tarefa 1.3: Testes Unitários CatalogService (10 horas)

**Objetivo:** Validar catálogo (criar, atualizar, soft delete, listagem)

**Testes a implementar:**
```
✅ 1. shouldCreateServiceSuccessfully()
✅ 2. shouldThrowExceptionWhenServiceNameDuplicated()
✅ 3. shouldUpdateServiceSuccessfully()
✅ 4. shouldThrowExceptionWhenUpdatingToExistingName()
✅ 5. shouldThrowExceptionWhenServiceNotFound()
✅ 6. shouldListActiveServicesOnly()
✅ 7. shouldDeactivateServiceWithoutDeleting()
✅ 8. shouldThrowExceptionWhenPriceIsZero()
✅ 9. shouldThrowExceptionWhenDurationLessThan15()
✅ 10. shouldIsolateTenantCatalog()
```

**Arquivo:** `src/test/java/br/com/dht/apibackend/domain/catalog/CatalogServiceTest.java`

---

### Tarefa 1.4: Testes Unitários AppointmentService (15 horas)

**Objetivo:** Expandir dos 13 para 25+ testes

**Novos testes a adicionar:**
```
✅ 1. shouldThrowExceptionWhenServiceItemNotFound()
✅ 2. shouldCalculateEndTimeBasedOnServiceDuration()
✅ 3. shouldRejectOverlappingAppointments()
✅ 4. shouldIgnoreCanceledAppointmentsInOverlapCheck()
✅ 5. shouldHandleMultipleConcurrentAppointments()
✅ 6. shouldThrowExceptionWhenConfirmingAlreadyConfirmed()
✅ 7. shouldThrowExceptionWhenCancelCompletedAppointment()
✅ 8. shouldAllowCancelPendingOrConfirmed()
✅ 9. shouldIsolateAppointmentsByTenant()
✅ 10. shouldValidateStartTimeNotNull()
✅ 11. shouldHandleBlockScheduleRequests()
✅ 12. shouldListAppointmentsWithPagination()
```

**Arquivo:** `src/test/java/br/com/dht/apibackend/domain/appointment/AppointmentServiceTest.java`
(Expandir dos 13 existentes)

---

### Tarefa 1.5: Testes de Integração (15 horas)

**Objetivo:** Testes end-to-end com TestContainers + banco real

**1. BarberIntegrationTest (3 horas)**
```java
@SpringBootTest
@Testcontainers
class BarberIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
  
  ✅ Teste: Registrar ADMIN → Criar USER → Listar → Deletar
  ✅ Teste: Validar isolamento por tenant
  ✅ Teste: Validar transações (rollback em erro)
}
```

**2. ClientIntegrationTest (3 horas)**
```java
✅ Teste: Criar cliente → Atualizar → Listar → Deletar
✅ Teste: Validar e-mail único por tenant
✅ Teste: Paginação funcionando
```

**3. CatalogIntegrationTest (3 horas)**
```java
✅ Teste: Criar serviço → Listar → Deactivate → Listar (filtrado)
✅ Teste: Validar soft delete (NOT DELETE)
```

**4. AppointmentIntegrationTest (3 horas)**
```java
✅ Teste: Agendar → Confirmar → Completar (fluxo completo)
✅ Teste: Double-booking prevention
✅ Teste: Estado máquina funcionando
```

**5. MultiTenantIntegrationTest (3 horas)**
```java
✅ Teste: Tenant A cria barbeiro, Tenant B não vê
✅ Teste: Queries isoladas por tenant
```

**Arquivo:** `src/test/java/br/com/dht/apibackend/domain/*/...IntegrationTest.java`

---

### Tarefa 1.6: Testes de Segurança (12 horas)

**Objetivo:** Autenticação, autorização, rate limiting

**1. SecurityFilterTest (3 horas)**
```java
✅ Teste: Token válido → SecurityContext configurado
✅ Teste: Token inválido → 401 Unauthorized
✅ Teste: TenantContext limpo após request (evita vazamento)
✅ Teste: JWT com claims corretos (sub, tenantId, role)
```

**2. AuthorizationTest (3 horas)**
```java
✅ Teste: USER NÃO pode criar barbeiro (ADMIN-only)
✅ Teste: USER pode atualizar seu próprio perfil
✅ Teste: USER NÃO pode deletar barbeiros
✅ Teste: Barbeiro de outro tenant não vê dados
```

**3. RateLimitingTest (3 horas)**
```java
✅ Teste: 5 logins/min OK
✅ Teste: 6º login retorna 429 (Too Many Requests)
✅ Teste: Rate limit resetar após 1 min
✅ Teste: Por IP (não por usuário)
```

**4. InputValidationTest (3 horas)**
```java
✅ Teste: @NotBlank rejeita vazio
✅ Teste: @Email rejeita e-mail inválido
✅ Teste: @Pattern phone rejeita formato inválido
✅ Teste: @Min/DecimalMin rejeita valores pequenos
✅ Teste: GlobalExceptionHandler converte para 400
```

**Arquivo:** `src/test/java/br/com/dht/apibackend/security/...Test.java`

---

### Tarefa 1.7: Testes de Controllers (10 horas)

**Objetivo:** HTTP status corretos, JSON responses

**1. BarberControllerTest (3 horas)**
```java
✅ POST /api/v1/barbers → 201 Created
✅ POST /api/v1/auth/register → 201 Created
✅ GET /api/v1/barbers → 200 OK + Page
✅ PATCH /api/v1/barbers/{id} → 200 OK
✅ DELETE /api/v1/barbers/{id} → 204 No Content
✅ Sem autenticação → 401
```

**2. ClientControllerTest (2 horas)**
```java
✅ POST /api/v1/clients → 201 Created
✅ GET /api/v1/clients → 200 OK + Page
✅ PATCH /api/v1/clients/{id} → 200 OK
```

**3. CatalogControllerTest (2 horas)**
```java
✅ POST /api/v1/catalog → 201 Created
✅ GET /api/v1/catalog → 200 OK (ativos)
✅ DELETE /api/v1/catalog/{id} → 204 (soft delete)
```

**4. AppointmentControllerTest (3 horas)**
```java
✅ POST /api/v1/appointments → 201 Created
✅ PATCH /api/v1/appointments/{id}/confirm → 200 OK
✅ PATCH /api/v1/appointments/{id}/cancel → 200 OK
✅ GET /api/v1/appointments → 200 OK + Page
```

**Arquivo:** Expandir `ClientControllerTest.java` existente

---

### Tarefa 1.8: Cobertura com Jacoco (5 horas)

**Objetivo:** Gerar relatório, validar 70%+

**Passos:**
```bash
✅ 1. Rodar todos testes
   .\mvnw.cmd clean test

✅ 2. Gerar relatório Jacoco
   .\mvnw.cmd jacoco:report
   
✅ 3. Abrir relatório
   target/site/jacoco/index.html
   
✅ 4. Validar métodos < 70% cobertos
   ✅ Idealmente Services: >80%
   ✅ Controllers: >70%
   ✅ Repositories: >60% (queries SQL difíceis testar)
   
✅ 5. Adicionar testes para fechar gaps
```

---

## 🎯 RESUMO DE TESTES (75+ total)

| Tipo | Qtd | Total |
|------|:---:|:-----:|
| Testes Unitários AppointmentService | 25+ | 25 |
| Testes Unitários BarberService | 12 | 12 |
| Testes Unitários ClientService | 10 | 10 |
| Testes Unitários CatalogService | 10 | 10 |
| Testes de Integração | 15 | 15 |
| Testes de Segurança | 12 | 12 |
| Testes de Controller (expandir) | 10 | 10 |
| **TOTAL** | | **94 testes** |

---

## 📊 COBERTURA ESPERADA

| Module | Current | Target | Gap |
|--------|:-------:|:------:|:---:|
| Services | ~20% | 80% | 60% |
| Controllers | ~30% | 70% | 40% |
| DTOs | N/A | 100% | — |
| Repositories | ~10% | 60% | 50% |
| **Geral** | ~20% | 70% | 50% |

---

## 📝 ARQUIVOS A CRIAR/MODIFICAR

### Novos (8 arquivos):
```
src/test/java/br/com/dht/apibackend/domain/barber/BarberServiceTest.java
src/test/java/br/com/dht/apibackend/domain/catalog/CatalogServiceTest.java
src/test/java/br/com/dht/apibackend/domain/appointment/AppointmentIntegrationTest.java
src/test/java/br/com/dht/apibackend/domain/barber/BarberIntegrationTest.java
src/test/java/br/com/dht/apibackend/domain/client/ClientIntegrationTest.java
src/test/java/br/com/dht/apibackend/domain/catalog/CatalogIntegrationTest.java
src/test/java/br/com/dht/apibackend/security/SecurityFilterTest.java
src/test/java/br/com/dht/apibackend/security/AuthorizationTest.java
```

### Modificar (3 arquivos):
```
src/test/java/br/com/dht/apibackend/domain/client/ClientServiceTest.java (expandir)
src/test/java/br/com/dht/apibackend/domain/appointment/AppointmentServiceTest.java (expandir)
src/test/java/br/com/dht/apibackend/domain/client/ClientControllerTest.java (expandir)
```

---

## ⏱️ TIMELINE SPRINT 1

| Semana | Tarefa | Horas | Status |
|--------|--------|:-----:|:------:|
| Semana 1 | 1.1 + 1.2 + 1.3 | 32h | ▶️ COMEÇAR |
| Semana 1 | 1.4 + 1.5 (parte 1) | 20h | ▶️ COMEÇAR |
| Semana 1/2 | 1.5 (parte 2) + 1.6 | 27h | PRÓXIMO |
| Semana 2 | 1.7 + 1.8 | 15h | PRÓXIMO |
| Semana 2 | Ajustes/Bugs | 6h | PRÓXIMO |

**Total: 80 horas (2 semanas, 1 dev fulltime)**

---

## ✅ CRITÉRIOS DE SUCESSO

- [x] 94+ testes implementados
- [x] Todos testes passando (`.\mvnw.cmd clean test`)
- [x] Cobertura mínima 70% (`.\mvnw.cmd jacoco:report`)
- [x] Services > 80% cobertura
- [x] Controllers > 70% cobertura
- [x] Zero falsos positivos (testes validam regras negócio reais)
- [x] TenantContext isolado em testes (setup/teardown)
- [x] TestContainers funcionando (testes de integração)
- [x] Segurança testada (401, 403, rate limit)

---

## 🚀 PRÓXIMO PASSO

**Tarefa 1.1 — Criar BarberServiceTest.java com 12 testes**

Quer começar agora?

---

`Sprint 1 iniciado: 25 maio 2026`

