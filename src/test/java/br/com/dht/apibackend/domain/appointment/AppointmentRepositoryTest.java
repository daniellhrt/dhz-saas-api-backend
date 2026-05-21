package br.com.dht.apibackend.domain.appointment;

import br.com.dht.apibackend.domain.catalog.ServiceItem;
import br.com.dht.apibackend.domain.catalog.ServiceItemRepository;
import br.com.dht.apibackend.domain.client.Client;
import br.com.dht.apibackend.domain.client.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Test
    void testHasOverlappingAppointment() {
        String tenantId = "tenant-123";
        String differentTenant = "tenant-456";

        Client client = new Client(tenantId, "Client 1", "email@test.com", "123");
        clientRepository.save(client);

        ServiceItem service = new ServiceItem(tenantId, "Service 1", "Desc", new BigDecimal("50"), 60);
        serviceItemRepository.save(service);

        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime end = start.plusMinutes(60);

        Appointment appointment = new Appointment(tenantId, client, service, start, end);
        appointmentRepository.save(appointment);

        // Act & Assert
        // Exatamente o mesmo horário no mesmo tenant -> deve conflitar
        assertTrue(appointmentRepository.hasOverlappingAppointment(tenantId, start, end));

        // Parcialmente sobreposto no mesmo tenant -> deve conflitar
        assertTrue(appointmentRepository.hasOverlappingAppointment(tenantId, start.plusMinutes(30), end.plusMinutes(30)));

        // Horário diferente no mesmo tenant -> não deve conflitar
        assertFalse(appointmentRepository.hasOverlappingAppointment(tenantId, end, end.plusMinutes(60)));

        // Exatamente o mesmo horário, MAS em outro tenant -> não deve conflitar
        assertFalse(appointmentRepository.hasOverlappingAppointment(differentTenant, start, end));
    }
}
