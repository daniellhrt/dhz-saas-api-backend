# Conventions

> Last mapped: 2026-05-26

## Code Style

### Java Records for DTOs
All DTOs use Java `record` types with nested classes inside a parent DTO class:
```java
public class BarberDTO {
    public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password
    ) {}

    public record Response(UUID id, String name, String email, BarberRole role) {
        public static Response fromEntity(Barber barber) { ... }
    }
}
```

### Lombok Annotations
- `@RequiredArgsConstructor` — Constructor injection (no `@Autowired`)
- `@Slf4j` — Logger (`log.info()`, `log.warn()`)
- `@Getter` / `@Setter` — Entity field access
- `@Builder` — `StandardError` construction

### Javadoc Headers
Every class has a 3-line doc block:
```java
/**
 * Propósito: [purpose]
 * Responsabilidade: [responsibility]
 * Papel na Arquitetura: [layer / role]
 */
```

## Naming Patterns

### Methods
- Service: `createBarber()`, `updateBarber()`, `deleteBarber()`, `listAllBarbers()`
- Repository: `findByIdAndTenantId()`, `findAllByTenantId()`, `findByEmail()`
- Controller: HTTP methods map directly (`@PostMapping`, `@GetMapping`, `@PutMapping`, `@DeleteMapping`)

### Variables
- `currentTenant` — always from `TenantContext.getTenantId()`
- `currentEmail` — always from `SecurityContextHolder.getContext().getAuthentication().getName()`

## Error Handling

### Exception Hierarchy
| Exception | HTTP Status | Error Label |
|-----------|:-----------:|-------------|
| `InvalidCredentialsException` | 401 | "Unauthorized" |
| `IllegalArgumentException` | 400 | "Business Rule Violation" |
| `IllegalStateException` | 400 | "Business Rule Violation" |
| `SecurityException` | 403 | "Forbidden" |
| `MethodArgumentNotValidException` | 400 | "Validation Error" |
| `Exception` (fallback) | 500 | "Internal Server Error" |

### StandardError Format
```json
{
  "timestamp": "2026-05-26T00:00:00.000",
  "status": 400,
  "error": "Business Rule Violation",
  "message": "Já existe um cliente com este e-mail nesta barbearia.",
  "path": "/api/v1/clients"
}
```

### Convention: Business Rule Exceptions
- Throw `IllegalArgumentException` for invalid input or not-found cases
- Throw `IllegalStateException` for business rule violations (e.g., double-booking)
- Throw `SecurityException` for authorization failures (caught by `GlobalExceptionHandler`)
- **Never** throw checked exceptions from service layer

## Security Patterns

### Tenant Isolation Pattern
```java
// Every service method follows this pattern:
String currentTenant = TenantContext.getTenantId();
Entity entity = repository.findByIdAndTenantId(id, currentTenant)
    .orElseThrow(() -> new IllegalArgumentException("Entidade não encontrada."));
```

### ADMIN Role Assertion
```java
private void assertAdminRole(String email) {
    Barber current = barberRepository.findByEmail(email).orElseThrow(...);
    if (current.getRole() != BarberRole.ADMIN) {
        throw new SecurityException("Acesso negado: apenas ADMIN pode realizar esta operação.");
    }
}
```

### Soft Delete
Catalog uses `active` flag instead of physical deletion:
```java
public void deactivateService(UUID id) {
    item.setActive(false);  // Not repository.delete()
}
// Listing only returns active items: findAllByTenantIdAndActiveTrue()
```

## Validation Annotations (JSR-380)
- `@NotBlank` — Required strings
- `@Email` — Email format
- `@Size(min = 6)` — Password minimum length
- `@Pattern` — Phone format `(11) 99999-8888`
- `@DecimalMin("0.01")` — Minimum price
- `@Min(15)` — Minimum duration (minutes)

## Logging

### Pattern
- `log.info()` for successful operations: creation, update, deletion, state transitions
- `log.warn()` for business rule violations: duplicates, unauthorized access, rate limiting
- `log.error()` only in `GlobalExceptionHandler` for unhandled exceptions
- Messages always include `tenant_id` context for traceability
