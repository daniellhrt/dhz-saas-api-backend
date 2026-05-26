/**
 * Propósito: Mapeamento ORM dos itens que compõem uma venda.
 * Responsabilidade: Registrar serviços e produtos vendidos, quantidade e preço unitário no momento da venda.
 * Papel na Arquitetura: Domain / Entity.
 */
package br.com.dht.apibackend.domain.sale;

import br.com.dht.apibackend.domain.catalog.ServiceItem;
import br.com.dht.apibackend.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sale_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_item_id")
    private ServiceItem serviceItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    public SaleItem(ServiceItem serviceItem, Product product, Integer quantity, BigDecimal unitPrice) {
        this.serviceItem = serviceItem;
        this.product = product;
        this.quantity = quantity != null ? quantity : 1;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        this.totalPrice = this.unitPrice.multiply(new BigDecimal(this.quantity));
    }
}
