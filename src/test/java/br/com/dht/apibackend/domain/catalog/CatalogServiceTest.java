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
}
