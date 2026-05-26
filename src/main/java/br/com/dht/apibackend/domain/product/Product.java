/**
 * Propósito: Mapeamento ORM do produto físico vendido na barbearia.
 * Responsabilidade: Representar características como nome, preço, custo e estoque.
 * Papel na Arquitetura: Domain / Entity.
 */
package br.com.dht.apibackend.domain.product;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Product {

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
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Setter
    @Column(nullable = false)
    private Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Product(String tenantId, String name, String description, BigDecimal price, Integer stockQuantity) {
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.active = true;
    }
}
