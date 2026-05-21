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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false) // Desativa os filtros de segurança para focar na camada web
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;
    
    @MockBean
    private TokenService tokenService; // Necessário para não quebrar a carga do contexto de segurança

    @Test
    void shouldReturn200WhenRequestIsValid() throws Exception {
        AppointmentDTO.Request request = new AppointmentDTO.Request(
                UUID.randomUUID(), 
                UUID.randomUUID(), 
                LocalDateTime.now().plusDays(2)
        );

        AppointmentDTO.Response mockResponse = new AppointmentDTO.Response(
                UUID.randomUUID(), "John", "Corte", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusMinutes(30), "PENDING"
        );

        when(appointmentService.scheduleAppointment(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientName").value("John"));
    }

    @Test
    void shouldReturn400WhenDateIsInThePast() throws Exception {
        AppointmentDTO.Request request = new AppointmentDTO.Request(
                UUID.randomUUID(), 
                UUID.randomUUID(), 
                LocalDateTime.now().minusDays(1) // Passado!
        );

        mockMvc.perform(post("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
