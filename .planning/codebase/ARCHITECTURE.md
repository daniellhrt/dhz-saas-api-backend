# Arquitetura — dhz-saas-api-backend

> Mapeado em: 2026-05-21

## Padrão Arquitetural

**Layered Architecture** com pacotes organizados por domínio (feature-based):

```
Controller (REST) → Service (Lógica de Negócio) → Repository (Acesso a Dados) → Entity (ORM)
     ↕                      ↕                          ↕
    DTO                TenantContext              JPA/Hibernate → PostgreSQL
```

## Camadas

### 1. Controller (Presentation)
- REST endpoints com `@RestController`
- Validação de entrada via `@Valid` + Bean Validation
- Retorna `ResponseEntity<DTO.Response>` com status HTTP explícito
- Sem lógica de negócio — delega tudo ao Service

### 2. Service (Business Logic)
- `@Service` com injeção por construtor (Lombok `@RequiredArgsConstructor`)
- Orquestra validações, regras de negócio, e persistência
- Obtém `tenantId` do `TenantContext` (ThreadLocal)
- Transações gerenciadas via `@Transactional`

### 3. Repository (Data Access)
- `JpaRepository<Entity, UUID>` do Spring Data
- Todos os queries incluem `tenantId` para isolamento
- Queries derivadas do nome do método + JPQL customizado

### 4. Entity (Domain Model)
- JPA entities mapeadas via annotations
- UUID como PK com geração automática
- `tenant_id` imutável (`updatable = false`)
- Lombok para boilerplate

## Fluxo de Dados

```
HTTP Request
    ↓
SecurityFilter (extrai JWT → seta TenantContext)
    ↓
Controller (valida DTO, roteia)
    ↓
Service (regras de negócio, anti-IDOR, conflitos)
    ↓
Repository (queries com tenant isolation)
    ↓
PostgreSQL (Flyway schema, row-level isolation)
    ↓
Entity → DTO.Response → HTTP Response
```

## Multi-Tenancy

**Estratégia:** Row-Level Isolation via coluna `tenant_id`

```
JWT Token (contém tenantId)
    ↓
SecurityFilter.doFilterInternal()
    ↓
TenantContext.setTenantId(tenantId)  ← ThreadLocal
    ↓
Service usa TenantContext.getTenantId()
    ↓
Repository filtra por tenantId
    ↓
TenantContext.clear()  ← finally block (previne vazamento)
```

**Contratos de segurança:**
- Todas as queries de Repository incluem `tenantId`
- Services validam que entidades referenciadas pertencem ao tenant (Anti-IDOR)
- `tenant_id` é `updatable = false` nas entities (não pode ser alterado)
- `ClientRepository` alerta: "NUNCA use métodos globais sem tenantId"

## Relacionamento entre Entidades

```
Barber (dono da barbearia / tenant owner)
  └── tenantId ──────┐
                     │ (chave de isolamento compartilhada)
Client ─────────────┤
  └── ManyToOne ←───┤── Appointment
ServiceItem ────────┤     ├── startTime / endTime
  └── ManyToOne ←───┘     └── status (PENDING → CONFIRMED → COMPLETED | CANCELED)
```

| Entidade | Relacionamentos | Notas |
|---|---|---|
| `Barber` | Nenhum (isolado) | Dono do tenant, usado para autenticação |
| `Client` | Referenciado por `Appointment` | `@ManyToOne` com `LAZY` fetch |
| `ServiceItem` | Referenciado por `Appointment` | `@ManyToOne` com `LAZY` fetch |
| `Appointment` | `→ Client`, `→ ServiceItem` | Calcula `endTime` da duração do serviço |

## Endpoints da API

| Método | Path | Status | Descrição |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | 200 | Login (público) |
| `POST` | `/api/v1/clients` | 201 | Criar cliente |
| `GET` | `/api/v1/clients` | 200 | Listar clientes do tenant |
| `POST` | `/api/v1/catalog` | 201 | Criar serviço |
| `GET` | `/api/v1/catalog` | 200 | Listar serviços ativos |
| `DELETE` | `/api/v1/catalog/{id}` | 204 | Desativar serviço (soft delete) |
| `POST` | `/api/v1/appointments` | 201 | Agendar atendimento |

## Tratamento de Erros (Cross-Cutting)

Três níveis via `@RestControllerAdvice`:

| Exceção | Status | Uso |
|---|---|---|
| `IllegalArgumentException` / `IllegalStateException` | 400 | Violação de regra de negócio |
| `MethodArgumentNotValidException` | 400 | Falha de validação de DTO |
| `Exception` (catch-all) | 500 | Erro genérico (mensagem segura) |

Formato de resposta: `StandardError` (timestamp, status, error, message, path)

## Regras de Negócio Implementadas

1. **Anti-IDOR:** Cliente e serviço devem pertencer ao tenant atual
2. **Double-Booking:** Query de overlap temporal (exclui `CANCELED`)
3. **Serviço inativo:** Não permite agendar serviço desativado
4. **Email único:** Cliente não pode ter email duplicado no mesmo tenant
5. **Nome único:** Serviço não pode ter nome duplicado no mesmo tenant (case-insensitive)
6. **Cálculo automático:** `endTime = startTime + serviceItem.durationMinutes`

---
*Mapeado: 2026-05-21 via gsd-map-codebase*
