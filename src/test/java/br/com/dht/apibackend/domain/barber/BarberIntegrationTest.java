package br.com.dht.apibackend.domain.barber;

import br.com.dht.apibackend.domain.BaseIntegrationTest;
import br.com.dht.apibackend.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BarberIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private TokenService tokenService;

    @BeforeEach
    void cleanUp() {
        barberRepository.deleteAll();
    }

    @Test
    void shouldRegisterAdminAndCreateUserSuccessfully() throws Exception {
        // 1. Register ADMIN
        BarberDTO.RegisterRequest registerRequest = new BarberDTO.RegisterRequest(
                "Super Admin", "admin@barber.com", "password123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Super Admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        Barber adminEntity = barberRepository.findByEmail("admin@barber.com")
                .orElseThrow(() -> new AssertionError("Admin not found"));
        String tenantId = adminEntity.getTenantId();
        assertNotNull(tenantId);

        // Generate Token
        String token = tokenService.generateToken("admin@barber.com", tenantId, "ADMIN");

        // 2. Create USER Barber using ADMIN token
        BarberDTO.CreateRequest createRequest = new BarberDTO.CreateRequest(
                "Employee Barber", "employee@barber.com", "password123"
        );

        String userResponseJson = mockMvc.perform(post("/api/v1/barbers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Employee Barber"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn().getResponse().getContentAsString();

        BarberDTO.Response userResponse = objectMapper.readValue(userResponseJson, BarberDTO.Response.class);
        UUID userId = userResponse.id();

        // 3. List Barbers
        mockMvc.perform(get("/api/v1/barbers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        // 4. Delete Barber
        mockMvc.perform(delete("/api/v1/barbers/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 5. Verify deleted
        mockMvc.perform(get("/api/v1/barbers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
