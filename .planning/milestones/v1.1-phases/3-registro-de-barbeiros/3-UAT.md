---
status: complete
phase: 3-registro-de-barbeiros
source: PLAN.md
started: 2026-05-22T14:23:00-03:00
updated: 2026-05-22T14:30:00-03:00
---

## Current Test

[testing complete]

## Tests

### 1. Auto-Registro (Admin)
expected: POST /api/v1/auth/register com name, email e password retorna 201 Created. Retorna os dados do barbeiro criado com role=ADMIN e id gerado.
result: pass

### 2. ADMIN cria USER
expected: POST /api/v1/barbers (com token de ADMIN) com name, email e password retorna 201 Created. Barbeiro criado tem role=USER.
result: pass

### 3. USER não pode criar barbeiro
expected: POST /api/v1/barbers (com token de USER) retorna 403 Forbidden ou SecurityException.
result: pass

### 4. Listar barbeiros do tenant
expected: GET /api/v1/barbers retorna 200 OK com lista paginada de barbeiros do mesmo tenant.
result: pass

### 5. Atualizar próprio perfil
expected: PUT /api/v1/barbers/{id} (do próprio barbeiro) retorna 200 OK com dados atualizados.
result: pass

### 6. Não pode atualizar perfil de outro
expected: PUT /api/v1/barbers/{id} (de outro barbeiro) retorna 400 Bad Request com mensagem "Você só pode atualizar seus próprios dados."
result: pass

### 7. ADMIN deleta USER
expected: DELETE /api/v1/barbers/{id} (ADMIN deletando USER) retorna 204 No Content.
result: pass

### 8. ADMIN não pode deletar a si mesmo
expected: DELETE /api/v1/barbers/{id} (ADMIN tentando deletar a si mesmo) retorna 400 Bad Request.
result: pass

### 9. USER não pode deletar
expected: DELETE /api/v1/barbers/{id} (USER tentando deletar) retorna 403 Forbidden.
result: pass

### 10. Duplicidade de e-mail
expected: POST /api/v1/auth/register ou POST /api/v1/barbers com e-mail já existente retorna erro.
result: pass

### 11. Testes automatizados passam
expected: `mvn test` para BarberServiceTest e BarberControllerTest — 16 testes, 0 falhas.
result: pass

## Summary

total: 11
passed: 11
issues: 0
pending: 0
skipped: 0

## Gaps

[none yet]
