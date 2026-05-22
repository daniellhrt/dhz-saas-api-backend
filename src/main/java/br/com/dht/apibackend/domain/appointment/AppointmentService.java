/**
 * Propósito: Regras de negócio de agendamento.
 * Responsabilidade: Orquestrar validações anti-IDOR, cálculo de término e detecção de conflitos.
 * Papel na Arquitetura: Domain / Service.
 */
package br.com.dht.apibackend.domain.appointment;

import br.com.dht.apibackend.config.TenantContext;
import br.com.dht.apibackend.domain.catalog.ServiceItem;
import br.com.dht.apibackend.domain.catalog.ServiceItemRepository;
import br.com.dht.apibackend.domain.client.Client;
import br.com.dht.apibackend.domain.client.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final ServiceItemRepository serviceItemRepository;

    @Transactional
    public AppointmentDTO.Response scheduleAppointment(AppointmentDTO.Request request) {
        String currentTenant = TenantContext.getTenantId();

        Client client = clientRepository.findByIdAndTenantId(request.clientId(), currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));

        ServiceItem serviceItem = serviceItemRepository.findByIdAndTenantId(request.serviceItemId(), currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));

        if (!serviceItem.getActive()) {
            throw new IllegalStateException("Não é possível agendar um serviço inativo.");
        }

        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = startTime.plusMinutes(serviceItem.getDurationMinutes());

        if (appointmentRepository.hasOverlappingAppointment(currentTenant, startTime, endTime)) {
            throw new IllegalStateException("Já existe um agendamento conflitante neste horário.");
        }

        Appointment newAppointment = new Appointment(currentTenant, client, serviceItem, startTime, endTime);
        Appointment savedAppointment = appointmentRepository.save(newAppointment);

        return AppointmentDTO.Response.fromEntity(savedAppointment);
    }

    @Transactional
    public AppointmentDTO.Response confirmAppointment(UUID id) {
        Appointment appointment = findAppointmentForTenant(id);

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Apenas agendamentos pendentes podem ser confirmados.");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return AppointmentDTO.Response.fromEntity(appointment);
    }

    @Transactional
    public AppointmentDTO.Response cancelAppointment(UUID id, String reason) {
        Appointment appointment = findAppointmentForTenant(id);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Agendamentos concluídos não podem ser cancelados.");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new IllegalStateException("Agendamento já está cancelado.");
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setCancelReason(reason);
        return AppointmentDTO.Response.fromEntity(appointment);
    }

    @Transactional
    public AppointmentDTO.Response completeAppointment(UUID id) {
        Appointment appointment = findAppointmentForTenant(id);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Apenas agendamentos confirmados podem ser concluídos.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return AppointmentDTO.Response.fromEntity(appointment);
    }

    private Appointment findAppointmentForTenant(UUID id) {
        String currentTenant = TenantContext.getTenantId();
        return appointmentRepository.findByIdAndTenantId(id, currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
    }
}