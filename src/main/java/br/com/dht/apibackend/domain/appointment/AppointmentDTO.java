/**
 * Propósito: Objetos de transferência para agendamentos.
 * Papel na Arquitetura: Domain / DTO.
 */
package br.com.dht.apibackend.domain.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentDTO {

    public record Request(
            @NotNull(message = "O ID do cliente é obrigatório") UUID clientId,
            @NotNull(message = "O ID do serviço é obrigatório") UUID serviceItemId,
            @NotNull(message = "A data e hora de início são obrigatórias") @Future(message = "O agendamento deve ser no futuro") LocalDateTime startTime
    ) {}

    public record Response(
            UUID id,
            String clientName,
            String serviceName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status
    ) {
        public static Response fromEntity(Appointment app) {
            return new Response(
                    app.getId(),
                    app.getClient().getName(),
                    app.getServiceItem().getName(),
                    app.getStartTime(),
                    app.getEndTime(),
                    app.getStatus().name()
            );
        }
    }
}