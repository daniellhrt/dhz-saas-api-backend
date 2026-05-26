/**
 * Propósito: Data Transfer Objects para transitar dados de vendas entre o frontend e a API.
 * Responsabilidade: Encapsular dados da venda e itens para payload e response.
 * Papel na Arquitetura: Domain / DTO.
 */
package br.com.dht.apibackend.domain.sale;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class SaleDTO {
    private UUID id;
    private UUID appointmentId;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private String paymentMethod;
    private String notes;
    private LocalDateTime createdAt;
    private List<SaleItemDTO> items;

    public SaleDTO() {}

    public SaleDTO(Sale sale) {
        this.id = sale.getId();
        this.appointmentId = sale.getAppointment() != null ? sale.getAppointment().getId() : null;
        this.subtotal = sale.getSubtotal();
        this.discount = sale.getDiscount();
        this.total = sale.getTotal();
        this.paymentMethod = sale.getPaymentMethod();
        this.notes = sale.getNotes();
        this.createdAt = sale.getCreatedAt();
        if (sale.getItems() != null) {
            this.items = sale.getItems().stream().map(SaleItemDTO::new).collect(Collectors.toList());
        }
    }

    @Data
    public static class SaleItemDTO {
        private UUID id;
        private UUID serviceItemId;
        private UUID productId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public SaleItemDTO() {}

        public SaleItemDTO(SaleItem item) {
            this.id = item.getId();
            this.serviceItemId = item.getServiceItem() != null ? item.getServiceItem().getId() : null;
            this.productId = item.getProduct() != null ? item.getProduct().getId() : null;
            this.quantity = item.getQuantity();
            this.unitPrice = item.getUnitPrice();
            this.totalPrice = item.getTotalPrice();
        }
    }
}
