# 🚀 SPRINT 1 — TESTES COMPLETOS (70%+ Cobertura)

**Status:** ✅ INICIADA  
**Data:** 25 maio 2026  
**Objetivo:** 94+ testes rodando, 70%+ cobertura de código

---

## ✅ O QUE FOI FEITO

### Sprint 0 Finalizada
- ✅ 4 Services com logging estruturado (@Slf4j)
- ✅ 5 headers HTTP de segurança (OWASP)
- ✅ DTOs com validações rigorosas
- ✅ 29 testes existentes (AppointmentService 13 + ClientService 4 + BarberService 8 + Controller 4)
- ✅ Build SUCESSO
- ✅ @Future removido (agendamentos no passado permitidos)

---

## ✅ O QUE ACABOU DE SER FEITO (Sprint 1 — Tarefa 1.1)

### BarberServiceTest Expandido para 12 Testes

**Adicionados 4 novos testes:**
```java
✅ shouldThrowException_WhenBarberNotFound()
   → Valida que atualização falha se barbeiro não existe

✅ shouldThrowException_WhenUpdatingToExistingEmail()
   → Valida que não pode mudar para e-mail de outro

✅ shouldThrowException_WhenCreateBarber_EmailDuplicate()
   → Valida que criar com e-mail duplicado falha

✅ shouldIsolateBarbersByTenant()
   → Valida isolamento: barbeiro de outro tenant não vê dados
```

**Total BarberServiceTest:** 12 testes ✅

**Arquivo:** `src/test/java/br/com/dht/apibackend/domain/barber/BarberServiceTest.java`

---

## 📊 COBERTURA ATUAL

| Classe | Testes Antes | Testes Depois | Status |
|--------|:----------:|:------------:|:------:|
| AppointmentServiceTest | 13 | → 13 (expandir) | ▶️ PRÓXIMO |
| BarberServiceTest | 8 | ✅ 12 | ✅ COMPLETO |
| ClientServiceTest | 4 | → 10 (expandir) | ▶️ PRÓXIMO |
| CatalogServiceTest | 0 | → 10 (criar) | ▶️ PRÓXIMO |
| Integração | 0 | → 15 (criar) | ▶️ PRÓXIMO |
| Segurança | 0 | → 12 (criar) | ▶️ PRÓXIMO |
| Controllers | 7 | → 10 (expandir) | ▶️ PRÓXIMO |

**Total Sprint 0:** 29 testes  
**Total Sprint 1 (target):** 94+ testes  
**Gap:** 65+ novos testes

---

## 📝 TAREFAS RESTANTES SPRINT 1

### Tarefa 1.2: Expandir ClientServiceTest (10 horas)
**Target:** 10 testes (expandir dos 4 existentes)
- ✅ shouldCreateClientSuccessfully() — existente
- ✅ shouldThrowExceptionWhenTenantContextNull() — existente
- ✅ shouldThrowExceptionWhenEmailAlreadyExists() — existente
- ✅ shouldListAllClients() — existente
- ➕ shouldUpdateClientSuccessfully()
- ➕ shouldThrowExceptionWhenUpdatingToExistingEmail()
- ➕ shouldThrowExceptionWhenClientNotFound()
- ➕ shouldThrowExceptionWhenPhoneFormatInvalid()
- ➕ shouldAllowOptionalFields()
- ➕ shouldIsolateClientsByTenant()

**Arquivo:** `src/test/java/br/com/dht/apibackend/domain/client/ClientServiceTest.java`

---

### Tarefa 1.3: Criar CatalogServiceTest (10 horas)
**Target:** 10 testes (novo arquivo)
- ✅ shouldCreateServiceSuccessfully()
- ✅ shouldThrowExceptionWhenServiceNameDuplicated()
- ✅ shouldUpdateServiceSuccessfully()
- ✅ shouldThrowExceptionWhenUpdatingToExistingName()
- ✅ shouldThrowExceptionWhenServiceNotFound()
- ✅ shouldListActiveServicesOnly()
- ✅ shouldDeactivateServiceWithoutDeleting()
- ✅ shouldThrowExceptionWhenPriceIsZero()
- ✅ shouldThrowExceptionWhenDurationLessThan15()
- ✅ shouldIsolateTenantCatalog()

**Arquivo:** `src/test/java/br/com/dht/apibackend/domain/catalog/CatalogServiceTest.java`

---

### Tarefa 1.4: Expandir AppointmentServiceTest (15 horas)
**Target:** 25 testes (expandir dos 13 existentes)
- ✅ 13 testes existentes
- ➕ shouldThrowExceptionWhenServiceItemNotFound()
- ➕ shouldCalculateEndTimeBasedOnServiceDuration()
- ➕ shouldRejectOverlappingAppointments()
- ➕ shouldIgnoreCanceledAppointmentsInOverlapCheck()
- ➕ shouldHandleMultipleConcurrentAppointments()
- ➕ shouldThrowExceptionWhenConfirmingAlreadyConfirmed()
- ➕ shouldThrowExceptionWhenCancelCompletedAppointment()
- ➕ shouldAllowCancelPendingOrConfirmed()
- ➕ shouldIsolateAppointmentsByTenant()
- ➕ shouldValidateStartTimeNotNull()
- ➕ shouldHandleBlockScheduleRequests()
- ➕ shouldListAppointmentsWithPagination()

**Arquivo:** `src/test/java/br/com/dht/apibackend/domain/appointment/AppointmentServiceTest.java`

---

### Tarefa 1.5: Testes de Integração (15 horas)
**Target:** 15 testes (novos arquivos com TestContainers)

**5 classes novas:**
1. `BarberIntegrationTest.java` — 3 testes
2. `ClientIntegrationTest.java` — 3 testes
3. `CatalogIntegrationTest.java` — 3 testes
4. `AppointmentIntegrationTest.java` — 3 testes
5. `MultiTenantIntegrationTest.java` — 3 testes

**Padrão:** `@SpringBootTest @Testcontainers` com PostgreSQL 16

---

### Tarefa 1.6: Testes de Segurança (12 horas)
**Target:** 12 testes (novos arquivos)

**4 classes novas:**
1. `SecurityFilterTest.java` — 4 testes (autenticação)
2. `AuthorizationTest.java` — 4 testes (autorização)
3. `RateLimitingTest.java` — 2 testes (rate limit)
4. `InputValidationTest.java` — 2 testes (validação)

---

### Tarefa 1.7: Expandir Controller Tests (10 horas)
**Target:** 10 testes (expandir controllers existentes ou criar novos)

- `BarberControllerTest.java` — 3 testes (expandir dos 3)
- `ClientControllerTest.java` — 2 testes (existente + expansão)
- `CatalogControllerTest.java` — 2 testes (novo)
- `AppointmentControllerTest.java` — 3 testes (novo)

---

### Tarefa 1.8: Cobertura com Jacoco (5 horas)
**Target:** Gerar relatório, validar 70%+

```bash
✅ Rodarsearch todos testes
   .\mvnw.cmd clean test

✅ Gerar relatório Jacoco
   .\mvnw.cmd jacoco:report
   
✅ Abrir relatório
   target/site/jacoco/index.html
   
✅ Validar cobertura
   ✅ Services: >80%
   ✅ Controllers: >70%
   ✅ DTOs: >60%
   ✅ Geral: >70% (critério de sucesso)
```

---

## 📈 TIMELINE SPRINT 1

| Tarefa | Horas | Status |
|--------|:-----:|:------:|
| 1.1 (BarberServiceTest) | 12h | ✅ COMPLETO |
| 1.2 (ClientServiceTest) | 10h | ▶️ PRÓXIMO |
| 1.3 (CatalogServiceTest) | 10h | ⏳ SEMANA 1 |
| 1.4 (AppointmentServiceTest) | 15h | ⏳ SEMANA 1 |
| 1.5 (Integração) | 15h | ⏳ SEMANA 1/2 |
| 1.6 (Segurança) | 12h | ⏳ SEMANA 2 |
| 1.7 (Controllers) | 10h | ⏳ SEMANA 2 |
| 1.8 (Jacoco) | 5h | ⏳ SEMANA 2 |
| **TOTAL** | **79h** | ▶️ PROGRESSO |

---

## 🎯 STATUS RESUMIDO

```
SPRINT 0: ✅ COMPLETO
  ├─ 4 Services com logging
  ├─ 5 headers HTTP security
  ├─ DTOs com validações
  ├─ 29 testes
  └─ Build SUCESSO

SPRINT 1: ▶️ INICIADA (12% completo)
  ├─ ✅ Tarefa 1.1 (BarberServiceTest 12 testes)
  ├─ ▶️ Tarefa 1.2 (ClientServiceTest próximo)
  ├─ ⏳ Tarefa 1.3-1.8
  └─ 🎯 Target: 94+ testes, 70%+ cobertura
```

---

## 🚀 PRÓXIMO PASSO

**Tarefa 1.2:** Expandir **ClientServiceTest** de 4 para 10 testes

Quer começar agora?

---

`Sprint 1 iniciada: 25 maio 2026`  
`Tarefa 1.1 completa: ✅ BarberServiceTest 12/12 testes`  
`Progresso: 12/94 testes (12.8%)`

