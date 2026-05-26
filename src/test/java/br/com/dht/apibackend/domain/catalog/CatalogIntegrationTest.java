package br.com.dht.apibackend.domain.catalog;

import br.com.dht.apibackend.domain.BaseIntegrationTest;
import br.com.dht.apibackend.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CatalogIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Autowired
    private br.com.dht.apibackend.domain.appointment.AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    private String token;
    private final String TENANT_ID = "tenant-catalog-123";

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        serviceItemRepository.deleteAll();
        token = tokenService.generateToken("admin@barber.com", TENANT_ID, "ADMIN");
    }

    @Test
    void shouldCreateServiceAndListActiveOnly() throws Exception {
        // 1. Create active service
        ServiceItemDTO.Request corte = new ServiceItemDTO.Request(
                "Corte de Cabelo", "Corte degradê moderno", new BigDecimal("50.00"), 30
        );

        String corteJson = mockMvc.perform(post("/api/v1/catalog")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corte)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Corte de Cabelo"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();

        ServiceItemDTO.Response response = objectMapper.readValue(corteJson, ServiceItemDTO.Response.class);
        UUID serviceId = response.id();

        // 2. List catalog - should contain the active service
        mockMvc.perform(get("/api/v1/catalog")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Corte de Cabelo"));

        // 3. Deactivate the service (soft delete)
        mockMvc.perform(delete("/api/v1/catalog/" + serviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 4. List catalog again - should be empty because it only lists active items
        mockMvc.perform(get("/api/v1/catalog")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
