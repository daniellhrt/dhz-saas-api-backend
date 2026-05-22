/**
 * Propósito: Comunicação com o banco de dados para a entidade Barber.
 * Responsabilidade: Prover acesso ao repositório de barbeiros para autenticação.
 * Papel na Arquitetura: Domain / Repository.
 */
package br.com.dht.apibackend.domain.barber;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarberRepository extends JpaRepository<Barber, UUID> {

    Optional<Barber> findByEmail(String email);

    Optional<Barber> findByIdAndTenantId(UUID id, String tenantId);

    Page<Barber> findAllByTenantId(String tenantId, Pageable pageable);

    boolean existsByIdAndTenantId(UUID id, String tenantId);
}