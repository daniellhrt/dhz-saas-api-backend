/**
 * Propósito: Regras de negócio do Catálogo de Serviços.
 * Responsabilidade: Orquestrar validações e manipulação de catálogo isolado por tenant.
 * Papel na Arquitetura: Domain / Service.
 */
package br.com.dht.apibackend.domain.catalog;

import br.com.dht.apibackend.config.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final ServiceItemRepository repository;

    @Transactional
    public ServiceItemDTO.Response createService(ServiceItemDTO.Request request) {
        String currentTenant = TenantContext.getTenantId();

        if (repository.existsByTenantIdAndNameIgnoreCase(currentTenant, request.name())) {
            log.warn("Tentativa de criar serviço com nome duplicado {} no tenant {}", request.name(), currentTenant);
            throw new IllegalArgumentException("Já existe um serviço com este nome cadastrado.");
        }

        ServiceItem newItem = new ServiceItem(
                currentTenant,
                request.name(),
                request.description(),
                request.price(),
                request.durationMinutes()
        );

        ServiceItem saved = repository.save(newItem);
        log.info("Serviço criado {} {} no tenant {}", saved.getId(), request.name(), currentTenant);
        return ServiceItemDTO.Response.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<ServiceItemDTO.Response> listActiveServices(Pageable pageable) {
        return repository.findAllByTenantIdAndActiveTrue(TenantContext.getTenantId(), pageable)
                .map(ServiceItemDTO.Response::fromEntity);
    }

    @Transactional
    public ServiceItemDTO.Response updateService(UUID id, ServiceItemDTO.Request request) {
        String currentTenant = TenantContext.getTenantId();

        ServiceItem item = repository.findByIdAndTenantId(id, currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));

        // Validar nome duplicado
        if (!item.getName().equalsIgnoreCase(request.name()) && 
            repository.existsByTenantIdAndNameIgnoreCase(currentTenant, request.name())) {
            log.warn("Tentativa de atualizar serviço {} com nome duplicado {} no tenant {}", id, request.name(), currentTenant);
            throw new IllegalArgumentException("Já existe outro serviço com este nome cadastrado.");
        }

        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setDurationMinutes(request.durationMinutes());

        repository.save(item);
        log.info("Serviço atualizado {} no tenant {}", id, currentTenant);
        return ServiceItemDTO.Response.fromEntity(item);
    }

    @Transactional
    public void deactivateService(UUID id) {
        String currentTenant = TenantContext.getTenantId();
        ServiceItem item = repository.findByIdAndTenantId(id, currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));

        item.setActive(false);
        log.info("Serviço desativado {} no tenant {}", id, currentTenant);
        // Não é necessário chamar repository.save() pois a entidade está "Managed" pelo Hibernate
    }
}