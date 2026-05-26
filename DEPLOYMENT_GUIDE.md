# Guia de Etapas para Deploy e Venda (dhz-saas-api-backend)

> Este documento lista as melhorias e tarefas necessárias no backend para finalizar o projeto e colocá-lo em produção de forma segura e escalável.

---

## 1. Testes (Crítico — P0)

### 1.1 Testes unitários nos Services
- [ ] Adicionar testes unitários para `AppointmentService` (validar double-booking, transições de estado)
- [ ] Adicionar testes para `BarberService` (criação, atualização, validações de ADMIN/USER)
- [ ] Adicionar testes para `ClientService` e `CatalogService`
- [ ] Usar Mockito para mockar repositories

**Exemplo:**
```java
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock AppointmentRepository appointmentRepository;
    @InjectMocks AppointmentService service;
    
    @Test void shouldThrowWhenOverlappingAppointment() { ... }
}
```

### 1.2 Testes de integração (com banco real)
- [ ] Usar `@SpringBootTest` + TestContainers (PostgreSQL) para testar fluxos end-to-end
- [ ] Validar isolamento por tenant: confirmar que queries sempre trazem dados do tenant correto

### 1.3 Testes de segurança
- [ ] Testar autenticação: requisições sem token devem retornar 401
- [ ] Testar autorização: USER não deve conseguir criar barbeiros
- [ ] Validar que `TenantContext` não vaza entre requisições (usar Thread Pool no teste)

### 1.4 Cobertura de testes
- [ ] Target: mínimo 70% de cobertura de código crítico (Services, Repositories)
- [ ] Usar `jacoco-maven-plugin` para medir e relatar cobertura

---

## 2. Validações e Tratamento de Erros (P0)

### 2.1 Validações de entrada mais rigorosas
- [ ] Adicionar validações em DTOs com `@NotBlank`, `@Email`, `@Min/@Max`
- [ ] Validar datas: `startTime` não pode ser no passado
- [ ] Validar duração de serviços: `durationMinutes` > 0
- [ ] Validar phone: padrão brasileiro ou internacional

**Exemplo:**
```java
public record AppointmentRequest(
    @NotNull UUID clientId,
    @NotNull UUID serviceItemId,
    @Future LocalDateTime startTime
) {}
```

### 2.2 Mensagens de erro claras
- [ ] Revisar todas as mensagens de `IllegalArgumentException`/`IllegalStateException`
- [ ] Adicionar tradução para português consistente
- [ ] Incluir dica de ação no erro (ex: "Serviço não existe. Crie um novo em /catalog")

### 2.3 Logging estruturado
- [ ] Adicionar logs significativos: login attempts, criação/exclusão de recursos, erros
- [ ] Usar níveis corretos: INFO (eventos normais), WARN (anomalias), ERROR (falhas)
- [ ] Evitar logar dados sensíveis (senhas, tokens completos)

**Exemplo:**
```java
@Slf4j
public class AppointmentService {
    public AppointmentDTO.Response scheduleAppointment(AppointmentDTO.Request request) {
        log.info("Agendamento criado para cliente {} em tenant {}", request.clientId(), tenantId);
        // ...
    }
}
```

---

## 3. Autenticação e Segurança Avançada (P0)

### 3.1 Endpoints de gerenciamento de conta
- [ ] POST `/api/v1/auth/change-password` — Permitir trocar senha
- [ ] POST `/api/v1/auth/refresh-token` — Renovar token sem fazer login novamente
- [ ] POST `/api/v1/auth/logout` — Blacklist token (opcional, JWT é stateless)

### 3.2 Segurança HTTP
- [ ] Adicionar headers de segurança:
  - `Strict-Transport-Security` (HTTPS only)
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY` (clickjacking)
- [ ] Implementar em `SecurityConfig` ou filtro customizado

**Exemplo:**
```java
http.headers(headers -> headers
    .contentTypeOptions(Customizer.withDefaults())
    .xssProtection(Customizer.withDefaults())
    .frameOptions(frameOptions -> frameOptions.deny())
);
```

### 3.3 Validação de JWT aprimorada
- [ ] Verificar expiração do token
- [ ] Implementar refresh token com TTL menor que o access token
- [ ] Adicionar verificação de IP/user-agent para detectar roubo de token (opcional)

### 3.4 Proteção contra CSRF e CORS refinada
- [ ] CORS já está desabilitado no `SecurityConfig` (bom); revisar `CorsConfig` de exemplo em `API.md`
- [ ] Se CORS for necessário, whitelist apenas domínios conhecidos (nunca `*`)

---

## 4. Performance e Escalabilidade (P1)

### 4.1 Índices de banco de dados
- [ ] Adicionar índice em `Barber(tenant_id, email)` para validar duplicação mais rápido
- [ ] Índice em `Client(tenant_id)` para listagem
- [ ] Índice em `Appointment(tenant_id, startTime, endTime)` para double-booking check

**Exemplo Flyway:**
```sql
CREATE INDEX idx_barber_tenant_email ON barber(tenant_id, email);
CREATE INDEX idx_appointment_tenant_time ON appointment(tenant_id, start_time, end_time) WHERE status != 'CANCELED';
```

### 4.2 Caching
- [ ] Cache de catálogo: serviços ativos mudam raramente → usar Redis/Spring Cache
- [ ] Invalidar cache ao criar/atualizar/deletar serviço
- [ ] TTL: 1 hora para dados menos críticos

**Exemplo:**
```java
@Cacheable(value = "services", key = "#tenantId")
public List<ServiceItem> getActiveServices(String tenantId) { ... }

@CacheEvict(value = "services", key = "#tenantId")
public void createService(...) { ... }
```

### 4.3 Paginação obrigatória
- [ ] Revisar endpoints de listagem: verificar se todos usam `Pageable`
- [ ] Adicionar limite máximo de página (ex: size ≤ 100)
- [ ] Validar em controller

**Exemplo:**
```java
@GetMapping
public Page<BarberDTO> list(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20", max=100) int size) {
    return service.list(PageRequest.of(page, Math.min(size, 100)));
}
```

### 4.4 Connection pooling
- [ ] PostgreSQL: usar HikariCP (já vem no Spring Boot)
- [ ] Configurar em `application.yml`:
  ```yaml
  spring:
    datasource:
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
        connection-timeout: 20000
  ```

---

## 5. Monitoramento e Observabilidade (P1)

### 5.1 Health checks
- [ ] POST `/actuator/health` — Status da aplicação (UP/DOWN)
- [ ] Adicionar `spring-boot-starter-actuator` ao `pom.xml`
- [ ] Verificar status de banco e Redis

**Configuração:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
```

### 5.2 Métricas e APM
- [ ] Adicionar Micrometer para coletar métricas (tempo de requests, erros, etc.)
- [ ] Exportar para Prometheus/Grafana ou New Relic (opcional, mas importante em produção)
- [ ] Monitorar: latência P95/P99, taxa de erro, uso de memória

### 5.3 Distributed tracing (opcional, mas recomendado)
- [ ] Adicionar Spring Cloud Sleuth para rastrear requests na stack
- [ ] Correlação de logs com trace ID

---

## 6. Documentação e Onboarding (P1)

### 6.1 Documentação de API melhorada
- [ ] Revisar descrições no Swagger: adicionar exemplos de erro, timeouts
- [ ] Documentar headers esperados (ex: `Authorization: Bearer ...`)
- [ ] Documentar rate limiting (5 req/min no login)

**Exemplo em controller:**
```java
@PostMapping("/appointments")
@Operation(summary = "Criar agendamento", 
           description = "Cria novo agendamento com validação de conflito de horário")
@ApiResponse(responseCode = "201", description = "Agendamento criado")
@ApiResponse(responseCode = "400", description = "Double-booking ou cliente não encontrado")
public ResponseEntity<AppointmentDTO> create(@RequestBody @Valid AppointmentDTO.Request req) {
    // ...
}
```

### 6.2 Runbook de operações
- [ ] Documentar: como fazer backup manual, restaurar, escalar banco
- [ ] Incluir: comandos para debug, logs importantes, verificações de saúde

### 6.3 Guia de deploy
- [ ] Documentar variáveis de ambiente obrigatórias
- [ ] Incluir checklist pré-deploy (testes passando, migrations OK, secrets configurados)

---

## 7. Infraestrutura e Deploy (P1)

### 7.1 Database migrations em produção
- [ ] Revisitar todas as migrations em `src/main/resources/db/migration/`
- [ ] Testar rollback em case de erro
- [ ] Adicionar V8 migration com índices (de 4.1)

### 7.2 CI/CD pipeline
- [ ] Criar `.github/workflows/ci.yml` para rodar:
  - Testes (`mvn test`)
  - Lint/checkstyle
  - Build Docker (`docker build`)
  - Push para registry (DockerHub, ECR, etc.)

**Exemplo GitHub Actions:**
```yaml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: mvn clean test
      - run: docker build -t myapp:${{ github.sha }} .
```

### 7.3 Secrets management
- [ ] Não commitar `.env` — adicionar ao `.gitignore` (já deve estar)
- [ ] Em produção: usar AWS Secrets Manager, HashiCorp Vault, ou plataforma nativa (Heroku Config Vars, etc.)
- [ ] Rotacionar secrets regularmente (JWT_SECRET a cada 90 dias recomendado)

### 7.4 Dockerfile otimizado
- [ ] Dockerfile já é multi-stage (bom!)
- [ ] Verificar: versão do Java 21, uso de non-root user (spring:spring) já está

### 7.5 Kubernetes/Orchestration (se escalarà muito)
- [ ] Criar `deployment.yaml`, `service.yaml`, `configmap.yaml`
- [ ] Adicionar readiness/liveness probes
- [ ] Definir resources (CPU/memory requests e limits)

---

## 8. Regras de Negócio Adicionais (P2)

### 8.1 Avaliação de agendamentos
- [ ] GET `/api/v1/appointments/{id}/rating` — Permitir feedback do cliente
- [ ] PATCH `/api/v1/appointments/{id}/rate` — Guardar nota (1-5 estrelas)

### 8.2 Relatórios e analytics
- [ ] GET `/api/v1/reports/revenue` — Receita por período
- [ ] GET `/api/v1/reports/appointments` — Total de agendamentos confirmados/cancelados
- [ ] Adicionar campos de auditoria: `createdAt`, `updatedAt`, `createdBy` nas entidades

### 8.3 Notificações
- [ ] Integrar com email (SendGrid, AWS SES) para confirmação de agendamento
- [ ] POST `/api/v1/auth/send-password-reset` — Recuperação de senha por email

### 8.4 Soft limits e quotas (SaaS típico)
- [ ] Limitar número de barbeiros por barbearia (ex: plano básico = 3)
- [ ] Limitar agendamentos no futuro (ex: máx 90 dias à frente)

---

## 9. Testes de Carga e Stress (P2)

### 9.1 Teste de pico
- [ ] Usar ferramentas como JMeter, Locust ou k6
- [ ] Simular: 100, 500, 1000 usuários simultâneos
- [ ] Medir: latência, throughput, erros

### 9.2 Teste de resiliência
- [ ] Simular falha de Redis: verificar se fallback em memória funciona
- [ ] Simular lentidão do banco: ajustar timeouts se necessário

---

## 10. Legal e Compliance (P2)

### 10.1 LGPD / GDPR
- [ ] Implementar DELETE de dados pessoais (right to be forgotten)
- [ ] Adicionar endpoint: DELETE `/api/v1/auth/account` para barber deletar sua conta
- [ ] Revisar: dados coletados, consentimento, storage time

### 10.2 Terms of Service e Privacy Policy
- [ ] Vincular a arquivo estático no repositório ou site
- [ ] Adicionar checkbox no registro: "Li e concordo com os T.O.S"

---

## Checklist de Deploy em Produção

- [ ] Todos os testes passando (70%+ cobertura)
- [ ] Secrets configurados (JWT_SECRET, DB_PASSWORD, etc.)
- [ ] Database migrations testadas
- [ ] Logging e monitoring configurados
- [ ] Rate limiting ativo
- [ ] HTTPS/TLS habilitado
- [ ] Headers de segurança adicionados
- [ ] CI/CD pipeline funcionando
- [ ] Backups automáticos configurados
- [ ] Health check responde 200
- [ ] Documentação atualizada (API.md, DEPLOYMENT_GUIDE.md)
- [ ] Runbook de operações pronto
- [ ] Teste de carga passou (SLA de latência atingido)

---

## Priorização Sugerida

| Fase | Itens | Tempo Est. |
|------|-------|-----------|
| **MVP 1 (1-2 semanas)** | 1.1, 1.3, 2.1, 2.2, 3.1-3.2, 4.1, 5.1 | 80h |
| **MVP 2 (2-3 semanas)** | 1.2, 3.3-3.4, 4.2-4.4, 5.2-5.3, 6.1 | 100h |
| **Beta (2-3 semanas)** | 6.2-6.3, 7.1-7.4, 8.1 | 80h |
| **Production (1 semana)** | 9.1-9.2, 10.1, Deploy + QA final | 40h |

---

**Total estimado: 300-400 horas (5-8 semanas com 1 dev)"**

Comece pelo P0: testes + segurança + validações. Depois P1: performance + deploy. P2 é nice-to-have mas importante para produto maduro.

