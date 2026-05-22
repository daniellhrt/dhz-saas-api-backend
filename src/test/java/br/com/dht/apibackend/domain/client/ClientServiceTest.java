package br.com.dht.apibackend.domain.client;

import br.com.dht.apibackend.config.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private final String TENANT_ID = "tenant-test-123";

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateClientSuccessfully() {
        ClientDTO.Request request = new ClientDTO.Request("John Doe", "john@test.com", "11999999999");

        when(clientRepository.findByEmailAndTenantId("john@test.com", TENANT_ID)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientDTO.Response response = clientService.createClient(request);

        assertNotNull(response);
        assertEquals("John Doe", response.name());
        assertEquals("john@test.com", response.email());
        assertEquals("11999999999", response.phone());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void shouldThrowExceptionWhenTenantContextNull() {
        TenantContext.clear();

        ClientDTO.Request request = new ClientDTO.Request("John Doe", "john@test.com", "11999999999");

        assertThrows(IllegalStateException.class, () -> clientService.createClient(request));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        ClientDTO.Request request = new ClientDTO.Request("John Doe", "john@test.com", "11999999999");

        when(clientRepository.findByEmailAndTenantId("john@test.com", TENANT_ID)).thenReturn(Optional.of(new Client()));

        assertThrows(IllegalArgumentException.class, () -> clientService.createClient(request));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldListAllClients() {
        var page = mock(org.springframework.data.domain.Page.class);
        when(clientRepository.findAllByTenantId(TENANT_ID, org.springframework.data.domain.Pageable.unpaged()))
                .thenReturn(page);

        clientService.listAllClients(org.springframework.data.domain.Pageable.unpaged());

        verify(clientRepository).findAllByTenantId(TENANT_ID, org.springframework.data.domain.Pageable.unpaged());
    }
}
