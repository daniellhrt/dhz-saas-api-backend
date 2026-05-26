/**
 * Propósito: Data Transfer Object para transitar dados de produtos entre o frontend e a API.
 * Responsabilidade: Mapear os campos de input/output do Product, validar dados obrigatórios.
 * Papel na Arquitetura: Domain / DTO.
 */
package br.com.dht.apibackend.domain.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductDTO {
    private UUID id;
    
    @NotBlank(message = "O nome do produto é obrigatório")
    private String name;
    
    private String description;
    
    @NotNull(message = "O preço do produto é obrigatório")
    @PositiveOrZero(message = "O preço não pode ser negativo")
    private BigDecimal price;
    
    @NotNull(message = "A quantidade em estoque é obrigatória")
    @PositiveOrZero(message = "O estoque não pode ser negativo")
    private Integer stockQuantity;
    
    private Boolean active;
    
    private LocalDateTime createdAt;
    
    public ProductDTO() {}
    
    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.active = product.getActive();
        this.createdAt = product.getCreatedAt();
    }
}
