package br.com.dht.apibackend.domain.appointment;

import br.com.dht.apibackend.domain.BaseIntegrationTest;
import br.com.dht.apibackend.domain.catalog.ServiceItem;
import br.com.dht.apibackend.domain.catalog.ServiceItemRepository;
import br.com.dht.apibackend.domain.client.Client;
import br.com.dht.apibackend.domain.client.ClientRepository;
import br.com.dht.apibackend.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AppointmentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Autowired
    private TokenService tokenService;

    private String token;
    private final String TENANT_ID = "tenant-appt-123";
    private Client client;
    private ServiceItem service;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        clientRepository.deleteAll();
        serviceItemRepository.deleteAll();

        token = tokenService.generateToken("admin@barber.com", TENANT_ID, "ADMIN");

        // Prepare client & service
        client = clientRepository.save(new Client(TENANT_ID, "John Doe", "john@test.com", "(11) 99999-9999"));
        service = serviceItemRepository.save(new ServiceItem(TENANT_ID, "Corte", "Corte simples", new BigDecimal("50.00"), 30));
    }

    @Test
    void shouldExecuteFullAppointmentWorkflow() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

        // 1. Create Appointment
        AppointmentDTO.Request request = new AppointmentDTO.Request(client.getId(), service.getId(), startTime);

        String apptJson = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        AppointmentDTO.Response response = objectMapper.readValue(apptJson, AppointmentDTO.Response.class);
        UUID apptId = response.id();

        // 2. Confirm Appointment
        mockMvc.perform(patch("/api/v1/appointments/" + apptId + "/confirm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // 3. Start Appointment
        mockMvc.perform(patch("/api/v1/appointments/" + apptId + "/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 4. Complete Appointment
        mockMvc.perform(patch("/api/v1/appointments/" + apptId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldPreventDoubleBookingOnOverlappingTimes() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0);

        // Schedule first appointment (14:00 - 14:30)
        AppointmentDTO.Request request1 = new AppointmentDTO.Request(client.getId(), service.getId(), startTime);
        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Attempt scheduling second overlapping appointment (14:15 - 14:45) - should return 400 Bad Request
        LocalDateTime overlappingTime = startTime.plusMinutes(15);
        AppointmentDTO.Request request2 = new AppointmentDTO.Request(client.getId(), service.getId(), overlappingTime);
        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }
}
