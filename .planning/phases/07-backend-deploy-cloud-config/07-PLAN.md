# Phase 7: Backend Deploy & Cloud Config (GCP Pivot)

## 1. Goal
Modificar as configurações de infraestrutura para preparar o deploy no Google Cloud Platform (Cloud Run para a API, Cloud SQL para o Postgres e Memorystore para o Redis). O foco será deixar os arquivos de configuração 100% genéricos, pois o GCP permite que o desenvolvedor defina as chaves livremente no console.

## 2. Approach
- **PostgreSQL:** Simplificar o `application-prod.yml` removendo as variáveis prefixadas (`PGHOST`, `PGUSER`, etc) do Railway e retornando para as variáveis agnósticas (`DB_HOST`, `DB_PORT`, `POSTGRES_USER`, etc).
- **Redis:** Manter o `RedisConfig.java` como está, pois a lógica de injetar a senha de forma opcional funcionará bem no Memorystore (que muitas vezes sequer usa senha na mesma VPC). Retornar as propriedades do `application-prod.yml` para apenas usar `REDIS_HOST` e `REDIS_PORT`.
- O CORS já está dinâmico e pronto.

## 3. Tasks

- [ ] **Task 1: Atualizar application-prod.yml**
  - Mudar o mapping de datasource de volta para: `url: jdbc:postgresql://${DB_HOST:postgres}:${DB_PORT:5432}/${POSTGRES_DB}`.
  - Mudar username/password de volta para `${POSTGRES_USER}` e `${POSTGRES_PASSWORD}`.
  - Isso garante que a aplicação não dependa de nenhuma estrutura proprietária.
  - Para o Redis, remover as variáveis `REDISHOST`, e voltar a consumir `${REDIS_HOST}` puramente.

- [ ] **Task 2: Atualizar application.yml**
  - Fazer a mesma limpeza nas propriedades do Redis nos profiles `dev` e `prod`.

- [ ] **Task 3: Validar a suíte de testes**
  - Executar os testes automatizados para garantir integridade.

## 4. Verification
1. `.\mvnw.cmd clean test` não deverá falhar e validará a regressão das variáveis.
