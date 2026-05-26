/**
 * Propósito: Regras de negócio relacionadas a vendas/comandas.
 * Responsabilidade: Criar vendas, processar itens (serviços e produtos), atualizar estoque e finalizar agendamentos.
 * Papel na Arquitetura: Domain / Service.
 */
package br.com.dht.apibackend.domain.sale;

import br.com.dht.apibackend.config.TenantContext;
import br.com.dht.apibackend.domain.appointment.Appointment;
import br.com.dht.apibackend.domain.appointment.AppointmentRepository;
import br.com.dht.apibackend.domain.appointment.AppointmentStatus;
import br.com.dht.apibackend.domain.catalog.ServiceItem;
import br.com.dht.apibackend.domain.catalog.ServiceItemRepository;
import br.com.dht.apibackend.domain.product.Product;
import br.com.dht.apibackend.domain.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final ProductRepository productRepository;

    public Page<SaleDTO> list(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        return saleRepository.findByTenantId(tenantId, pageable)
                .map(SaleDTO::new);
    }

    public SaleDTO getByAppointmentId(UUID appointmentId) {
        String tenantId = TenantContext.getTenantId();
        return saleRepository.findByAppointmentIdAndTenantId(appointmentId, tenantId)
                .map(SaleDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada para o agendamento"));
    }

    @Transactional
    public SaleDTO create(SaleDTO dto) {
        String tenantId = TenantContext.getTenantId();

        Appointment appointment = null;
        if (dto.getAppointmentId() != null) {
            appointment = appointmentRepository.findByIdAndTenantId(dto.getAppointmentId(), tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));
            
            // Marca o agendamento como finalizado (pois gerou venda)
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }

        Sale sale = new Sale(
                tenantId,
                appointment,
                dto.getSubtotal(),
                dto.getDiscount(),
                dto.getTotal(),
                dto.getPaymentMethod(),
                dto.getNotes()
        );

        if (dto.getItems() != null) {
            for (SaleDTO.SaleItemDTO itemDto : dto.getItems()) {
                ServiceItem service = null;
                Product product = null;

                if (itemDto.getServiceItemId() != null) {
                    service = serviceItemRepository.findByIdAndTenantId(itemDto.getServiceItemId(), tenantId)
                            .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado: " + itemDto.getServiceItemId()));
                }

                if (itemDto.getProductId() != null) {
                    product = productRepository.findByIdAndTenantId(itemDto.getProductId(), tenantId)
                            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDto.getProductId()));
                    
                    // Atualizar estoque
                    int newStock = product.getStockQuantity() - (itemDto.getQuantity() != null ? itemDto.getQuantity() : 1);
                    if (newStock < 0) {
                        throw new IllegalArgumentException("Estoque insuficiente para o produto: " + product.getName());
                    }
                    product.setStockQuantity(newStock);
                    productRepository.save(product);
                }

                SaleItem saleItem = new SaleItem(service, product, itemDto.getQuantity(), itemDto.getUnitPrice());
                sale.addItem(saleItem);
            }
        }

        return new SaleDTO(saleRepository.save(sale));
    }

    @Transactional
    public void deleteSaleByAppointmentId(UUID appointmentId) {
        String tenantId = TenantContext.getTenantId();
        saleRepository.findByAppointmentIdAndTenantId(appointmentId, tenantId).ifPresent(sale -> {
            // Restore stock for products
            for (SaleItem item : sale.getItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
            saleRepository.delete(sale);
        });
    }
}
