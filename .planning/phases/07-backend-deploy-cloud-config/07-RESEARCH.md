# Phase 7 Research

## 1. Cloud Provider Setup (Railway)
- Railway suporta deploy via `Dockerfile`. Como o projeto já possui um `Dockerfile` multi-stage bem configurado (Java 21 + Alpine), o Railway o detectará e o usará automaticamente.
- O Railway provisiona PostgreSQL e Redis como serviços no mesmo projeto. Eles expõem variáveis automáticas como `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` (Postgres) e `REDISHOST`, `REDISPORT`, `REDISPASSWORD` (Redis).
- O Railway injeta a porta HTTP na variável `PORT`. O Spring Boot precisa escutar nessa porta.

## 2. Configuração de Variáveis de Ambiente
Atualmente, o `application-prod.yml` está esperando variáveis customizadas (`DB_HOST`, `POSTGRES_DB`, etc.). Para um deploy "zero-config" no Railway, devemos atualizar o YAML para usar as variáveis do Railway como opções primárias:
```yaml
server:
  port: ${PORT:8080}

spring:
  datasource:
    url: jdbc:postgresql://${PGHOST:${DB_HOST:postgres}}:${PGPORT:${DB_PORT:5432}}/${PGDATABASE:${POSTGRES_DB:postgres}}
    username: ${PGUSER:${POSTGRES_USER:postgres}}
    password: ${PGPASSWORD:${POSTGRES_PASSWORD:postgres}}
  data:
    redis:
      host: ${REDISHOST:${REDIS_HOST:localhost}}
      port: ${REDISPORT:${REDIS_PORT:6379}}
      password: ${REDISPASSWORD:}
```
*Obs: O arquivo `application-prod.yml` atual possui um typo na linha 1 (`nao# Arquivo:...`) que precisa ser corrigido.*

## 3. Configuração do Redis (Bucket4j)
O arquivo `br.com.dht.apibackend.config.RedisConfig` cria o `RedisURI` manualmente:
```java
RedisURI redisUri = RedisURI.builder().withHost(host).withPort(port).build();
```
Como o Redis em produção (Railway) quase sempre exige senha, precisamos injetar a propriedade de senha e adicioná-la ao builder se existir:
```java
@Value("${spring.data.redis.password:#{null}}") String password
// ...
RedisURI.Builder builder = RedisURI.builder().withHost(host).withPort(port);
if (password != null && !password.isEmpty()) {
    builder.withPassword(password.toCharArray());
}
RedisURI redisUri = builder.build();
```

## 4. Configuração de CORS (Integração Frontend Vercel)
A API Spring Boot bloqueia requisições cross-origin por padrão. Como o frontend está na Vercel (`https://*.vercel.app` ou domínio próprio), precisamos adicionar uma configuração de CORS global.
Podemos criar um `br.com.dht.apibackend.config.CorsConfig` implementando `WebMvcConfigurer` e lendo uma variável `CORS_ALLOWED_ORIGINS`.

## Conclusão de Planejamento
O Planner deve gerar tarefas para:
1. Corrigir o typo no `application-prod.yml` e atualizar as propriedades do banco de dados e Redis para mapear as variáveis do Railway.
2. Atualizar o `RedisConfig.java` para suportar autenticação com senha.
3. Adicionar `server.port: ${PORT:8080}` no `application.yml` ou `application-prod.yml`.
4. Criar a classe `CorsConfig.java` para gerenciar a segurança de comunicação com o frontend.
