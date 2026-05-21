/**
 * Propósito: Mapeamento ORM do serviço oferecido pela barbearia.
 * Responsabilidade: Representar características como preço e duração de um serviço (Corte, Barba, etc).
 * Papel na Arquitetura: Domain / Entity.
 */
package br.com.dht.apibackend.domain.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column
    private String description;

    @Setter
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Setter
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Setter
    @Column(nullable = false)
    private Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ServiceItem(String tenantId, String name, String description, BigDecimal price, Integer durationMinutes) {
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.active = true; // Todo serviço nasce ativo
    }
}