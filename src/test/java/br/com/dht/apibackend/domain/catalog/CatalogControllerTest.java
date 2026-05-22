package br.com.dht.apibackend.domain.catalog;

import br.com.dht.apibackend.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogService catalogService;

    @MockBean
    private TokenService tokenService;

    @Test
    void shouldReturn201_WhenCreateService() throws Exception {
        ServiceItemDTO.Request request = new ServiceItemDTO.Request("Corte", "Corte simples", new BigDecimal("50.0"), 30);
        ServiceItemDTO.Response response = new ServiceItemDTO.Response(UUID.randomUUID(), "Corte", "Corte simples", new BigDecimal("50.0"), 30, true);

        when(catalogService.createService(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Corte"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturn400_WhenCreateService_InvalidBody() throws Exception {
        ServiceItemDTO.Request request = new ServiceItemDTO.Request("", "", null, null);

        mockMvc.perform(post("/api/v1/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200_WhenListActive() throws Exception {
        ServiceItemDTO.Response service = new ServiceItemDTO.Response(UUID.randomUUID(), "Corte", "Corte simples", new BigDecimal("50.0"), 30, true);
        Page<ServiceItemDTO.Response> page = new PageImpl<>(List.of(service));

        when(catalogService.listActiveServices(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Corte"));
    }

    @Test
    void shouldReturn204_WhenDeactivate() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/catalog/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn400_WhenDeactivate_NotFound() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new IllegalArgumentException("Serviço não encontrado."))
                .when(catalogService).deactivateService(id);

        mockMvc.perform(delete("/api/v1/catalog/{id}", id))
                .andExpect(status().isBadRequest());
    }
}
