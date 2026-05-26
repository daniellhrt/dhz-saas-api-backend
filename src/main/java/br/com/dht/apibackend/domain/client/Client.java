/**
 * Propósito: Mapeamento ORM da tabela de clientes do SaaS.
 * Responsabilidade: Representar o estado de um cliente e garantir que campos imutáveis não sejam alterados.
 * Papel na Arquitetura: Domain / Entity.
 */
package br.com.dht.apibackend.domain.client;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // OBRIGATÓRIO para o Hibernate, protegido para evitar uso incorreto
@EqualsAndHashCode(of = "id")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter
    private UUID id;

    // CRÍTICO: updatable = false garante que um cliente NUNCA mude de barbearia após criado
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    private String email;

    @Setter
    @Column(nullable = false)
    private String phone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "cpf", length = 20)
    private String cpf;

    @Setter
    @Column(name = "birth_date")
    private java.time.LocalDate birthDate;

    @Setter
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Construtor sem o ID e sem Data, usados apenas para criação de novos registros
    public Client(String tenantId, String name, String email, String phone) {
        this.tenantId = tenantId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}