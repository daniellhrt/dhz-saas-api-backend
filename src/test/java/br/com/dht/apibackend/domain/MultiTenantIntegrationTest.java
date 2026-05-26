package br.com.dht.apibackend.domain;

import br.com.dht.apibackend.domain.barber.Barber;
import br.com.dht.apibackend.domain.barber.BarberDTO;
import br.com.dht.apibackend.domain.barber.BarberRepository;
import br.com.dht.apibackend.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MultiTenantIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        barberRepository.deleteAll();
    }

    @Test
    void shouldStrictlyIsolateDataBetweenTenantAAndTenantB() throws Exception {
        // 1. Tenant A registers Admin
        BarberDTO.RegisterRequest regA = new BarberDTO.RegisterRequest("Admin Tenant A", "adminA@barber.com", "password123");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regA)))
                .andExpect(status().isCreated());

        Barber entityA = barberRepository.findByEmail("adminA@barber.com")
                .orElseThrow(() -> new AssertionError("Admin A not found"));
        String tenantA = entityA.getTenantId();
        String tokenA = tokenService.generateToken("adminA@barber.com", tenantA, "ADMIN");

        // 2. Tenant B registers Admin
        BarberDTO.RegisterRequest regB = new BarberDTO.RegisterRequest("Admin Tenant B", "adminB@barber.com", "password123");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regB)))
                .andExpect(status().isCreated());

        Barber entityB = barberRepository.findByEmail("adminB@barber.com")
                .orElseThrow(() -> new AssertionError("Admin B not found"));
        String tenantB = entityB.getTenantId();
        String tokenB = tokenService.generateToken("adminB@barber.com", tenantB, "ADMIN");

        // 3. Tenant A lists barbers - should only see 1 (Admin A)
        mockMvc.perform(get("/api/v1/barbers")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Admin Tenant A"));

        // 4. Tenant B lists barbers - should only see 1 (Admin B)
        mockMvc.perform(get("/api/v1/barbers")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Admin Tenant B"));
    }
}
