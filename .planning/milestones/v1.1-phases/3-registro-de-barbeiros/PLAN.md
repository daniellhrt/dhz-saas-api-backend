# Phase 3: Registro de Barbeiros

## Goal
Implementar CRUD completo de barbeiros com dois níveis de acesso: **ADMIN** (dono da barbearia) e **USER** (funcionário). Hoje só existe autenticação com login.

## Regras de Negócio
- **ADMIN** = dono da barbearia. Criado via `POST /api/v1/auth/register` (self-registration). Pode cadastrar/deletar barbeiros USER.
- **USER** = funcionário. Criado pelo ADMIN via `POST /api/v1/barbers`. Pode logar e fazer operações do dia a dia (agendar, fechar comanda, cadastrar clientes) — escopo futuro.
- Ambos os papéis podem listar barbeiros do tenant e atualizar os próprios dados.
- Nenhum papel pode deletar a si mesmo.

## Requirements
- **REG-01**: Barbeiro pode criar conta com nome, email e senha (cria ADMIN)
- **REG-02**: Barbeiro pode listar todos os barbeiros da sua barbearia (tenant)
- **REG-03**: Barbeiro pode atualizar seus próprios dados
- **REG-04**: ADMIN pode deletar barbeiros USER (nunca a si mesmo)

## Tasks

### Wave 0 — BarberRole enum
**Dependencies:** Nenhuma

- [ ] `barber/BarberRole.java` — enum `ADMIN` e `USER`
- [ ] `barber/Barber.java` — adicionar campo `role` (não nulo, default ADMIN)

### Wave 1 — BarberDTO
**Dependencies:** Wave 0

- [ ] `barber/BarberDTO.java` — record `RegisterRequest` com `name`, `email`, `password` + validações (para self-registration)
- [ ] `barber/BarberDTO.java` — record `CreateRequest` com `name`, `email`, `password` + validações (ADMIN cria USER)
- [ ] `barber/BarberDTO.java` — record `UpdateRequest` com `name`, `email` (opcionais para PATCH ou PUT parcial)
- [ ] `barber/BarberDTO.java` — record `Response` com `id`, `name`, `email`, `role` + `fromEntity()`

### Wave 2 — BarberRepository
**Dependencies:** Nenhuma

- [ ] `barber/BarberRepository.java` — adicionar `findByIdAndTenantId(UUID, String)`
- [ ] `barber/BarberRepository.java` — adicionar `findAllByTenantId(String, Pageable)`
- [ ] `barber/BarberRepository.java` — adicionar `existsByIdAndTenantId(UUID, String)`

### Wave 3 — BarberService
**Dependencies:** Wave 1, Wave 2

- [ ] `barber/BarberService.java` — `registerAdmin()`: cria ADMIN (primeiro barbeiro do tenant, self-registration)
- [ ] `barber/BarberService.java` — `createBarber()`: só ADMIN pode criar USER, valida e-mail único no tenant, hash BCrypt
- [ ] `barber/BarberService.java` — `listAllBarbers()`: lista paginado por tenant (ambos os papéis)
- [ ] `barber/BarberService.java` — `updateBarber()`: atualiza apenas próprios dados (valida id+tenant+email dono)
- [ ] `barber/BarberService.java` — `deleteBarber()`: só ADMIN pode deletar, não pode deletar a si mesmo

### Wave 4 — BarberController
**Dependencies:** Wave 3

- [ ] `barber/BarberController.java` — `POST /api/v1/barbers` → 201 Created (cria USER, requer ADMIN)
- [ ] `barber/BarberController.java` — `GET /api/v1/barbers` → 200 OK + Page (ambos os papéis)
- [ ] `barber/BarberController.java` — `PUT /api/v1/barbers/{id}` → 200 OK (só próprio dado)
- [ ] `barber/BarberController.java` — `DELETE /api/v1/barbers/{id}` → 204 No Content (só ADMIN, não self)

### Wave 5 — AuthController (registration)
**Dependencies:** Wave 3

- [ ] `security/AuthController.java` — `POST /api/v1/auth/register` → 201 Created (cria ADMIN, sem auth)

### Wave 6 — Migration
**Dependencies:** Nenhuma

- [ ] `V3__add_role_to_barbers.sql` — `ALTER TABLE barbers ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ADMIN'`

### Wave 7 — Testes
**Dependencies:** Wave 3, Wave 4, Wave 5

- [ ] `barber/BarberServiceTest.java` — testar registerAdmin, createBarber (admin vs user role), list, update, delete
- [ ] `barber/BarberControllerTest.java` — testar endpoints via MockMvc com diferentes papéis

## Verification
1. Compilação sem erros: `mvn compile`
2. Testes passam: `mvn test`
3. POST /api/v1/auth/register retorna 201 com barbeiro ADMIN criado (senha hasheada)
4. POST /api/v1/barbers retorna 201 com barbeiro USER (requer token ADMIN)
5. POST /api/v1/barbers retorna 403 se token é de USER
6. GET /api/v1/barbers retorna apenas barbeiros do tenant logado
7. PUT /api/v1/barbers/{id} retorna 403 se tenta atualizar dados de outro barbeiro
8. DELETE /api/v1/barbers/{id} retorna 204 se ADMIN deleta USER
9. DELETE /api/v1/barbers/{id} retorna 400 se ADMIN tenta deletar a si mesmo
10. DELETE /api/v1/barbers/{id} retorna 403 se USER tenta deletar
