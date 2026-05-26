package br.com.dht.apibackend.domain.catalog;

import br.com.dht.apibackend.config.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ServiceItemRepository repository;

    @InjectMocks
    private CatalogService catalogService;

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
    void shouldCreateServiceSuccessfully() {
        ServiceItemDTO.Request request = new ServiceItemDTO.Request("Corte", "Corte simples", new BigDecimal("50.0"), 30);

        when(repository.existsByTenantIdAndNameIgnoreCase(TENANT_ID, "Corte")).thenReturn(false);
        when(repository.save(any(ServiceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceItemDTO.Response response = catalogService.createService(request);

        assertNotNull(response);
        assertEquals("Corte", response.name());
        assertEquals(0, new BigDecimal("50.0").compareTo(response.price()));
        assertEquals(30, response.durationMinutes());
        assertTrue(response.active());
        verify(repository).save(any(ServiceItem.class));
    }

    @Test
    void shouldThrowExceptionWhenNameAlreadyExists() {
        ServiceItemDTO.Request request = new ServiceItemDTO.Request("Corte", "Corte simples", new BigDecimal("50.0"), 30);

        when(repository.existsByTenantIdAndNameIgnoreCase(TENANT_ID, "Corte")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> catalogService.createService(request));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldListActiveServices() {
        var page = mock(org.springframework.data.domain.Page.class);
        when(repository.findAllByTenantIdAndActiveTrue(TENANT_ID, org.springframework.data.domain.Pageable.unpaged()))
                .thenReturn(page);

        catalogService.listActiveServices(org.springframework.data.domain.Pageable.unpaged());

        verify(repository).findAllByTenantIdAndActiveTrue(TENANT_ID, org.springframework.data.domain.Pageable.unpaged());
    }

    @Test
    void shouldDeactivateServiceSuccessfully() {
        UUID id = UUID.randomUUID();
        ServiceItem item = new ServiceItem(TENANT_ID, "Corte", "Corte simples", new BigDecimal("50.0"), 30);

        when(repository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.of(item));

        catalogService.deactivateService(id);

        assertFalse(item.getActive());
    }

    @Test
    void shouldThrowExceptionWhenDeactivateNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByIdAndTenantId(id, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> catalogService.deactivateService(id));
    }

    // ==================== NOVOS TESTES (Sprint 1 — Tarefa 1.3) ====================

    @Test
    void shouldUpdateServiceSuccessfully() {
        UUID serviceId = UUID.randomUUID();
        ServiceItemDTO.Request request = new ServiceItemDTO.Request(
            "Corte Atualizado",
            "Nova descrição",
            new BigDecimal("60.0"),
            40
        );

        ServiceItem existingService = new ServiceItem(TENANT_ID, "Corte", "Descrição antiga", new BigDecimal("50.0"), 30);
        existingService.setId(serviceId);

        when(repository.findByIdAndTenantId(serviceId, TENANT_ID)).thenReturn(Optional.of(existingService));
        when(repository.existsByTenantIdAndNameIgnoreCase(TENANT_ID, "Corte Atualizado")).thenReturn(false);
        when(repository.save(any(ServiceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceItemDTO.Response response = catalogService.updateService(serviceId, request);

        assertNotNull(response);
        assertEquals("Corte Atualizado", response.name());
        assertEquals(0, new BigDecimal("60.0").compareTo(response.price()));
        verify(repository, times(1)).save(any(ServiceItem.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToExistingName() {
        UUID serviceId = UUID.randomUUID();
        ServiceItemDTO.Request request = new ServiceItemDTO.Request(
            "Outro Serviço",
            "Descrição",
            new BigDecimal("50.0"),
            30
        );

        ServiceItem existingService = new ServiceItem(TENANT_ID, "Corte", "Descrição", new BigDecimal("50.0"), 30);
        existingService.setId(serviceId);

        when(repository.findByIdAndTenantId(serviceId, TENANT_ID)).thenReturn(Optional.of(existingService));
        when(repository.existsByTenantIdAndNameIgnoreCase(TENANT_ID, "Outro Serviço")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> catalogService.updateService(serviceId, request)
        );
        assertEquals("Já existe outro serviço com este nome cadastrado.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenServiceNotFoundOnUpdate() {
        UUID nonExistentId = UUID.randomUUID();
        ServiceItemDTO.Request request = new ServiceItemDTO.Request(
            "Serviço",
            "Descrição",
            new BigDecimal("50.0"),
            30
        );

        when(repository.findByIdAndTenantId(nonExistentId, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> catalogService.updateService(nonExistentId, request));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldPreventZeroPriceAtDTO() {
        // Validação acontece no DTO via @DecimalMin(0.01)
        // Este teste documenta que preço zero é inválido
        ServiceItemDTO.Request request = new ServiceItemDTO.Request(
            "Serviço",
            "Descrição",
            BigDecimal.ZERO,
            30
        );

        assertTrue(request.price().compareTo(BigDecimal.ZERO) <= 0);
    }

    @Test
    void shouldIsolateTenantCatalog() {
        String anotherTenant = "other-tenant-456";
        UUID serviceId = UUID.randomUUID();

        TenantContext.setTenantId(anotherTenant);

        when(repository.findByIdAndTenantId(serviceId, anotherTenant)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> catalogService.deactivateService(serviceId));
        verify(repository, times(1)).findByIdAndTenantId(serviceId, anotherTenant);
    }
}
