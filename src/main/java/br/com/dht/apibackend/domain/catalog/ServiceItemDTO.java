/**
 * Propósito: Objetos de transferência para o Catálogo de Serviços.
 * Papel na Arquitetura: Domain / DTO.
 */
package br.com.dht.apibackend.domain.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class ServiceItemDTO {

    public record Request(
            @NotBlank(message = "O nome do serviço é obrigatório") String name,
            String description,
            @NotNull(message = "O preço é obrigatório") @DecimalMin(value = "0.01", inclusive = true, message = "O preço deve ser maior que zero") BigDecimal price,
            @NotNull(message = "A duração é obrigatória") @Min(value = 15, message = "A duração mínima é de 15 minutos") Integer durationMinutes
    ) {}

    public record Response(
            UUID id,
            String name,
            String description,
            BigDecimal price,
            Integer durationMinutes,
            Boolean active
    ) {
        public static Response fromEntity(ServiceItem item) {
            return new Response(
                    item.getId(),
                    item.getName(),
                    item.getDescription(),
                    item.getPrice(),
                    item.getDurationMinutes(),
                    item.getActive()
            );
        }
    }
}