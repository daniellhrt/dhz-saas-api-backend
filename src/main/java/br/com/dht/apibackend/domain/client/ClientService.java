/**
 * Propósito: Regras de negócio da gestão de clientes.
 * Responsabilidade: Orquestrar validações e persistência isolada por tenant.
 * Papel na Arquitetura: Domain / Service.
 */
package br.com.dht.apibackend.domain.client;

import br.com.dht.apibackend.config.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public ClientDTO.Response createClient(ClientDTO.Request request) {
        String currentTenant = TenantContext.getTenantId();

        if (currentTenant == null) {
            throw new IllegalStateException("Acesso negado: Contexto de tenant ausente.");
        }

        // Regra de Negócio: Cliente não pode ter e-mail duplicado na mesma barbearia
        if (clientRepository.findByEmailAndTenantId(request.email(), currentTenant).isPresent()) {
            throw new IllegalArgumentException("Já existe um cliente com este e-mail nesta barbearia.");
        }

        Client newClient = new Client(currentTenant, request.name(), request.email(), request.phone());
        Client savedClient = clientRepository.save(newClient);

        return ClientDTO.Response.fromEntity(savedClient);
    }

    @Transactional(readOnly = true)
    public List<ClientDTO.Response> listAllClients() {
        String currentTenant = TenantContext.getTenantId();

        return clientRepository.findAllByTenantId(currentTenant)
                .stream()
                .map(ClientDTO.Response::fromEntity)
                .toList();
    }
}