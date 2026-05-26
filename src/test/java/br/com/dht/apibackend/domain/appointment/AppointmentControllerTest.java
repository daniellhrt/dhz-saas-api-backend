package br.com.dht.apibackend.domain.appointment;

import br.com.dht.apibackend.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private TokenService tokenService;

    @Test
    void shouldReturn201WhenRequestIsValid() throws Exception {
        AppointmentDTO.Request request = new AppointmentDTO.Request(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().plusDays(2)
        );

        AppointmentDTO.Response mockResponse = new AppointmentDTO.Response(
                UUID.randomUUID(), "John", null, null, null, null, "Corte",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusMinutes(30), "PENDING", null
        );

        when(appointmentService.scheduleAppointment(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientName").value("John"));
    }

    @Test
    void shouldReturn200WhenConfirm() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentDTO.Response mockResponse = new AppointmentDTO.Response(
                id, "John", null, null, null, null, "Corte",
                LocalDateTime.now(), LocalDateTime.now(), "CONFIRMED", null
        );

        when(appointmentService.confirmAppointment(eq(id))).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/v1/appointments/{id}/confirm", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldReturn200WhenCancelWithReason() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentDTO.Response mockResponse = new AppointmentDTO.Response(
                id, "John", null, null, null, null, "Corte",
                LocalDateTime.now(), LocalDateTime.now(), "CANCELED", "Cliente desistiu"
        );

        when(appointmentService.cancelAppointment(eq(id), any())).thenReturn(mockResponse);

        AppointmentDTO.CancelRequest request = new AppointmentDTO.CancelRequest("Cliente desistiu");

        mockMvc.perform(patch("/api/v1/appointments/{id}/cancel", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.cancelReason").value("Cliente desistiu"));
    }

    @Test
    void shouldReturn200WhenCancelWithoutReason() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentDTO.Response mockResponse = new AppointmentDTO.Response(
                id, "John", null, null, null, null, "Corte",
                LocalDateTime.now(), LocalDateTime.now(), "CANCELED", null
        );

        when(appointmentService.cancelAppointment(eq(id), eq(null))).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/v1/appointments/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldReturn200WhenComplete() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentDTO.Response mockResponse = new AppointmentDTO.Response(
                id, "John", null, null, null, null, "Corte",
                LocalDateTime.now(), LocalDateTime.now(), "COMPLETED", null
        );

        when(appointmentService.completeAppointment(eq(id))).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/v1/appointments/{id}/complete", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
