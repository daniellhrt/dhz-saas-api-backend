# DHZ SaaS API Backend

Sistema de backend multilocatário (multi-tenant) para gestão de barbearias, construído com Spring Boot 3, Java 21, Spring Security (JWT) e PostgreSQL.

## Arquitetura

- **Multitenancy**: Isolamento lógico por linha (row-level) via coluna `tenant_id`. O tenant atual é extraído do JWT e validado a cada requisição.
- **Segurança**: Autenticação stateless com JWT.
- **Dados**: PostgreSQL orquestrado com Flyway migrations.

## Como Executar Localmente

Para rodar este projeto, você precisará do **Docker** e **Docker Compose** instalados.

### 1. Configurar as Variáveis de Ambiente

Não mantemos segredos no controle de versão. Copie o arquivo de exemplo para criar o seu arquivo `.env` local:

```bash
cp .env.example .env
```

Abra o arquivo `.env` e preencha as variáveis, se necessário (o padrão já funciona para ambiente de desenvolvimento local).

### 2. Iniciar a Aplicação

Este comando irá baixar/construir as imagens e iniciar tanto o banco de dados PostgreSQL quanto a API Java.

```bash
docker compose up --build
```

A API estará disponível em `http://localhost:8080`.

## Contato e Desenvolvimento

Para testes locais e debug via IDE, lembre-se de configurar a integração com o banco subindo o serviço `postgres` independentemente:
```bash
docker compose up postgres -d
```
E certifique-se de configurar as variáveis de ambiente na sua IDE (como `JWT_SECRET`, `POSTGRES_USER` e `POSTGRES_PASSWORD`).
