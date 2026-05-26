/**
 * Propósito: Mapeamento ORM da transação de venda (comanda/checkout).
 * Responsabilidade: Registrar os dados financeiros finais de um agendamento.
 * Papel na Arquitetura: Domain / Entity.
 */
package br.com.dht.apibackend.domain.sale;

import br.com.dht.apibackend.domain.appointment.Appointment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @OneToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Setter
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Setter
    @Column(precision = 10, scale = 2)
    private BigDecimal discount;

    @Setter
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Setter
    @Column(name = "payment_method")
    private String paymentMethod;

    @Setter
    @Column
    private String notes;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Sale(String tenantId, Appointment appointment, BigDecimal subtotal, BigDecimal discount, BigDecimal total, String paymentMethod, String notes) {
        this.tenantId = tenantId;
        this.appointment = appointment;
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }
}
