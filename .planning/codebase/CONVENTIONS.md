# Convenções — dhz-saas-api-backend

> Mapeado em: 2026-05-21

## Linguagem & Estilo

- **Idioma do código:** Inglês (nomes de classes, métodos, variáveis)
- **Idioma dos comentários:** Português (BR) — Javadoc e comentários internos
- **Java version:** 21 (sem uso explícito de features modernas como virtual threads)

## Estrutura de Pacotes

```
br.com.dht.apibackend
├── config/              # Configurações transversais (JWT, Tenant)
├── domain/              # Domínios de negócio (feature-based)
│   ├── appointment/     # Entity, Controller, Service, Repository, DTO, Enum
│   ├── barber/          # Entity, Repository (sem Controller/Service)
│   ├── catalog/         # Entity, Controller, Service, Repository, DTO
│   └── client/          # Entity, Controller, Service, Repository, DTO
├── exception/           # Tratamento global de exceções
└── security/            # Auth, JWT, Filtros, DTOs de auth
    └── dto/
```

**Padrão:** Pacotes por domínio/feature, não por camada técnica.

## Padrão por Domínio

Cada domínio segue a estrutura:
- `Entity.java` — JPA Entity com Lombok
- `Repository.java` — Spring Data JPA (extends `JpaRepository<Entity, UUID>`)
- `Service.java` — Lógica de negócio com `@Service`
- `Controller.java` — REST endpoints com `@RestController`
- `DTO.java` — Records Java como classes internas (ex: `ClientDTO.Request`, `ClientDTO.Response`)
- `Enum.java` — Enumerações de status (quando aplicável)

**Exceção:** `barber` só tem Entity + Repository (sem controller/service próprios — autenticação é via `security/`).

## Entidades (JPA)

- **PKs:** `UUID` em todas as entidades
- **Multi-tenancy:** Coluna `tenant_id` em todas as tabelas (`@Column(updatable = false)`)
- **Timestamps:** `@CreationTimestamp` para `created_at`
- **Construtores:** `protected` no-arg constructor (requisito JPA) + construtores customizados
- **Lombok:** `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@NoArgsConstructor`, `@EqualsAndHashCode(of = "id")`
- **Builders:** `@Builder` em algumas entidades

## DTOs

- **Tipo:** Java Records (imutáveis)
- **Estrutura:** Classes wrapper com records internos (ex: `ClientDTO.Request`, `ClientDTO.Response`)
- **Validação:** Jakarta Bean Validation annotations (`@NotBlank`, `@Email`, `@NotNull`, `@Min`, etc.)
- **Conversão:** Métodos estáticos `toEntity()` e `fromEntity()` nos records

## Controllers (REST)

- **Base path:** `/api/v1/{domínio}`
- **Anotações:** `@RestController`, `@RequestMapping`
- **Respostas:** `ResponseEntity<>` com status HTTP explícitos
- **Validação:** `@Valid` nos parâmetros de request body

## Services

- **Anotação:** `@Service` com `@RequiredArgsConstructor`
- **Injeção:** Via construtor (Lombok gera)
- **Tenant:** `TenantContext.getTenantId()` para filtrar dados por tenant
- **Exceções:** `RuntimeException` com mensagens descritivas em português

## Tratamento de Erros

- **Handler global:** `@RestControllerAdvice` em `GlobalExceptionHandler`
- **Formato:** `StandardError` (timestamp, status, error, message, path)
- **Exceções tratadas:** `MethodArgumentNotValidException`, `RuntimeException`
- **Logging:** Comentado — placeholder para implementação futura

## API Versioning

- **Estratégia:** URL-based (`/api/v1/...`)

## Documentação de Código

Cada classe Java possui bloco de comentário com:
```java
/**
 * Propósito: [descrição]
 * Responsabilidade: [o que faz]
 * Papel na Arquitetura: [onde se encaixa]
 */
```

---
*Mapeado: 2026-05-21 via gsd-map-codebase*
