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

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final ServiceItemRepository serviceItemRepository;

    @Transactional
    public AppointmentDTO.Response scheduleAppointment(AppointmentDTO.Request request) {
        String currentTenant = TenantContext.getTenantId();

        // 1. Validação Anti-IDOR: O cliente pertence a esta barbearia?
        Client client = clientRepository.findByIdAndTenantId(request.clientId(), currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));

        // 2. Validação Anti-IDOR: O serviço pertence a esta barbearia e está ativo?
        ServiceItem serviceItem = serviceItemRepository.findByIdAndTenantId(request.serviceItemId(), currentTenant)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));

        if (!serviceItem.getActive()) {
            throw new IllegalStateException("Não é possível agendar um serviço inativo.");
        }

        // 3. Calcula o horário de término baseado na duração do serviço
        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = startTime.plusMinutes(serviceItem.getDurationMinutes());

        // 4. Validação de Conflito de Horário (Double-Booking)
        if (appointmentRepository.hasOverlappingAppointment(currentTenant, startTime, endTime)) {
            throw new IllegalStateException("Já existe um agendamento conflitante neste horário.");
        }

        // 5. Persiste a entidade
        Appointment newAppointment = new Appointment(currentTenant, client, serviceItem, startTime, endTime);
        Appointment savedAppointment = appointmentRepository.save(newAppointment);

        return AppointmentDTO.Response.fromEntity(savedAppointment);
    }
}