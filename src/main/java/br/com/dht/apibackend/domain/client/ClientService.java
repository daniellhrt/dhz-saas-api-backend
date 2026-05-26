/**
 * Propósito: Regras de negócio da gestão de clientes.
 * Responsabilidade: Orquestrar validações e persistência isolada por tenant.
 * Papel na Arquitetura: Domain / Service.
 */
package br.com.dht.apibackend.domain.client;

import br.com.dht.apibackend.config.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public ClientDTO.Response createClient(ClientDTO.Request request) {
        String currentTenant = TenantContext.getTenantId();

        if (currentTenant == null) {
            log.warn("Tentativa de criar cliente sem contexto de tenant");
            throw new IllegalStateException("Acesso negado: Contexto de tenant ausente.");
        }

        // Regra de Negócio: Cliente não pode ter e-mail duplicado na mesma barbearia
        if (clientRepository.findByEmailAndTenantId(request.email(), currentTenant).isPresent()) {
            log.warn("Tentativa de criar cliente com e-mail duplicado {} no tenant {}", request.email(), currentTenant);
            throw new IllegalArgumentException("Já existe um cliente com este e-mail nesta barbearia.");
        }

        Client newClient = new Client(currentTenant, request.name(), request.email(), request.phone());
        newClient.setCpf(request.cpf());
        newClient.setBirthDate(request.birthDate());
        newClient.setNotes(request.notes());

        Client savedClient = clientRepository.save(newClient);

        log.info("Cliente criado {} ({}) no tenant {}", savedClient.getId(), request.email(), currentTenant);
        return ClientDTO.Response.fromEntity(savedClient);
    }

    @Transactional
    public ClientDTO.Response updateClient(java.util.UUID id, ClientDTO.Request request) {
        String currentTenant = TenantContext.getTenantId();

        Client client = clientRepository.findByIdAndTenantId(id, currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));

        // Regra de Negócio: E-mail não pode ser de outro cliente
        clientRepository.findByEmailAndTenantId(request.email(), currentTenant)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(client.getId())) {
                        log.warn("Tentativa de atualizar cliente {} com e-mail duplicado {} no tenant {}", id, request.email(), currentTenant);
                        throw new IllegalArgumentException("Já existe outro cliente com este e-mail.");
                    }
                });

        client.setName(request.name());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client.setCpf(request.cpf());
        client.setBirthDate(request.birthDate());
        client.setNotes(request.notes());

        Client updatedClient = clientRepository.save(client);

        log.info("Cliente atualizado {} no tenant {}", id, currentTenant);
        return ClientDTO.Response.fromEntity(updatedClient);
    }

    @Transactional(readOnly = true)
    public Page<ClientDTO.Response> listAllClients(Pageable pageable) {
        String currentTenant = TenantContext.getTenantId();

        return clientRepository.findAllByTenantId(currentTenant, pageable)
                .map(ClientDTO.Response::fromEntity);
    }
}