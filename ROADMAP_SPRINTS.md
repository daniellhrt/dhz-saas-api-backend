# Roadmap de Desenvolvimento (Sprint-Based) — dhz-saas-api-backend

> Sequência prática semana-a-semana para levar o projeto de MVP para produção vendável.

---

## Sprint 0 (Fundação) — Semana 1

**Objetivo:** Infraestrutura, testes base e segurança mínima.

### Tarefa 0.1: Setup de testes (4h)
- [ ] Adicionar dependências: `spring-boot-starter-test`, `mockito-inline`, `testcontainers-postgresql` no pom.xml
- [ ] Criar pasta `src/test/java/br/com/dht/apibackend/support/` com TestContainers + annotation `@DataJpaTest`
- [ ] Primeira classe de teste: `AppointmentServiceTest` (mínimo 3 testes)

**Comando:**
```bash
# Rodar testes
mvn clean test

# Gerar relatório de cobertura
mvn clean test jacoco:report
```

### Tarefa 0.2: Validações rigorosas (3h)
- [ ] Revisar DTOs em `domain/*/` — adicionar `@NotBlank`, `@Email`, `@Future` conforme necessário
- [ ] Exemplo: `AppointmentRequest` deve ter `@Future LocalDateTime startTime`
- [ ] Adicionar message customizadas no `@NotNull` etc

### Tarefa 0.3: Logging estruturado (2h)
- [ ] Adicionar `@Slf4j` em todos os Services
- [ ] Log de login attempts, criação de resources: `log.info("Barbeiro {} criado em tenant {}", barber.getId(), tenantId)`
- [ ] LOG de erros com nível WARN/ERROR (nunca logar senhas)

### Tarefa 0.4: Headers de segurança HTTP (2h)
- [ ] Editar `SecurityConfig.java` para adicionar headers de segurança
- [ ] `Strict-Transport-Security`, `X-Content-Type-Options`, `X-Frame-Options`

### Entregáveis da Sprint:
- [ ] CI pipeline básico (GitHub Actions)
- [ ] Testes rodando em CI
- [ ] Relatório de cobertura gerado

---

## Sprint 1 (Testes Completos) — Semana 2–3

**Objetivo:** Cobertura mínima 70% nos Services; testes de segurança e integração.

### Tarefa 1.1: Testes de Services (20h)
Fazer para cada service (`Barber`, `Client`, `Appointment`, `Catalog`):
- Teste de criação com sucesso
- Teste de validação (campos inválidos)
- Teste de isolamento por tenant (requisição de tenant A não acessa dados do tenant B)
- Teste de erros esperados

**Estrutura:**
```java
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock AppointmentRepository appointmentRepository;
    @Mock ClientRepository clientRepository;
    @InjectMocks AppointmentService service;
    
    @BeforeEach void setup() { TenantContext.setTenantId(UUID.randomUUID().toString()); }
    @AfterEach void teardown() { TenantContext.clear(); }
    
    @Test void shouldCreateAppointment() { ... }
    @Test void shouldThrowWhenDoubleBooking() { ... }
    @Test void shouldThrowWhenClientNotFound() { ... }
}
```

### Tarefa 1.2: Testes de integração com TestContainers (12h)
```java
@SpringBootTest
@Testcontainers
class AppointmentIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    
    @Test void shouldCreateAndRetrieveAppointment() {
        // Usar RestTemplate ou MockMvc para fazer requests HTTP reais
    }
}
```

### Tarefa 1.3: Testes de segurança (8h)
```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {
    @Autowired MockMvc mockMvc;
    
    @Test void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/barbers"))
               .andExpect(status().isUnauthorized());
    }
    
    @Test void shouldReturn403WhenUserTriesAdminOperation() throws Exception { ... }
}
```

### Tarefa 1.4: Jacoco relatório (2h)
- [ ] Configurar `pom.xml` com plugin Jacoco
- [ ] Gerar relatório HTML: `mvn clean test jacoco:report`
- [ ] Alvo: 70%+ de cobertura

### Entregas:
- [ ] 50+ testes unitários
- [ ] 15+ testes de integração
- [ ] Cobertura ≥ 70%
- [ ] CI passou com todos os testes

---

## Sprint 2 (Performance & Banco) — Semana 4–5

**Objetivo:** Otimizações de banco, índices, caching.

### Tarefa 2.1: Índices de banco de dados (3h)
Criar migration `V8__add_indexes.sql`:
```sql
CREATE INDEX idx_barber_tenant_email ON barber(tenant_id, email) WHERE tenant_id IS NOT NULL;
CREATE INDEX idx_client_tenant ON client(tenant_id) WHERE tenant_id IS NOT NULL;
CREATE INDEX idx_appointment_tenant_time ON appointment(tenant_id, start_time, end_time, status) 
WHERE tenant_id IS NOT NULL AND status != 'CANCELED';
CREATE INDEX idx_service_tenant_active ON service_item(tenant_id, active) WHERE active = true;
```

### Tarefa 2.2: Caching com Spring Cache (8h)
- [ ] Adicionar `spring-boot-starter-cache` no pom.xml
- [ ] Habilitar `@EnableCaching` em `DhzSaasApiBackendApplication.java`
- [ ] Em `CatalogService`:
  ```java
  @Cacheable(value = "catalog", key = "#tenantId")
  public Page<ServiceItemDTO> listActive(String tenantId, Pageable pageable) { ... }
  
  @CacheEvict(value = "catalog", key = "#tenantId")
  public void createService(...) { ... }
  ```

### Tarefa 2.3: Connection pooling & timeouts (4h)
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 600000
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 15
```

### Tarefa 2.4: Paginação com limite máximo (2h)
Adicionar validator customizado:
```java
@GetMapping
public Page<BarberDTO> list(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") @Max(100) int size
) {
    return service.list(PageRequest.of(page, size));
}
```

### Entregas:
- [ ] Migrations com índices rodadas
- [ ] Caching funcional (testar com cache eviction)
- [ ] Query performance melhorada (verificar EXPLAIN PLAN)

---

## Sprint 3 (Monitoramento & API Docs) — Semana 6

**Objetivo:** Actuator, métricas, swagger melhorado, readiness probes.

### Tarefa 3.1: Actuator & Health checks (3h)
- [ ] Adicionar `spring-boot-starter-actuator`
- [ ] Configurar `application.yml`:
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,metrics,info
    endpoint:
      health:
        show-details: when-authorized
  ```
- [ ] Criar controller simples para `/health` com status de DB e Redis

### Tarefa 3.2: Métricas Micrometer (5h)
- [ ] Adicionar `micrometer-core` (já vem com Spring Boot)
- [ ] Criar custom metrics: "appointments.created", "appointments.canceled"
- [ ] Exportar para Prometheus (opcional, mas bom ter pronto)

**Exemplo:**
```java
@Component
public class AppointmentMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordAppointmentCreated() {
        meterRegistry.counter("appointments.created").increment();
    }
}
```

### Tarefa 3.3: Swagger melhorado (4h)
- [ ] Adicionar `@Operation`, `@ApiResponse`, `@Parameter` em todos os controllers
- [ ] Exemplo em `BarberController`:
  ```java
  @PostMapping
  @Operation(summary = "Criar barbeiro", description = "Cria novo barbeiro (requer ADMIN)")
  @ApiResponse(responseCode = "201", description = "Barbeiro criado")
  @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN)")
  public ResponseEntity<BarberDTO> create(@RequestBody @Valid BarberDTO.Request req) { ... }
  ```

### Tarefa 3.4: Readiness/Liveness probes para K8s (2h)
- [ ] Adicionar endpoint customizado `/api/v1/health/ready` (verifica DB ok)
- [ ] Endpoint `/api/v1/health/alive` (só retorna OK se rodando)

### Entregas:
- [ ] Swagger UI com documentação completa
- [ ] `/actuator/health` retornando status correto
- [ ] Métricas sendo coletadas

---

## Sprint 4 (Segurança Avançada) — Semana 7

**Objetivo:** Refresh tokens, change password, tokens com expiration, headers security.

### Tarefa 4.1: Change Password endpoint (4h)
```java
@PatchMapping("/auth/change-password")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest req) {
    // Validar senha atual
    // Validar força de nova senha (min 8 chars, número, especial)
    // Salvar com BCrypt
}
```

### Tarefa 4.2: Refresh token (5h)
- [ ] Adicionar coluna `refresh_token` e `refresh_token_expires_at` na tabela `barber`
- [ ] POST `/api/v1/auth/refresh` com refresh_token → retorna novo access_token
- [ ] Access token: 15 minutos
- [ ] Refresh token: 7 dias

### Tarefa 4.3: Token blacklist (opcional, 2h)
Se quiser logoff real:
- [ ] Redis: guardar tokens expirados em blacklist
- [ ] `SecurityFilter` verifica se token está em blacklist antes de validar

### Tarefa 4.4: JWT com claims extras (1h)
- [ ] Adicionar `iat` (issued at) e `exp` explicitamente no token
- [ ] Validar `exp` em SecurityFilter

### Entregas:
- [ ] POST `/auth/change-password` funcional
- [ ] POST `/auth/refresh` funcional
- [ ] Senhas armazenadas com força mínima validada

---

## Sprint 5 (Deploy & Infra) — Semana 8–9

**Objetivo:** CI/CD pipeline, Docker otimizado, secrets management, docs de deploy.

### Tarefa 5.1: GitHub Actions CI/CD (5h)
```yaml
# .github/workflows/ci.yml
name: CI/CD
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
      redis:
        image: redis:7-alpine
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - run: mvn clean package -DskipTests
      - run: mvn clean test
      - run: docker build -t myapp:latest .
      - run: docker login -u ${{ secrets.DOCKER_USER }} -p ${{ secrets.DOCKER_PASS }}
      - run: docker push myapp:latest
```

### Tarefa 5.2: Secrets via CI e em produção (3h)
- [ ] Adicionar GitHub Secrets: `DOCKER_USER`, `DOCKER_PASS`, `JWT_SECRET`, `DB_PASSWORD`
- [ ] Documentar ou criar script: `.env.production.example` (sem valores sensíveis)
- [ ] Para K8s: usar AWS Secrets Manager ou HashiCorp Vault

### Tarefa 5.3: Docker otimizado & Dockerfile (2h)
- [ ] Dockerfile já existe e é multi-stage (bom!)
- [ ] Verificar: versão Java pinada, non-root user `spring:spring`, pequeno tamanho de imagem

### Tarefa 5.4: Kubernetes deployment (8h, opcional)
Se usando K8s (Heroku, EKS, GKE, etc):
```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dhz-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: dhz-api
  template:
    metadata:
      labels:
        app: dhz-api
    spec:
      containers:
      - name: api
        image: myapp:latest
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/ready
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
        resources:
          requests:
            cpu: 500m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 1Gi
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: JWT_SECRET
```

### Tarefa 5.5: Documentação de deploy (3h)
Criar `DEPLOYMENT_GUIDE.md` (já feito!) com:
- [ ] Variáveis de ambiente obrigatórias
- [ ] Checklist pré-deploy
- [ ] Procedimento de rollback
- [ ] Contatos de emergência

### Entregas:
- [ ] CI/CD pipeline rodando no GitHub Actions
- [ ] Imagem Docker publicada em registry
- [ ] Deployment docs pronto
- [ ] Tudo pronto para deploy em staging

---

## Sprint 6 (Relatórios & Analytics) — Semana 10

**Objetivo:** Endpoints de relatórios, auditoria com created_at/updated_at.

### Tarefa 6.1: Campos de auditoria (5h)
Adicionar migrations auto-populate:
```sql
ALTER TABLE barber ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE barber ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE barber ADD COLUMN created_by UUID;  -- ID do barbeiro que criou
```

Adicionar no `@Entity` base:
```java
@MappedSuperclass
public abstract class BaseEntity {
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private UUID createdBy;
}
```

### Tarefa 6.2: Endpoints de relatório (5h)
```java
@GetMapping("/reports/appointments")
@PreAuthorize("hasRole('ADMIN')")
public AppointmentReportDTO getAppointmentReport(
    @RequestParam LocalDate startDate,
    @RequestParam LocalDate endDate
) {
    // Total confirmado, cancelado, na fila
    // Receita estimada
    // Barbeiro top
}

GET /reports/revenue?startDate=2026-05-01&endDate=2026-05-31
```

### Entregas:
- [ ] Campos de auditoria sincados com a base
- [ ] 2-3 endpoints de relatório funcionando

---

## Sprint 7 (QA & Testes de Carga) — Semana 11

**Objetivo:** Teste de carga, teste de failover, tudo funcionando sob stress.

### Tarefa 7.1: Teste de carga com k6 ou JMeter (8h)
```javascript
// test-load.js (k6)
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 200 },
    { duration: '5m', target: 200 },
    { duration: '2m', target: 0 },
  ],
};

export default function () {
  let res = http.get('http://localhost:8080/api/v1/barbers');
  check(res, { 'status is 200': (r) => r.status === 200 });
}
```

Executar: `k6 run test-load.js`

### Tarefa 7.2: Teste de resiliência (4h)
- [ ] Derrubar Redis — verificar fallback em memória
- [ ] Derrubar banco — verificar se traz erro 503 ou 500 apropriado
- [ ] Lentidão do banco (adicionar delay em query) — verificar timeout correto

### Tarefa 7.3: QA manual de fluxo completo (4h)
- [ ] Registrar novo barbeiro
- [ ] Logar
- [ ] Criar cliente
- [ ] Criar serviço
- [ ] Agendar em múltiplos horários
- [ ] Confirmar/cancelar/completar
- [ ] Verificar listagem com paginação
- [ ] Testar rate limit (fazer >5 logins em 1 min)

### Entregas:
- [ ] Teste de carga passou (latência P95 < 500ms)
- [ ] Zero crashes sob stress
- [ ] Fallback de Redis funcionando

---

## Sprint 8 (Produção — Go-Live) — Semana 12

**Objetivo:** Deploy em staging, validação final, deploy em produção.

### Tarefa 8.1: Deploy em staging (2h)
- [ ] Apontar novo ambiente (staging.api.empresa.com)
- [ ] Rodar migrations
- [ ] Testar pontos críticos

### Tarefa 8.2: Monitoramento em staging (2h)
- [ ] Dashboards no Prometheus/Grafana
- [ ] Alertas configurados (CPU > 80%, error rate > 1%, latência P95 > 1s)

### Tarefa 8.3: Incident runbook (2h)
Documentar:
- Como escalar quando memória está alta
- Como restaurar a partir de backup
- Contatos de emergência
- Escalação de suporte

### Tarefa 8.4: Deploy em produção (2h)
- [ ] Backup do banco antes de deploy
- [ ] Blue-green deployment (old + new rodando, switch gradual)
- [ ] Monitorar por 2 horas após deploy

### Tarefa 8.5: Celebration 🎉 (0h)
- Projeto vendável!

### Entregas:
- [ ] API rodando em produção
- [ ] HTTPS/TLS habilitado
- [ ] Certificado SSL válido
- [ ] Logs e métricas visíveis em produção
- [ ] Usuário pode se registrar e logar

---

## Checklist Final de Vendas

- [x] Testes: 70%+ cobertura, CI/CD pipeline OK
- [x] Segurança: HTTPS, rate limiting, validações, headers security
- [x] Performance: índices de BD, caching, paginação
- [x] Monitoramento: Actuator, métricas, alerts
- [x] Docs: Swagger, DEPLOYMENT_GUIDE, AGENTS.md
- [x] Resiliência: Failover testado, backup automático
- [x] Escalabilidade: Horizontal scaling possível, K8s ready
- [x] Legal: LGPD checklist iniciado

---

**Tempo Total: 12 semanas (300-400 horas)**  
**Com 2 devs em paralelo: 6-8 semanas**  
**Com 1 dev: 12 semanas**

Próximo passo: Começar Sprint 0 semana que vem! 🚀

