/**
 * Propósito: Mapeamento ORM da tabela de barbeiros.
 * Responsabilidade: Representar o usuário dono da barbearia.
 * Papel na Arquitetura: Domain / Entity.
 */
package br.com.dht.apibackend.domain.barber;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "barbers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Barber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BarberRole role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Barber(String tenantId, String name, String email, String password, BarberRole role) {
        this.tenantId = tenantId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}