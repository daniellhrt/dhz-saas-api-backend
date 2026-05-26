package br.com.dht.apibackend.security;

import br.com.dht.apibackend.domain.barber.BarberController;
import br.com.dht.apibackend.domain.barber.BarberDTO;
import br.com.dht.apibackend.domain.barber.BarberService;
import br.com.dht.apibackend.domain.catalog.CatalogController;
import br.com.dht.apibackend.domain.catalog.CatalogService;
import br.com.dht.apibackend.domain.catalog.ServiceItemDTO;
import br.com.dht.apibackend.domain.client.ClientController;
import br.com.dht.apibackend.domain.client.ClientDTO;
import br.com.dht.apibackend.domain.client.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de validação de entrada (JSR-380).
 * Usa @WebMvcTest para testar as anotações de validação nos DTOs
 * e o formato de resposta padronizado do GlobalExceptionHandler.
 */
@WebMvcTest({BarberController.class, ClientController.class, CatalogController.class})
@AutoConfigureMockMvc(addFilters = false)
public class InputValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BarberService barberService;

    @MockBean
    private ClientService clientService;

    @MockBean
    private CatalogService catalogService;

    @MockBean
    private TokenService tokenService;

    // ==================== Barber Validation ====================

    @Test
    void shouldRejectEmptyNameAndEmailOnRegister() throws Exception {
        BarberDTO.CreateRequest request = new BarberDTO.CreateRequest("", "", "");

        mockMvc.perform(post("/api/v1/barbers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldRejectInvalidEmailFormat() throws Exception {
        BarberDTO.CreateRequest request = new BarberDTO.CreateRequest("Valid Name", "invalid-email", "password123");

        mockMvc.perform(post("/api/v1/barbers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message", containsString("e-mail")));
    }

    // ==================== Client Validation ====================

    @Test
    void shouldRejectInvalidPhoneFormat() throws Exception {
        ClientDTO.Request request = new ClientDTO.Request("John Doe", "john@test.com", "123456789", null, null, null);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message", containsString("telefone")));
    }

    // ==================== Catalog Validation ====================

    @Test
    void shouldRejectNegativePriceAndShortDurationOnCatalog() throws Exception {
        ServiceItemDTO.Request request = new ServiceItemDTO.Request("Corte", null, new BigDecimal("-5.00"), 10);

        mockMvc.perform(post("/api/v1/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== StandardError Format ====================

    @Test
    void shouldConvertValidationExceptionToStandardErrorResponse() throws Exception {
        // Completely empty body should trigger multiple validation errors
        BarberDTO.CreateRequest request = new BarberDTO.CreateRequest("", "", "");

        mockMvc.perform(post("/api/v1/barbers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/barbers"));
    }
}
