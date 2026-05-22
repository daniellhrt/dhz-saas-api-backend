/**
 * Propósito: Comunicação com o banco de dados para a entidade ServiceItem.
 * Responsabilidade: Prover acesso aos dados garantindo a restrição de tenant e filtros de status.
 * Papel na Arquitetura: Domain / Repository.
 */
package br.com.dht.apibackend.domain.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, UUID> {

    // Validação de unicidade na hora de criar um serviço
    boolean existsByTenantIdAndNameIgnoreCase(String tenantId, String name);

    // Lista apenas os serviços que a barbearia está prestando no momento
    Page<ServiceItem> findAllByTenantIdAndActiveTrue(String tenantId, Pageable pageable);

    // Busca um serviço específico da barbearia
    Optional<ServiceItem> findByIdAndTenantId(UUID id, String tenantId);
}