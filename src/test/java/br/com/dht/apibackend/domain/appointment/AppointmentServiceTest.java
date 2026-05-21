package br.com.dht.apibackend.domain.appointment;

import br.com.dht.apibackend.config.TenantContext;
import br.com.dht.apibackend.domain.catalog.ServiceItem;
import br.com.dht.apibackend.domain.catalog.ServiceItemRepository;
import br.com.dht.apibackend.domain.client.Client;
import br.com.dht.apibackend.domain.client.ClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ServiceItemRepository serviceItemRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private final String TENANT_ID = "tenant-test-123";

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldScheduleAppointmentSuccessfully() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        
        AppointmentDTO.Request request = new AppointmentDTO.Request(clientId, serviceId, startTime);

        Client mockClient = new Client(TENANT_ID, "John Doe", "john@test.com", "123456789");
        ServiceItem mockService = new ServiceItem(TENANT_ID, "Corte", "Corte simples", new BigDecimal("50.0"), 30);
        
        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.of(mockClient));
        when(serviceItemRepository.findByIdAndTenantId(serviceId, TENANT_ID)).thenReturn(Optional.of(mockService));
        when(appointmentRepository.hasOverlappingAppointment(eq(TENANT_ID), eq(startTime), any(LocalDateTime.class))).thenReturn(false);

        Appointment savedAppointment = new Appointment(TENANT_ID, mockClient, mockService, startTime, startTime.plusMinutes(30));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        // Act
        AppointmentDTO.Response response = appointmentService.scheduleAppointment(request);

        // Assert
        assertNotNull(response);
        assertEquals("John Doe", response.clientName());
        assertEquals("Corte", response.serviceName());
        assertEquals(AppointmentStatus.PENDING.name(), response.status());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowExceptionWhenClientNotFoundInTenant() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        AppointmentDTO.Request request = new AppointmentDTO.Request(clientId, UUID.randomUUID(), LocalDateTime.now().plusDays(1));
        
        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.scheduleAppointment(request);
        });
        assertEquals("Cliente não encontrado.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenServiceIsInactive() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        
        AppointmentDTO.Request request = new AppointmentDTO.Request(clientId, serviceId, startTime);

        Client mockClient = new Client(TENANT_ID, "John Doe", "john@test.com", "123456789");
        ServiceItem mockService = new ServiceItem(TENANT_ID, "Corte", "Corte", new BigDecimal("50"), 30);
        mockService.setActive(false); // Inativo
        
        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.of(mockClient));
        when(serviceItemRepository.findByIdAndTenantId(serviceId, TENANT_ID)).thenReturn(Optional.of(mockService));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            appointmentService.scheduleAppointment(request);
        });
        assertEquals("Não é possível agendar um serviço inativo.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionOnDoubleBooking() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        
        AppointmentDTO.Request request = new AppointmentDTO.Request(clientId, serviceId, startTime);

        Client mockClient = new Client(TENANT_ID, "John Doe", "john@test.com", "123456789");
        ServiceItem mockService = new ServiceItem(TENANT_ID, "Corte", "Corte", new BigDecimal("50"), 30);
        
        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.of(mockClient));
        when(serviceItemRepository.findByIdAndTenantId(serviceId, TENANT_ID)).thenReturn(Optional.of(mockService));
        when(appointmentRepository.hasOverlappingAppointment(eq(TENANT_ID), eq(startTime), any(LocalDateTime.class))).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            appointmentService.scheduleAppointment(request);
        });
        assertEquals("Já existe um agendamento conflitante neste horário.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }
}
