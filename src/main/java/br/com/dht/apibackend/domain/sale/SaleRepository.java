/**
 * Propósito: Repositório para persistência de dados de vendas (comandas).
 * Responsabilidade: Intermediar chamadas ao banco com isolamento por Tenant.
 * Papel na Arquitetura: Infrastructure / Repository.
 */
package br.com.dht.apibackend.domain.sale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
    Page<Sale> findByTenantId(String tenantId, Pageable pageable);
    Optional<Sale> findByIdAndTenantId(UUID id, String tenantId);
    Optional<Sale> findByAppointmentIdAndTenantId(UUID appointmentId, String tenantId);
}
