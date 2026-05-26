# Phase 7 Research: Deploy no Google Cloud Platform (GCP)

## 1. Cloud Provider Setup (GCP)
- **Compute (Google Cloud Run)**: Excelente para aplicações Spring Boot conteinerizadas. É serverless (escala para zero) e cobra apenas pelo tempo de execução. O Cloud Run usará nosso `Dockerfile`. Ele expõe nativamente a variável de ambiente `PORT` (por padrão 8080).
- **PostgreSQL (Google Cloud SQL)**: Serviço gerenciado. Para conectar a partir do Cloud Run, usa-se IP Privado ou Unix Sockets. Se usarmos IP Privado, a conexão é padrão JDBC (Host, Porta, DB, User, Pass).
- **Redis (Google Cloud Memorystore)**: Serviço gerenciado. Fornece um IP e Porta. Geralmente, não utiliza senha na camada básica dentro da mesma VPC (rede privada).

## 2. Environment Variables Mapping (GCP)
Diferente do Railway ou Render que forçam nomes de variáveis específicas (`PGHOST` ou `DATABASE_URL`), no GCP o desenvolvedor configura as variáveis livremente no console do Cloud Run.
Portanto, a abordagem ideal para GCP é a mais "Pura" e agnóstica possível no `application-prod.yml`:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`

## 3. Ajustes no Código
- O `RedisConfig.java` que alteramos na época do Railway adicionando suporte a senha ainda é perfeitamente válido para o GCP. Se o Memorystore não usar senha, basta a variável vir vazia e ele fará o builder sem senha perfeitamente.
- O `application-prod.yml` deve ser limpo das variáveis do Railway (`PGHOST`, `PGPORT`, etc) e voltar a usar as chaves padrão do projeto.
- O `CorsConfig.java` não precisa de mudanças, pois já foi feito para ler `CORS_ALLOWED_ORIGINS`, que você configurará no painel do Cloud Run com a URL da Vercel.

## Conclusão
O deploy no GCP (Cloud Run + Cloud SQL + Memorystore) é a opção mais profissional e escalável. O código backend necessitará de uma limpeza nas variáveis hardcoded do Railway no YAML, garantindo que o YAML fique 100% genérico.
