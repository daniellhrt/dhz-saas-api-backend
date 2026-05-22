/**
 * Propósito: Regras de negócio do Catálogo de Serviços.
 * Responsabilidade: Orquestrar validações e manipulação de catálogo isolado por tenant.
 * Papel na Arquitetura: Domain / Service.
 */
package br.com.dht.apibackend.domain.catalog;

import br.com.dht.apibackend.config.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ServiceItemRepository repository;

    @Transactional
    public ServiceItemDTO.Response createService(ServiceItemDTO.Request request) {
        String currentTenant = TenantContext.getTenantId();

        if (repository.existsByTenantIdAndNameIgnoreCase(currentTenant, request.name())) {
            throw new IllegalArgumentException("Já existe um serviço com este nome cadastrado.");
        }

        ServiceItem newItem = new ServiceItem(
                currentTenant,
                request.name(),
                request.description(),
                request.price(),
                request.durationMinutes()
        );

        return ServiceItemDTO.Response.fromEntity(repository.save(newItem));
    }

    @Transactional(readOnly = true)
    public Page<ServiceItemDTO.Response> listActiveServices(Pageable pageable) {
        return repository.findAllByTenantIdAndActiveTrue(TenantContext.getTenantId(), pageable)
                .map(ServiceItemDTO.Response::fromEntity);
    }

    @Transactional
    public void deactivateService(UUID id) {
        ServiceItem item = repository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));

        item.setActive(false);
        // Não é necessário chamar repository.save() pois a entidade está "Managed" pelo Hibernate
    }
}