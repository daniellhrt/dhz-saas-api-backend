/**
 * Propósito: Regras de negócio da gestão de barbeiros.
 * Responsabilidade: Orquestrar criação, listagem, atualização e remoção de barbeiros com controle de papel (role) e isolamento por tenant.
 * Papel na Arquitetura: Domain / Service.
 */
package br.com.dht.apibackend.domain.barber;

import br.com.dht.apibackend.config.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BarberService {

    private final BarberRepository barberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public BarberDTO.Response registerAdmin(BarberDTO.RegisterRequest request) {
        if (barberRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Já existe um barbeiro com este e-mail.");
        }

        String tenantId = UUID.randomUUID().toString();
        Barber barber = new Barber(tenantId, request.name(), request.email(),
                passwordEncoder.encode(request.password()), BarberRole.ADMIN);

        return BarberDTO.Response.fromEntity(barberRepository.save(barber));
    }

    @Transactional
    public BarberDTO.Response createBarber(BarberDTO.CreateRequest request) {
        String currentTenant = TenantContext.getTenantId();
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        assertAdminRole(currentEmail);

        if (barberRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Já existe um barbeiro com este e-mail.");
        }

        Barber barber = new Barber(currentTenant, request.name(), request.email(),
                passwordEncoder.encode(request.password()), BarberRole.USER);

        return BarberDTO.Response.fromEntity(barberRepository.save(barber));
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
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Barber barber = barberRepository.findByIdAndTenantId(id, currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado."));

        if (!barber.getEmail().equals(currentEmail)) {
            throw new IllegalArgumentException("Você só pode atualizar seus próprios dados.");
        }

        if (request.name() != null) {
            barber.setName(request.name());
        }
        if (request.email() != null) {
            if (!request.email().equals(barber.getEmail()) && barberRepository.findByEmail(request.email()).isPresent()) {
                throw new IllegalArgumentException("Já existe um barbeiro com este e-mail.");
            }
            barber.setEmail(request.email());
        }

        return BarberDTO.Response.fromEntity(barberRepository.save(barber));
    }

    @Transactional
    public void deleteBarber(UUID id) {
        String currentTenant = TenantContext.getTenantId();
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Barber barber = barberRepository.findByIdAndTenantId(id, currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado."));

        assertAdminRole(currentEmail);

        if (barber.getEmail().equals(currentEmail)) {
            throw new IllegalArgumentException("Você não pode deletar a si mesmo.");
        }

        barberRepository.delete(barber);
    }

    private void assertAdminRole(String email) {
        Barber current = barberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (current.getRole() != BarberRole.ADMIN) {
            throw new SecurityException("Acesso negado: apenas ADMIN pode realizar esta operação.");
        }
    }
}
