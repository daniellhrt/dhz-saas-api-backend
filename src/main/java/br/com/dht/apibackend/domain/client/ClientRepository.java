package br.com.dht.apibackend.domain.client;

/**
 * Propósito: Interface de comunicação com o banco de dados para a entidade Client.
 * Responsabilidade: Prover métodos de acesso a dados com isolamento estrito por tenant.
 * Papel na Arquitetura: Domain / Repository.
 */


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    // NUNCA use métodos globais sem tenantId.

    // Busca um cliente específico de uma barbearia (usado para edição/detalhes)
    Optional<Client> findByIdAndTenantId(UUID id, String tenantId);

    // Busca cliente por email dentro de um tenant (usado para validar duplicidade na criação)
    Optional<Client> findByEmailAndTenantId(String email, String tenantId);

    // Lista todos os clientes de uma barbearia específica com paginação
    Page<Client> findAllByTenantId(String tenantId, Pageable pageable);

    // Verifica se o cliente existe antes de deletar
    boolean existsByIdAndTenantId(UUID id, String tenantId);
}