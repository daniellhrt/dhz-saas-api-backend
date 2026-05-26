# Phase 7 Research: Deploy no Render

## 1. Cloud Provider Setup (Render)
- O Render possui suporte nativo a `Dockerfile`. Ele compila e roda a imagem Docker que já possuímos.
- **PostgreSQL**: O Render oferece banco de dados Postgres gerenciado. O principal diferencial é que ele expõe nativamente a variável de ambiente `DATABASE_URL` (no formato `postgres://user:pass@host/db`). O Spring Boot precisa do formato `jdbc:postgresql://...`.
- **Redis**: O Render oferece instâncias Redis gerenciadas. A variável exposta costuma ser `REDIS_URL` (no formato `rediss://user:pass@host:port`).

## 2. Environment Variables Mapping (Render vs Railway)
No Railway mapeamos `PGHOST`, `PGPORT`, `REDISPASSWORD`, etc. O Render centraliza nas URLs de conexão.
Para facilitar, o ideal no Render é que o próprio usuário crie variáveis de ambiente no serviço Web do Render:
- `SPRING_DATASOURCE_URL`: (O usuário copia a `Internal Database URL` do banco de dados no Render, substituindo `postgres://` por `jdbc:postgresql://`).
- `SPRING_DATASOURCE_USERNAME`: (O usuário preenche).
- `SPRING_DATASOURCE_PASSWORD`: (O usuário preenche).
- `SPRING_DATA_REDIS_URL`: (O usuário copia a `Internal Redis URL` do Redis no Render).

Para isso, o `application-prod.yml` pode ser simplificado de volta para um padrão enxuto, ou podemos tentar fazer um parse automático da variável `DATABASE_URL` nativa do Render, mas o parse no Spring Boot requer bibliotecas extras (ou strings complexas). Portanto, o melhor caminho é usar as variáveis nativas do Spring Boot (`spring.datasource.url`, etc.) e apenas documentar o que o usuário deve colar lá.

## 3. Código Modificado no Railway (Para Reverter)
- No Railway alteramos o `RedisConfig.java` para injetar a senha do Redis via `builder.withPassword()`. Se passarmos a usar o `spring.data.redis.url`, o próprio framework do Spring cuida da URL completa, então podemos desfazer a mudança manual do `RedisConfig.java` caso seja possível usar a URL nativa (mas o Bucket4j/Lettuce precisa receber o cliente).
Na verdade, `RedisClient.create(redisUri)` suporta passar uma URI no formato de string: `RedisClient.create(redisUrl)`.

## Conclusão
1. Atualizar o `application-prod.yml` para limpar as sujeiras de `PGHOST`, `PGPORT` e voltar para as env vars puras que o Render e Spring Boot entendem nativamente (`SPRING_DATASOURCE_URL`, `SPRING_DATA_REDIS_URL`, etc).
2. Atualizar o `RedisConfig.java` para, ao invés de host/port/password picotado, tentar ler diretamente uma variável `REDIS_URL` (padrão do Render).
3. Gerar o novo PLAN.md e apresentar o Implementation Plan focado em Render.
