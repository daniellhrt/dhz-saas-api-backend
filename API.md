# DHZ SaaS API — Guia de Integração Frontend

> Documentação completa para consumir a API do sistema de gestão de barbearias.
> Base URL: `http://localhost:8080` (dev) / `http://api.exemplo.com` (prod)

---

## Índice

1. [Setup e Execução](#1-setup-e-execução)
2. [Autenticação (JWT)](#2-autenticação-jwt)
3. [Multitenancy (Isolamento por Barbearia)](#3-multitenancy-isolamento-por-barbearia)
4. [Endpoints](#4-endpoints)
   - [Auth](#41-auth)
   - [Barbeiros](#42-barbeiros)
   - [Clientes](#43-clientes)
   - [Agendamentos](#44-agendamentos)
   - [Catálogo de Serviços](#45-catálogo-de-serviços)
5. [Formato de Erros](#5-formato-de-erros)
6. [Paginação](#6-paginação)
7. [Rate Limiting](#7-rate-limiting)
8. [Regras de Negócio Importantes](#8-regras-de-negócio-importantes)
9. [CORS](#9-cors)
10. [Swagger UI](#10-swagger-ui)

---

## 1. Setup e Execução

### Pré-requisitos
- Docker + Docker Compose
- Arquivo `.env` na raiz do projeto

### Passos

```bash
# 1. Criar arquivo de variáveis de ambiente
cp .env.example .env

# 2. Iniciar tudo (PostgreSQL, Redis, API)
docker compose up --build

# A API estará em http://localhost:8080
```

### Variáveis de Ambiente (.env)

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `JWT_SECRET` | Chave secreta para assinar tokens JWT (min 256 bits) | `minha-chave-super-segura-para-jwt-256bits` |
| `POSTGRES_USER` | Usuário do PostgreSQL | `saas_user` |
| `POSTGRES_PASSWORD` | Senha do PostgreSQL | `saas_pass` |
| `POSTGRES_DB` | Nome do banco | `saas_db` |
| `REDIS_HOST` | Host do Redis | `localhost` |

---

## 2. Autenticação (JWT)

### Fluxo

```
[Frontend]                    [API]
    |                            |
    |-- POST /auth/register ---->|  Cria conta (role: ADMIN)
    |<--- 201 + user data -------|
    |                            |
    |-- POST /auth/login ------->|  Gera token JWT
    |<--- 200 + token -----------|
    |                            |
    |-- GET /barbers ----------->|  Envia token no header
    |   Authorization: Bearer    |
    |<--- 200 + data ------------|
```

### Login

```
POST /api/v1/auth/login

Request:
{
  "email": "admin@barbearia.com",
  "password": "senha123"
}

Response 200:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer"
}
```

### Uso do Token

Todas as requisições autenticadas **devem** incluir:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### O que está dentro do JWT

O token contém 3 claims customizadas que o frontend **pode** ler (opcional, para UI):

| Claim | Valor | Exemplo |
|-------|-------|---------|
| `sub` (subject) | E-mail do barbeiro | `admin@barbearia.com` |
| `tenantId` | ID da barbearia (UUID) | `a1b2c3d4-...` |
| `role` | Papel do barbeiro | `ADMIN` ou `USER` |

O token expira em **24 horas** (configurável via `JWT_EXPIRATION_MS`).

### Registro (Admin)

```
POST /api/v1/auth/register

Request:
{
  "name": "Minha Barbearia",
  "email": "admin@barbearia.com",
  "password": "senha123"
}

Response 201:
{
  "id": "uuid",
  "name": "Minha Barbearia",
  "email": "admin@barbearia.com",
  "role": "ADMIN"
}
```

> ⚠ **Importante:** O primeiro barbeiro a se registrar cria automaticamente uma nova barbearia (tenant) e recebe papel **ADMIN** (dono).

---

## 3. Multitenancy (Isolamento por Barbearia)

### Como funciona

- Cada barbearia é um **tenant** identificado por um UUID único.
- Ao se registrar, o barbeiro ganha um `tenantId` novo.
- O `tenantId` é embutido no JWT e extraído automaticamente pela API.
- **Todas as operações** (clientes, agendamentos, serviços) são automaticamente isoladas por tenant.
- O frontend **não precisa** enviar `tenantId` em lugar nenhum — ele vem do token.

### Papéis (Roles)

| Role | Descrição | Permissões |
|------|-----------|------------|
| `ADMIN` | Dono da barbearia | Criar/remover barbeiros, tudo |
| `USER` | Funcionário | Operações do dia a dia |

O frontend pode usar a role para exibir/esconder funcionalidades:

```typescript
const role = decodedToken.role; // "ADMIN" | "USER"
const isAdmin = role === "ADMIN";
```

---

## 4. Endpoints

### 4.1 Auth

#### `POST /api/v1/auth/login`
Público. Retorna token JWT.

Request:
```json
{
  "email": "admin@barbearia.com",
  "password": "senha123"
}
```

Response 200:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer"
}
```

> ⚠ Este endpoint tem rate limiting: **5 requisições por minuto por IP**. Retorna HTTP 429 se excedido.

#### `POST /api/v1/auth/register`
Público. Cria o primeiro barbeiro (dono) de uma nova barbearia.

Request:
```json
{
  "name": "Nome do Barbeiro",
  "email": "email@exemplo.com",
  "password": "senha123"
}
```

Response 201:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Nome do Barbeiro",
  "email": "email@exemplo.com",
  "role": "ADMIN"
}
```

Erros:
- `400` — E-mail já existe

---

### 4.2 Barbeiros

#### `POST /api/v1/barbers` — Criar funcionário
Requer **ADMIN**. Cria um barbeiro com papel `USER`.

Request:
```json
{
  "name": "João Tesoura",
  "email": "joao@barbearia.com",
  "password": "senha123"
}
```

Response 201:
```json
{
  "id": "uuid",
  "name": "João Tesoura",
  "email": "joao@barbearia.com",
  "role": "USER"
}
```

Erros:
- `400` — E-mail já existe ou usuário não encontrado
- `403` — Quem não é ADMIN tentou criar

#### `GET /api/v1/barbers` — Listar barbeiros
Requer autenticação. Retorna apenas barbeiros da mesma barbearia.

Query params: `?page=0&size=20&sort=name,asc`

Response 200:
```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Minha Barbearia",
      "email": "admin@barbearia.com",
      "role": "ADMIN"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  "first": true,
  "last": true,
  "empty": false
}
```

#### `PUT /api/v1/barbers/{id}` — Atualizar próprio perfil
Requer autenticação. Só permite alterar os **próprios** dados.

Request (campos opcionais):
```json
{
  "name": "Novo Nome",
  "email": "novo@email.com"
}
```

Response 200:
```json
{
  "id": "uuid",
  "name": "Novo Nome",
  "email": "novo@email.com",
  "role": "ADMIN"
}
```

Erros:
- `400` — Tentativa de atualizar dados de outro barbeiro, ou e-mail já em uso

#### `DELETE /api/v1/barbers/{id}` — Remover barbeiro
Requer **ADMIN**. Não pode deletar a si mesmo.

Response: `204 No Content` (sem corpo)

Erros:
- `400` — ADMIN tentou deletar a si mesmo
- `403` — USER tentou deletar

---

### 4.3 Clientes

#### `POST /api/v1/clients` — Cadastrar cliente
Requer autenticação.

Request:
```json
{
  "name": "Carlos Cliente",
  "email": "carlos@email.com",
  "phone": "(11) 99999-8888"
}
```

Response 201:
```json
{
  "id": "uuid",
  "name": "Carlos Cliente",
  "email": "carlos@email.com",
  "phone": "(11) 99999-8888"
}
```

#### `GET /api/v1/clients` — Listar clientes
Requer autenticação. Isolado por tenant.

Query params: `?page=0&size=20`

Response 200:
```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Carlos Cliente",
      "email": "carlos@email.com",
      "phone": "(11) 99999-8888"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  "first": true,
  "last": true,
  "empty": false
}
```

---

### 4.4 Agendamentos

#### `POST /api/v1/appointments` — Criar agendamento
Requer autenticação. Valida double-booking (não permite horários conflitantes para o mesmo barbeiro).

Request:
```json
{
  "clientId": "uuid-do-cliente",
  "serviceItemId": "uuid-do-servico",
  "startTime": "2026-06-01T14:00:00"
}
```

Response 201:
```json
{
  "id": "uuid",
  "clientName": "Carlos Cliente",
  "serviceName": "Corte de Cabelo",
  "startTime": "2026-06-01T14:00:00",
  "endTime": "2026-06-01T14:30:00",
  "status": "PENDING",
  "cancelReason": null
}
```

> O `endTime` é calculado automaticamente somando `startTime` + `durationMinutes` do serviço.

Erros:
- `400` — Conflito de horário (double-booking), cliente/serviço não encontrado, ou data no passado

#### `PATCH /api/v1/appointments/{id}/confirm` — Confirmar
Requer autenticação. Só funciona se status for `PENDING`.

Response 200:
```json
{
  "id": "uuid",
  "clientName": "Carlos Cliente",
  "serviceName": "Corte de Cabelo",
  "startTime": "2026-06-01T14:00:00",
  "endTime": "2026-06-01T14:30:00",
  "status": "CONFIRMED",
  "cancelReason": null
}
```

#### `PATCH /api/v1/appointments/{id}/cancel` — Cancelar
Requer autenticação. Motivo opcional.

Request (opcional):
```json
{
  "reason": "Cliente não compareceu"
}
```

Response 200:
```json
{
  "id": "uuid",
  "clientName": "Carlos Cliente",
  "serviceName": "Corte de Cabelo",
  "startTime": "2026-06-01T14:00:00",
  "endTime": "2026-06-01T14:30:00",
  "status": "CANCELED",
  "cancelReason": "Cliente não compareceu"
}
```

#### `PATCH /api/v1/appointments/{id}/complete` — Concluir
Requer autenticação. Marca o agendamento como concluído.

Response 200:
```json
{
  "id": "uuid",
  "clientName": "Carlos Cliente",
  "serviceName": "Corte de Cabelo",
  "startTime": "2026-06-01T14:00:00",
  "endTime": "2026-06-01T14:30:00",
  "status": "COMPLETED",
  "cancelReason": null
}
```

### Máquina de Estados dos Agendamentos

```
PENDING ──→ CONFIRMED ──→ COMPLETED
    │                       
    └──→ CANCELED           
```

---

### 4.5 Catálogo de Serviços

#### `POST /api/v1/catalog` — Criar serviço
Requer autenticação.

Request:
```json
{
  "name": "Corte de Cabelo",
  "description": "Corte masculino com tesoura e máquina",
  "price": 45.00,
  "durationMinutes": 30
}
```

Response 201:
```json
{
  "id": "uuid",
  "name": "Corte de Cabelo",
  "description": "Corte masculino com tesoura e máquina",
  "price": 45.00,
  "durationMinutes": 30,
  "active": true
}
```

#### `GET /api/v1/catalog` — Listar serviços ativos
Requer autenticação. Retorna apenas serviços ativos do tenant.

Query params: `?page=0&size=20`

Response 200:
```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Corte de Cabelo",
      "description": "Corte masculino com tesoura e máquina",
      "price": 45.00,
      "durationMinutes": 30,
      "active": true
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  ...
}
```

#### `DELETE /api/v1/catalog/{id}` — Desativar serviço
Requer autenticação. Usa **desativação lógica** (soft delete) — o registro permanece no banco com `active: false`.

Response: `204 No Content`

---

## 5. Formato de Erros

Todos os erros seguem o mesmo formato:

```json
{
  "timestamp": "2026-05-22T14:30:00",
  "status": 400,
  "error": "Business Rule Violation",
  "message": "Já existe um barbeiro com este e-mail.",
  "path": "/api/v1/auth/register"
}
```

### Códigos HTTP e Categorias

| Status | `error` | Quando |
|--------|---------|--------|
| `400` | `Business Rule Violation` | Regra de negócio violada (e-mail duplicado, double-booking, etc.) |
| `400` | `Validation Error` | Dados inválidos no body (campos obrigatórios, formato de e-mail, etc.) |
| `401` | `Unauthorized` | Credenciais inválidas no login |
| `403` | — | Acesso negado (USER tentou operação de ADMIN) |
| `429` | — | Rate limit excedido (login) |
| `500` | `Internal Server Error` | Erro inesperado no servidor |

### Exemplo de Validation Error (`400`)

```json
{
  "timestamp": "2026-05-22T14:30:00",
  "status": 400,
  "error": "Validation Error",
  "message": "O nome é obrigatório, A senha deve ter no mínimo 6 caracteres",
  "path": "/api/v1/auth/register"
}
```

---

## 6. Paginação

Endpoints de listagem aceitam parâmetros de query:

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `page` | int | `0` | Número da página (zero-indexed) |
| `size` | int | `20` | Itens por página |
| `sort` | string | — | Campo e direção, ex: `sort=name,asc` |

### Estrutura da Resposta Paginada

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": true, "unsorted": false, "empty": false },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 3,
  "totalElements": 45,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0,
  "sort": { "sorted": true, "unsorted": false, "empty": false },
  "numberOfElements": 20,
  "empty": false
}
```

---

## 7. Rate Limiting

O endpoint `POST /api/v1/auth/login` tem limite de **5 requisições por minuto por IP**.

Quando excedido, retorna HTTP 429:
```
Too many requests. Please try again later.
```

Se você estiver testando e receber 429, aguarde 1 minuto antes de tentar novamente.

---

## 8. Regras de Negócio Importantes

### Barberia (Tenant)
- Cada registro cria uma nova barbearia (não existe endpoint para "entrar em barbearia existente")
- O ADMIN pode cadastrar funcionários (USER) que operam na mesma barbearia
- Funcionários **não podem** cadastrar/remover outros barbeiros

### Agendamentos
- `startTime` deve ser no futuro (validado pelo backend)
- `endTime` é calculado automaticamente = `startTime` + `durationMinutes` do serviço
- **Double-blocking prevention**: não é possível criar 2 agendamentos no mesmo horário+barbeiro
- Transições de status: `PENDING → CONFIRMED → COMPLETED` ou `PENDING → CANCELED` a qualquer momento

### Catálogo
- Exclusão é **lógica** (soft delete): o recurso fica com `active: false`
- A listagem `GET /catalog` retorna **apenas** serviços ativos

---

## 9. CORS

⚠ **ATENÇÃO:** O backend atualmente **não possui configuração CORS**.

Se seu frontend roda em um domínio/porta diferente (ex: `localhost:5173` para Vite, `localhost:3000` para Next.js), você precisa configurar CORS.

### Solução rápida (criar classe de configuração)

Adicione o seguinte arquivo no backend em `src/main/java/br/com/dht/apibackend/config/CorsConfig.java`:

```java
package br.com.dht.apibackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

Ajuste os `allowedOrigins` conforme seu ambiente de desenvolvimento.

### Solução para desenvolvimento (desabilitar CORS no navegador)

Alternativamente, pode desabilitar CORS no navegador durante o desenvolvimento:

- **Chrome**: `chrome.exe --disable-web-security --user-data-dir="C:\temp\chrome-dev"`
- Ou use uma extensão como "CORS Unblock"

---

## 10. Swagger UI

A API possui documentação interativa via Swagger UI (Springdoc OpenAPI).

Com a API rodando, acesse:

```
http://localhost:8080/swagger-ui/index.html
```

Ou obtenha o schema OpenAPI em JSON:

```
http://localhost:8080/v3/api-docs
```

---

## Resumo de Endpoints

| Método | Caminho | Auth | Descrição |
|--------|---------|------|-----------|
| POST | `/api/v1/auth/register` | ❌ Público | Criar conta (ADMIN) |
| POST | `/api/v1/auth/login` | ❌ Público | Login (retorna JWT) |
| POST | `/api/v1/barbers` | ✅ ADMIN | Criar funcionário |
| GET | `/api/v1/barbers` | ✅ Autenticado | Listar barbeiros |
| PUT | `/api/v1/barbers/{id}` | ✅ Autenticado | Atualizar próprio perfil |
| DELETE | `/api/v1/barbers/{id}` | ✅ ADMIN | Remover barbeiro |
| POST | `/api/v1/clients` | ✅ Autenticado | Cadastrar cliente |
| GET | `/api/v1/clients` | ✅ Autenticado | Listar clientes |
| POST | `/api/v1/appointments` | ✅ Autenticado | Criar agendamento |
| PATCH | `/api/v1/appointments/{id}/confirm` | ✅ Autenticado | Confirmar |
| PATCH | `/api/v1/appointments/{id}/cancel` | ✅ Autenticado | Cancelar |
| PATCH | `/api/v1/appointments/{id}/complete` | ✅ Autenticado | Concluir |
| POST | `/api/v1/catalog` | ✅ Autenticado | Criar serviço |
| GET | `/api/v1/catalog` | ✅ Autenticado | Listar serviços |
| DELETE | `/api/v1/catalog/{id}` | ✅ Autenticado | Desativar serviço |

---

## Exemplo de Integração (fetch API)

```typescript
const BASE_URL = "http://localhost:8080/api/v1";

// 1. Login
async function login(email: string, password: string) {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });
  const data = await res.json();
  localStorage.setItem("token", data.token);
  return data;
}

// 2. Requisição autenticada
async function apiCall(path: string, options: RequestInit = {}) {
  const token = localStorage.getItem("token");
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
      ...options.headers,
    },
  });
  if (!res.ok) {
    const error = await res.json();
    throw new Error(error.message);
  }
  return res.status === 204 ? null : await res.json();
}

// 3. Exemplos de uso
const loginData = await login("admin@barbearia.com", "senha123");
const barbers = await apiCall("/barbers?page=0&size=10");
const clients = await apiCall("/clients", {
  method: "POST",
  body: JSON.stringify({ name: "Cliente", email: "cli@email.com", phone: "11999998888" }),
});
const appointments = await apiCall("/appointments", {
  method: "POST",
  body: JSON.stringify({ clientId, serviceItemId, startTime: "2026-06-01T14:00:00" }),
});
```

---

*Documentação gerada em 2026-05-22 — DHZ SaaS API Backend v1.1*
