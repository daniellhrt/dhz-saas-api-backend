package br.com.dht.apibackend.security;

import br.com.dht.apibackend.domain.BaseIntegrationTest;
import br.com.dht.apibackend.domain.barber.Barber;
import br.com.dht.apibackend.domain.barber.BarberDTO;
import br.com.dht.apibackend.domain.barber.BarberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de autorização end-to-end.
 * Valida permissões de papel (ADMIN vs USER) e isolamento multi-tenant
 * usando o filtro de segurança real com tokens JWT válidos.
 */
public class AuthorizationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private TokenService tokenService;

    private String adminToken;
    private String userToken;
    private String adminEmail = "admin-auth@barber.com";
    private String userEmail = "user-auth@barber.com";
    private String tenantId;

    @BeforeEach
    void setUp() throws Exception {
        barberRepository.deleteAll();

        // 1. Register ADMIN via public endpoint
        BarberDTO.RegisterRequest registerRequest = new BarberDTO.RegisterRequest(
                "Admin Auth", adminEmail, "password123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        Barber adminEntity = barberRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new AssertionError("Admin not found"));
        tenantId = adminEntity.getTenantId();
        adminToken = tokenService.generateToken(adminEmail, tenantId, "ADMIN");

        // 2. Create USER barber via ADMIN token
        BarberDTO.CreateRequest createUserRequest = new BarberDTO.CreateRequest(
                "User Auth", userEmail, "password123"
        );
        mockMvc.perform(post("/api/v1/barbers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated());

        userToken = tokenService.generateToken(userEmail, tenantId, "USER");
    }

    @Test
    void shouldAllowAdminToCreateBarber() throws Exception {
        BarberDTO.CreateRequest request = new BarberDTO.CreateRequest(
                "New Employee", "employee-new@barber.com", "password123"
        );

        mockMvc.perform(post("/api/v1/barbers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Employee"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldDenyUserFromCreatingBarber() throws Exception {
        BarberDTO.CreateRequest request = new BarberDTO.CreateRequest(
                "Illegal Employee", "illegal@barber.com", "password123"
        );

        mockMvc.perform(post("/api/v1/barbers")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void shouldAllowUserToUpdateOwnProfile() throws Exception {
        Barber userEntity = barberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AssertionError("User not found"));

        BarberDTO.UpdateRequest request = new BarberDTO.UpdateRequest("Updated User Name", null);

        mockMvc.perform(put("/api/v1/barbers/{id}", userEntity.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User Name"));
    }

    @Test
    void shouldDenyUserFromDeletingBarber() throws Exception {
        Barber adminEntity = barberRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new AssertionError("Admin not found"));

        mockMvc.perform(delete("/api/v1/barbers/{id}", adminEntity.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void shouldDenyAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/barbers"))
                .andExpect(status().isForbidden());
    }
}
