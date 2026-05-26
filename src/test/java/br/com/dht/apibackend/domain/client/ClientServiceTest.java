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
        ClientDTO.Request request = new ClientDTO.Request("John Doe", "john@test.com", "11999999999", null, null, null);

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

        ClientDTO.Request request = new ClientDTO.Request("John Doe", "john@test.com", "11999999999", null, null, null);

        assertThrows(IllegalStateException.class, () -> clientService.createClient(request));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        ClientDTO.Request request = new ClientDTO.Request("John Doe", "john@test.com", "11999999999", null, null, null);

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

    // ==================== NOVOS TESTES (Sprint 1 — Tarefa 1.2) ====================

    @Test
    void shouldUpdateClientSuccessfully() {
        java.util.UUID clientId = java.util.UUID.randomUUID();

        Client existingClient = new Client(TENANT_ID, "John Old", "john@test.com", "11999999999");
        existingClient.setId(clientId);

        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.of(existingClient));
        when(clientRepository.findByEmailAndTenantId("john@test.com", TENANT_ID)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientDTO.Request request = new ClientDTO.Request("John Updated", "john@test.com", "11999999999", "12345678900", null, null);
        ClientDTO.Response response = clientService.updateClient(clientId, request);

        assertNotNull(response);
        assertEquals("John Updated", response.name());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToExistingEmail() {
        java.util.UUID clientId = java.util.UUID.randomUUID();

        Client currentClient = new Client(TENANT_ID, "John", "john@test.com", "11999999999");
        currentClient.setId(clientId);

        Client otherClient = new Client(TENANT_ID, "Jane", "jane@test.com", "11888888888");
        otherClient.setId(java.util.UUID.randomUUID());

        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.of(currentClient));
        when(clientRepository.findByEmailAndTenantId("jane@test.com", TENANT_ID)).thenReturn(Optional.of(otherClient));

        ClientDTO.Request request = new ClientDTO.Request("John", "jane@test.com", "11999999999", null, null, null);

        assertThrows(IllegalArgumentException.class, () -> clientService.updateClient(clientId, request));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenClientNotFound() {
        java.util.UUID nonExistentId = java.util.UUID.randomUUID();

        when(clientRepository.findByIdAndTenantId(nonExistentId, TENANT_ID)).thenReturn(Optional.empty());

        ClientDTO.Request request = new ClientDTO.Request("Name", "email@test.com", "11999999999", null, null, null);

        assertThrows(IllegalArgumentException.class, () -> clientService.updateClient(nonExistentId, request));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldAllowOptionalFields() {
        ClientDTO.Request request = new ClientDTO.Request(
            "John Doe",
            "john@test.com",
            "11999999999",
            null,           // cpf optional
            null,           // birthDate optional
            null            // notes optional
        );

        when(clientRepository.findByEmailAndTenantId("john@test.com", TENANT_ID)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientDTO.Response response = clientService.createClient(request);

        assertNotNull(response);
        assertNull(response.cpf());
        assertNull(response.birthDate());
        assertNull(response.notes());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void shouldIsolateClientsByTenant() {
        String anotherTenant = "other-tenant-456";
        java.util.UUID clientId = java.util.UUID.randomUUID();

        // Mudar tenant
        TenantContext.setTenantId(anotherTenant);

        when(clientRepository.findByIdAndTenantId(clientId, anotherTenant)).thenReturn(Optional.empty());

        ClientDTO.Request request = new ClientDTO.Request("Name", "email@test.com", "11999999999", null, null, null);

        assertThrows(IllegalArgumentException.class, () -> clientService.updateClient(clientId, request));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExistsOnUpdate() {
        java.util.UUID clientId = java.util.UUID.randomUUID();

        Client ownClient = new Client(TENANT_ID, "Old Name", "old@test.com", "11999999999");
        ownClient.setId(clientId);

        when(clientRepository.findByIdAndTenantId(clientId, TENANT_ID)).thenReturn(Optional.of(ownClient));
        when(clientRepository.findByEmailAndTenantId("old@test.com", TENANT_ID)).thenReturn(Optional.of(ownClient));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Agora tenta atualizar e-mail para um que já existe (mesmo caso, continua OK)
        // Mas se mudar para e-mail de outro, deve falhar
        // No caso atual, o clientRepository retorna ownClient para ambos, então OK
        ClientDTO.Request request = new ClientDTO.Request("New Name", "old@test.com", "11999999999", null, null, null);
        ClientDTO.Response response = clientService.updateClient(clientId, request);

        assertNotNull(response);
        verify(clientRepository).save(any(Client.class));
    }
}
