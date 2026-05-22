---
phase: 3
slug: 3-registro-de-barbeiros
status: verified
threats_open: 0
asvs_level: 1
created: 2026-05-22
---

# Phase 3 — Security: Registro de Barbeiros

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| HTTP → Controller | External requests enter the API | Name, email, password (sensitive) |
| Controller → Service | Business logic layer | BarberDTO records |
| Service → Repository | Data access layer | Barber entity (with BCrypt hash) |
| Service → SecurityContext | Authentication context | Principal email, tenant ID |

---

## Threat Register

| Threat ID | Category | Component | Disposition | Mitigation | Status |
|-----------|----------|-----------|-------------|------------|--------|
| T-3-01 | Spoofing | POST /api/v1/auth/register | mitigate | Senha hasheada com BCrypt (`PasswordEncoder`) | closed |
| T-3-02 | Spoofing | POST /api/v1/barbers | mitigate | Requer autenticação JWT + role ADMIN (`assertAdminRole`) | closed |
| T-3-03 | Tampering | PUT /api/v1/barbers/{id} | mitigate | Só permite atualizar próprio perfil (valida email do token == email do barbeiro) | closed |
| T-3-04 | Tampering | DELETE /api/v1/barbers/{id} | mitigate | Só ADMIN pode deletar (`assertAdminRole`) e não pode deletar a si mesmo | closed |
| T-3-05 | Information Disclosure | GET /api/v1/barbers | mitigate | Isolamento por tenant (`findAllByTenantId`) | closed |
| T-3-06 | Information Disclosure | BarberDTO.Response | mitigate | Password nunca incluído na resposta (`fromEntity` não mapeia password) | closed |
| T-3-07 | Elevation of Privilege | BarberService.createBarber | mitigate | Barbeiros criados via este endpoint sempre recebem role=USER (não configurável) | closed |
| T-3-08 | Tampering | Unique email constraint | mitigate | Validação de e-mail único no tenant antes de salvar | closed |
| T-3-09 | Spoofing | Login flow | mitigate | Autenticação via JWT com email+senha (preserva fase anterior) | closed |

*Status: closed · open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-3-01 | N/A | Sem logging de auditoria para operações sensíveis (criação/deleção de barbeiros). Aceito para MVP — será endereçado em fase futura de audit trail. | GSD | 2026-05-22 |
| AR-3-02 | N/A | Self-registration público cria ADMIN automaticamente (sem confirmação de e-mail). Aceito para MVP — ambiente controlado com barbeiros conhecidos. | GSD | 2026-05-22 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-05-22 | 9 | 9 | 0 | opencode |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-05-22
