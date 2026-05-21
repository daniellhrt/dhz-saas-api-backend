# Research Summary: Infra, Docker & Testing (Spring Boot 3)

## Stack Recommendations
- **Testing:** JUnit 5 + Mockito + MockMvc (unit/controller), Testcontainers (integration) ou H2 se a infra for puramente local/simples.
- **Docker:** `Dockerfile` multi-stage (builder + runtime com OpenJDK 21 slim).
- **Environment:** Dotenv ou perfis do Spring (`application-prod.yml`, `application-dev.yml`) injetando variáveis de ambiente (`${JWT_SECRET}`).
- **CI/CD:** GitHub Actions (workflow de build, test e lint).

## Table Stakes (Obrigatório)
- Segredos fora do código fonte.
- Testes cobrindo fluxos críticos de negócio (Service) e entradas da API (Controller).
- Containers que funcionem identicamente na máquina local e em produção.

## Watch Out For (Pitfalls)
- **Vazamento no Git:** Esquecer de ignorar o arquivo `.env`.
- **Falsos positivos em testes:** Mockar o banco inteiro ao invés de usar H2/Testcontainers para repositórios.
- **Tamanho da imagem Docker:** Não usar multi-stage build, resultando em imagens de +500MB com código fonte e Maven dentro.
