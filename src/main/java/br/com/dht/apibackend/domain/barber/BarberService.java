/**
 * Propósito: Regras de negócio da gestão de barbeiros.
 * Responsabilidade: Orquestrar criação, listagem, atualização e remoção de barbeiros com controle de papel (role) e isolamento por tenant.
 * Papel na Arquitetura: Domain / Service.
 */
package br.com.dht.apibackend.domain.barber;

import br.com.dht.apibackend.config.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarberService {

    private final BarberRepository barberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public BarberDTO.Response registerAdmin(BarberDTO.RegisterRequest request) {
        if (barberRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Tentativa de registro com e-mail duplicado: {}", request.email());
            throw new IllegalArgumentException("Já existe um barbeiro com este e-mail.");
        }

        String tenantId = UUID.randomUUID().toString();
        Barber barber = new Barber(tenantId, request.name(), request.email(),
                passwordEncoder.encode(request.password()), BarberRole.ADMIN);

        Barber saved = barberRepository.save(barber);
        log.info("Novo ADMIN registrado: {} em tenant {}", request.email(), tenantId);
        return BarberDTO.Response.fromEntity(saved);
    }

    @Transactional
    public BarberDTO.Response createBarber(BarberDTO.CreateRequest request) {
        String currentTenant = TenantContext.getTenantId();
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        assertAdminRole(currentEmail);

        if (barberRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Tentativa de criar barbeiro com e-mail duplicado: {} no tenant {}", request.email(), currentTenant);
            throw new IllegalArgumentException("Já existe um barbeiro com este e-mail.");
        }

        Barber barber = new Barber(currentTenant, request.name(), request.email(),
                passwordEncoder.encode(request.password()), BarberRole.USER);

        Barber saved = barberRepository.save(barber);
        log.info("Novo barbeiro USER criado por {}: {} no tenant {}", currentEmail, request.email(), currentTenant);
        return BarberDTO.Response.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<BarberDTO.Response> listAllBarbers(Pageable pageable) {
        String currentTenant = TenantContext.getTenantId();
        return barberRepository.findAllByTenantId(currentTenant, pageable)
                .map(BarberDTO.Response::fromEntity);
    }

    @Transactional
    public BarberDTO.Response updateBarber(UUID id, BarberDTO.UpdateRequest request) {
        String currentTenant = TenantContext.getTenantId();

        Barber barber = barberRepository.findByIdAndTenantId(id, currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado."));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!barber.getEmail().equals(currentEmail)) {
            log.warn("Tentativa de atualizar dados de outro barbeiro: {} tentou alterar {} no tenant {}", currentEmail, barber.getId(), currentTenant);
            throw new IllegalArgumentException("Você só pode atualizar seus próprios dados.");
        }

        if (request.name() != null) {
            barber.setName(request.name());
        }
        if (request.email() != null) {
            if (!request.email().equals(barber.getEmail()) && barberRepository.findByEmail(request.email()).isPresent()) {
                log.warn("Tentativa de atualizar para e-mail duplicado: {}", request.email());
                throw new IllegalArgumentException("Já existe um barbeiro com este e-mail.");
            }
            barber.setEmail(request.email());
        }

        barberRepository.save(barber);
        log.info("Barbeiro atualizado: {} no tenant {}", currentEmail, currentTenant);
        return BarberDTO.Response.fromEntity(barber);
    }

    @Transactional
    public void deleteBarber(UUID id) {
        String currentTenant = TenantContext.getTenantId();

        Barber barber = barberRepository.findByIdAndTenantId(id, currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado."));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        assertAdminRole(currentEmail);

        if (barber.getEmail().equals(currentEmail)) {
            log.warn("ADMIN {} tentou deletar a si mesmo no tenant {}", currentEmail, currentTenant);
            throw new IllegalArgumentException("Você não pode deletar a si mesmo.");
        }

        barberRepository.delete(barber);
        log.info("Barbeiro deletado por {}: {} no tenant {}", currentEmail, barber.getEmail(), currentTenant);
    }

    private void assertAdminRole(String email) {
        Barber current = barberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (current.getRole() != BarberRole.ADMIN) {
            log.warn("Acesso negado: {} tentou operação de ADMIN", email);
            throw new SecurityException("Acesso negado: apenas ADMIN pode realizar esta operação.");
        }
    }
}
