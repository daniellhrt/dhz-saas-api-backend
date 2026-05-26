# Phase 5: Rate Limiting Distribuído (Redis)

## Goal
Substituir o rate limiting em memória (ConcurrentHashMap) por Redis distribuído, permitindo que múltiplas instâncias da API compartilhem o mesmo limite de taxa.

## Requirements
- **RL-01**: Adicionar dependência do Redis (Spring Data Redis + Bucket4j Lettuce)
- **RL-02**: Substituir ConcurrentHashMap por bucket distribuído no Redis via LettuceProxyManager
- **RL-03**: Adicionar Redis ao compose.yaml

## Tasks

### Wave 1 — Dependências
**Dependencies:** Nenhuma

- [ ] `pom.xml` — adicionar `spring-boot-starter-data-redis`
- [ ] `pom.xml` — adicionar `bucket4j-lettuce`

### Wave 2 — Redis service
**Dependencies:** Nenhuma

- [ ] `compose.yaml` — adicionar serviço Redis (imagem redis:7-alpine)
- [ ] `compose.yaml` — adicionar variável REDIS_HOST ao serviço api

### Wave 3 — Redis config
**Dependencies:** Nenhuma

- [ ] `application.yml` — adicionar spring.data.redis.host
- [ ] `application-dev.yml` — configurar Redis para dev

### Wave 4 — LettuceProxyManager config
**Dependencies:** Wave 1

- [ ] `config/RedisConfig.java` — bean LettuceProxyManager para gerenciar buckets no Redis

### Wave 5 — RateLimitingFilter
**Dependencies:** Wave 4

- [ ] `RateLimitingFilter.java` — substituir ConcurrentHashMap por ProxyManager Lettuce

## Verification
1. Compilação sem erros
2. Redis aparece no compose.yaml com porta 6379
3. Rate limiting persiste entre requisições via Redis (teste indireto)
