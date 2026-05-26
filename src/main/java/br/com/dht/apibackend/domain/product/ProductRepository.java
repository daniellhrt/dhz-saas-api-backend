/**
 * Propósito: Repositório para persistência de dados de produtos físicos.
 * Responsabilidade: Intermediar as chamadas ao banco de dados utilizando Spring Data JPA com isolamento por Tenant.
 * Papel na Arquitetura: Infrastructure / Repository.
 */
package br.com.dht.apibackend.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByTenantId(String tenantId, Pageable pageable);
    Optional<Product> findByIdAndTenantId(UUID id, String tenantId);
}
