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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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
            log.warn("Tentativa de agendar serviço inativo {} no tenant {}", serviceItem.getId(), currentTenant);
            throw new IllegalStateException("Não é possível agendar um serviço inativo.");
        }

        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = startTime.plusMinutes(serviceItem.getDurationMinutes());

        if (appointmentRepository.hasOverlappingAppointment(currentTenant, startTime, endTime)) {
            log.warn("Double-booking detectado no tenant {} entre {} e {}", currentTenant, startTime, endTime);
            throw new IllegalStateException("Já existe um agendamento conflitante neste horário.");
        }

        Appointment newAppointment = new Appointment(currentTenant, client, serviceItem, startTime, endTime);
        Appointment savedAppointment = appointmentRepository.save(newAppointment);

        log.info("Agendamento criado {} para cliente {} no tenant {} início: {}", savedAppointment.getId(), client.getId(), currentTenant, startTime);
        return AppointmentDTO.Response.fromEntity(savedAppointment);
    }

    @Transactional
    public AppointmentDTO.Response blockSchedule(AppointmentDTO.BlockRequest request) {
        String currentTenant = TenantContext.getTenantId();
        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = request.endTime();

        if (appointmentRepository.hasOverlappingAppointment(currentTenant, startTime, endTime)) {
            log.warn("Double-booking detectado (Bloqueio) no tenant {} entre {} e {}", currentTenant, startTime, endTime);
            throw new IllegalStateException("Já existe um agendamento ou bloqueio conflitante neste horário.");
        }

        Appointment newBlock = new Appointment(currentTenant, startTime, endTime, request.notes());
        Appointment savedBlock = appointmentRepository.save(newBlock);

        log.info("Bloqueio criado {} no tenant {} início: {}", savedBlock.getId(), currentTenant, startTime);
        return AppointmentDTO.Response.fromEntity(savedBlock);
    }

    @Transactional
    public AppointmentDTO.Response confirmAppointment(UUID id) {
        Appointment appointment = findAppointmentForTenant(id);

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Apenas agendamentos pendentes podem ser confirmados.");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        log.info("Agendamento confirmado: {} no tenant {}", id, TenantContext.getTenantId());
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
        log.info("Agendamento cancelado: {} motivo: {} no tenant {}", id, reason, TenantContext.getTenantId());
        return AppointmentDTO.Response.fromEntity(appointment);
    }

    @Transactional
    public AppointmentDTO.Response startAppointment(UUID id) {
        Appointment appointment = findAppointmentForTenant(id);

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Apenas agendamentos pendentes ou confirmados podem ser iniciados.");
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        log.info("Agendamento em andamento: {} no tenant {}", id, TenantContext.getTenantId());
        return AppointmentDTO.Response.fromEntity(appointment);
    }

    @Transactional
    public AppointmentDTO.Response completeAppointment(UUID id) {
        Appointment appointment = findAppointmentForTenant(id);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Apenas agendamentos confirmados ou em andamento podem ser concluídos.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setFinalizedAt(LocalDateTime.now());
        log.info("Agendamento concluído: {} no tenant {}", id, TenantContext.getTenantId());
        return AppointmentDTO.Response.fromEntity(appointment);
    }

    @Transactional
    public AppointmentDTO.Response revertAppointment(UUID id) {
        Appointment appointment = findAppointmentForTenant(id);

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Apenas agendamentos concluídos podem ser revertidos.");
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment.setFinalizedAt(null);
        log.info("Agendamento revertido para em andamento: {} no tenant {}", id, TenantContext.getTenantId());
        return AppointmentDTO.Response.fromEntity(appointment);
    }

    private Appointment findAppointmentForTenant(UUID id) {
        String currentTenant = TenantContext.getTenantId();
        return appointmentRepository.findByIdAndTenantId(id, currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
    }

    public Page<AppointmentDTO.Response> listAllAppointments(Pageable pageable) {
        String currentTenant = TenantContext.getTenantId();
        return appointmentRepository.findAllByTenantId(currentTenant, pageable)
                .map(AppointmentDTO.Response::fromEntity);
    }
}