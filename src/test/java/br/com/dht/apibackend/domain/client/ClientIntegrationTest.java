package br.com.dht.apibackend.domain.client;

import br.com.dht.apibackend.domain.BaseIntegrationTest;
import br.com.dht.apibackend.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClientIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private br.com.dht.apibackend.domain.appointment.AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    private String token;
    private final String TENANT_ID = "tenant-client-123";

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        clientRepository.deleteAll();
        token = tokenService.generateToken("admin@barber.com", TENANT_ID, "ADMIN");
    }

    @Test
    void shouldPerformFullClientLifecycleWithPagination() throws Exception {
        // 1. Create Client
        ClientDTO.Request createRequest = new ClientDTO.Request(
                "John Doe", "john@test.com", "(11) 99999-9999", "12345678900", null, "Vip Client"
        );

        String responseJson = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.notes").value("Vip Client"))
                .andReturn().getResponse().getContentAsString();

        ClientDTO.Response clientResponse = objectMapper.readValue(responseJson, ClientDTO.Response.class);
        UUID clientId = clientResponse.id();

        // 2. Update Client
        ClientDTO.Request updateRequest = new ClientDTO.Request(
                "John Updated", "john@test.com", "(11) 98888-8888", "12345678900", null, "Premium Client"
        );

        mockMvc.perform(put("/api/v1/clients/" + clientId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.phone").value("(11) 98888-8888"))
                .andExpect(jsonPath("$.notes").value("Premium Client"));

        // 3. List Clients with pagination
        mockMvc.perform(get("/api/v1/clients?page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("John Updated"));
    }

    @Test
    void shouldEnforceUniqueEmailPerTenant() throws Exception {
        // Create Client 1
        ClientDTO.Request client1 = new ClientDTO.Request(
                "Client One", "common@test.com", "(11) 99999-9999", null, null, null
        );

        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client1)))
                .andExpect(status().isCreated());

        // Create Client 2 with same email - Should return 400 Bad Request
        ClientDTO.Request client2 = new ClientDTO.Request(
                "Client Two", "common@test.com", "(11) 98888-8888", null, null, null
        );

        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client2)))
                .andExpect(status().isBadRequest());
    }
}
