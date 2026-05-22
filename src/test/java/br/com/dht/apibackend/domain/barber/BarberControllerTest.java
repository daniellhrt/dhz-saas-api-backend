package br.com.dht.apibackend.domain.barber;

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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BarberController.class)
@AutoConfigureMockMvc(addFilters = false)
class BarberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BarberService barberService;

    @MockBean
    private TokenService tokenService;

    @Test
    void shouldReturn201_WhenCreateBarber() throws Exception {
        BarberDTO.CreateRequest request = new BarberDTO.CreateRequest("New User", "user@barber.com", "password123");
        BarberDTO.Response response = new BarberDTO.Response(UUID.randomUUID(), "New User", "user@barber.com", BarberRole.USER);

        when(barberService.createBarber(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/barbers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldReturn400_WhenCreateBarber_InvalidBody() throws Exception {
        BarberDTO.CreateRequest request = new BarberDTO.CreateRequest("", "", "");

        mockMvc.perform(post("/api/v1/barbers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200_WhenListAll() throws Exception {
        BarberDTO.Response barber = new BarberDTO.Response(UUID.randomUUID(), "Admin", "admin@barber.com", BarberRole.ADMIN);
        Page<BarberDTO.Response> page = new PageImpl<>(List.of(barber));

        when(barberService.listAllBarbers(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/barbers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Admin"));
    }

    @Test
    void shouldReturn200_WhenUpdate() throws Exception {
        UUID id = UUID.randomUUID();
        BarberDTO.UpdateRequest request = new BarberDTO.UpdateRequest("Updated", "updated@barber.com");
        BarberDTO.Response response = new BarberDTO.Response(id, "Updated", "updated@barber.com", BarberRole.ADMIN);

        when(barberService.updateBarber(eq(id), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/barbers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldReturn204_WhenDelete() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/barbers/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404_WhenDelete_NotFound() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new IllegalArgumentException("Barbeiro não encontrado."))
                .when(barberService).deleteBarber(id);

        mockMvc.perform(delete("/api/v1/barbers/{id}", id))
                .andExpect(status().isBadRequest());
    }
}
