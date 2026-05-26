# Phase 7: Backend Deploy & Cloud Config

## 1. Goal
Hospedar a API de forma acessível na internet junto ao banco de dados (Postgres) e Redis, configurando os arquivos de ambiente para suportar as variáveis automáticas do Railway e habilitando o CORS para o frontend (Vercel).

## 2. Approach
- **CORS:** Criar `CorsConfig.java` para permitir a comunicação com a aplicação web. O `allowedOrigins` será lido via variável de ambiente, com fallback seguro.
- **Variáveis de Ambiente:** Mapear propriedades no `application-prod.yml` para consumir variáveis do Railway (`PORT`, `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`, `REDISPASSWORD`, etc.).
- **Redis Auth:** Atualizar `RedisConfig.java` para verificar a presença de uma senha e adicioná-la ao `RedisURI` caso exista.

## 3. Tasks

- [ ] **Task 1: Criar configuração de CORS**
  - Criar classe `br.com.dht.apibackend.config.CorsConfig` implementando `WebMvcConfigurer`.
  - Ler variável de ambiente `${CORS_ALLOWED_ORIGINS:*}` (permitindo `*` por padrão para desenvolvimento e flexibilidade inicial de testes, ou a URL exata do Vercel via Env Var na nuvem).
  - Configurar `.addMapping("/**").allowedOrigins(origins).allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS").allowedHeaders("*")`.

- [ ] **Task 2: Atualizar RedisConfig para suportar Senha**
  - Modificar `br.com.dht.apibackend.config.RedisConfig.java`.
  - Adicionar `@Value("${spring.data.redis.password:#{null}}") String password`.
  - Atualizar o `RedisURI.builder()` para incluir `.withPassword(password.toCharArray())` se `password` não for nulo nem vazio.

- [ ] **Task 3: Atualizar application-prod.yml e application.yml**
  - Em `application.yml` (ou `application-prod.yml`), definir: `server: port: ${PORT:8080}`.
  - No `application-prod.yml`, corrigir o typo `nao# Arquivo:...` na linha 1.
  - Mapear Postgres: `url: jdbc:postgresql://${PGHOST:${DB_HOST:postgres}}:${PGPORT:${DB_PORT:5432}}/${PGDATABASE:${POSTGRES_DB:postgres}}`, username/password para `PGUSER`/`PGPASSWORD` com fallback.
  - Mapear Redis: `host: ${REDISHOST:${REDIS_HOST:localhost}}`, `port: ${REDISPORT:${REDIS_PORT:6379}}`, `password: ${REDISPASSWORD:}`.

## 4. Verification
1. Subir o ambiente local com `docker compose up` para garantir que o fallback ainda funciona sem quebrar a pipeline de dev.
2. Rodar a suíte de testes unitários e de integração `.\mvnw.cmd clean test` para garantir que `CorsConfig` e `RedisConfig` não quebram o contexto do Spring.
3. Deploy manual no Railway da branch `main` e validação se as vars são injetadas com sucesso e o health endpoint (ou Swagger UI) responde 200 OK publicamente.
