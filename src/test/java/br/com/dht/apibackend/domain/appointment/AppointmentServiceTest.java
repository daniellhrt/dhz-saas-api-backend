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

    private Client mockClient;
    private ServiceItem mockService;
    private Appointment pendingAppointment;
    private Appointment confirmedAppointment;
    private Appointment completedAppointment;
    private Appointment canceledAppointment;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);

        mockClient = new Client(TENANT_ID, "John Doe", "john@test.com", "123456789");
        mockService = new ServiceItem(TENANT_ID, "Corte", "Corte simples", new BigDecimal("50.0"), 30);
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusMinutes(30);

        pendingAppointment = new Appointment(TENANT_ID, mockClient, mockService, startTime, endTime);
        confirmedAppointment = new Appointment(TENANT_ID, mockClient, mockService, startTime, endTime);
        confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);
        completedAppointment = new Appointment(TENANT_ID, mockClient, mockService, startTime, endTime);
        completedAppointment.setStatus(AppointmentStatus.COMPLETED);
        canceledAppointment = new Appointment(TENANT_ID, mockClient, mockService, startTime, endTime);
        canceledAppointment.setStatus(AppointmentStatus.CANCELED);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldScheduleAppointmentSuccessfully() {
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        AppointmentDTO.Request request = new AppointmentDTO.Request(clientId, serviceId, startTime);

        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.of(mockClient));
        when(serviceItemRepository.findByIdAndTenantId(serviceId, TENANT_ID)).thenReturn(Optional.of(mockService));
        when(appointmentRepository.hasOverlappingAppointment(eq(TENANT_ID), eq(startTime), any(LocalDateTime.class))).thenReturn(false);

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(pendingAppointment);

        AppointmentDTO.Response response = appointmentService.scheduleAppointment(request);

        assertNotNull(response);
        assertEquals("John Doe", response.clientName());
        assertEquals(AppointmentStatus.PENDING.name(), response.status());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowExceptionWhenClientNotFoundInTenant() {
        UUID clientId = UUID.randomUUID();
        AppointmentDTO.Request request = new AppointmentDTO.Request(clientId, UUID.randomUUID(), LocalDateTime.now().plusDays(1));

        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                appointmentService.scheduleAppointment(request));
        assertEquals("Cliente não encontrado.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenServiceIsInactive() {
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        AppointmentDTO.Request request = new AppointmentDTO.Request(clientId, serviceId, startTime);

        mockService.setActive(false);

        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.of(mockClient));
        when(serviceItemRepository.findByIdAndTenantId(serviceId, TENANT_ID)).thenReturn(Optional.of(mockService));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                appointmentService.scheduleAppointment(request));
        assertEquals("Não é possível agendar um serviço inativo.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionOnDoubleBooking() {
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        AppointmentDTO.Request request = new AppointmentDTO.Request(clientId, serviceId, startTime);

        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.of(mockClient));
        when(serviceItemRepository.findByIdAndTenantId(serviceId, TENANT_ID)).thenReturn(Optional.of(mockService));
        when(appointmentRepository.hasOverlappingAppointment(eq(TENANT_ID), eq(startTime), any(LocalDateTime.class))).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                appointmentService.scheduleAppointment(request));
        assertEquals("Já existe um agendamento conflitante neste horário.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldConfirmPendingAppointment() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.of(pendingAppointment));

        AppointmentDTO.Response response = appointmentService.confirmAppointment(id);

        assertEquals(AppointmentStatus.CONFIRMED.name(), response.status());
        verify(appointmentRepository).findByIdAndTenantId(id, TENANT_ID);
    }

    @Test
    void shouldThrowExceptionWhenConfirmNonPending() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.of(confirmedAppointment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                appointmentService.confirmAppointment(id));
        assertEquals("Apenas agendamentos pendentes podem ser confirmados.", exception.getMessage());
    }

    @Test
    void shouldCancelPendingAppointmentWithReason() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.of(pendingAppointment));

        AppointmentDTO.Response response = appointmentService.cancelAppointment(id, "Cliente desistiu");

        assertEquals(AppointmentStatus.CANCELED.name(), response.status());
        assertEquals("Cliente desistiu", response.cancelReason());
    }

    @Test
    void shouldCancelPendingAppointmentWithoutReason() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.of(pendingAppointment));

        AppointmentDTO.Response response = appointmentService.cancelAppointment(id, null);

        assertEquals(AppointmentStatus.CANCELED.name(), response.status());
        assertNull(response.cancelReason());
    }

    @Test
    void shouldCancelConfirmedAppointment() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.of(confirmedAppointment));

        AppointmentDTO.Response response = appointmentService.cancelAppointment(id, null);

        assertEquals(AppointmentStatus.CANCELED.name(), response.status());
    }

    @Test
    void shouldThrowExceptionWhenCancelCompletedAppointment() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.of(completedAppointment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                appointmentService.cancelAppointment(id, null));
        assertEquals("Agendamentos concluídos não podem ser cancelados.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCancelAlreadyCanceled() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.of(canceledAppointment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                appointmentService.cancelAppointment(id, null));
        assertEquals("Agendamento já está cancelado.", exception.getMessage());
    }

    @Test
    void shouldCompleteConfirmedAppointment() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.of(confirmedAppointment));

        AppointmentDTO.Response response = appointmentService.completeAppointment(id);

        assertEquals(AppointmentStatus.COMPLETED.name(), response.status());
    }

    @Test
    void shouldThrowExceptionWhenCompleteNonConfirmed() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.of(pendingAppointment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                appointmentService.completeAppointment(id));
        assertEquals("Apenas agendamentos confirmados podem ser concluídos.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAppointmentNotFound() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                appointmentService.confirmAppointment(id));
        assertEquals("Agendamento não encontrado.", exception.getMessage());
    }
}
