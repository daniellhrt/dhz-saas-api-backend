/**
 * Propósito: Objetos de transferência para agendamentos.
 * Papel na Arquitetura: Domain / DTO.
 */
package br.com.dht.apibackend.domain.appointment;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentDTO {

    public record Request(
            @NotNull(message = "O ID do cliente é obrigatório") UUID clientId,
            @NotNull(message = "O ID do serviço é obrigatório") UUID serviceItemId,
            @NotNull(message = "A data e hora de início são obrigatórias") LocalDateTime startTime
    ) {}

    public record CancelRequest(
            String reason
    ) {}

    public record BlockRequest(
            @NotNull(message = "A data de início é obrigatória") LocalDateTime startTime,
            @NotNull(message = "A data de fim é obrigatória") LocalDateTime endTime,
            String notes
    ) {}

    public record Response(
            UUID id,
            String clientName,
            String clientPhone,
            String clientEmail,
            String clientCpf,
            java.time.LocalDate clientBirthDate,
            String serviceName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status,
            String cancelReason
    ) {
        public static Response fromEntity(Appointment app) {
            String clientName = app.getClient() != null ? app.getClient().getName() : "Bloqueio";
            String clientPhone = app.getClient() != null ? app.getClient().getPhone() : null;
            String clientEmail = app.getClient() != null ? app.getClient().getEmail() : null;
            String clientCpf = app.getClient() != null ? app.getClient().getCpf() : null;
            java.time.LocalDate clientBirthDate = app.getClient() != null ? app.getClient().getBirthDate() : null;
            String serviceName = app.getServiceItem() != null ? app.getServiceItem().getName() : "Horário Bloqueado";
            return new Response(
                    app.getId(),
                    clientName,
                    clientPhone,
                    clientEmail,
                    clientCpf,
                    clientBirthDate,
                    serviceName,
                    app.getStartTime(),
                    app.getEndTime(),
                    app.getStatus().name(),
                    app.getCancelReason()
            );
        }
    }
}