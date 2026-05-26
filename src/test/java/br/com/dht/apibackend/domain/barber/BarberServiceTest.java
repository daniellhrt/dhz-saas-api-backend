package br.com.dht.apibackend.domain.barber;

import br.com.dht.apibackend.config.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarberServiceTest {

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BarberService barberService;

    private final String TENANT_ID = "tenant-test-123";
    private final String ADMIN_EMAIL = "admin@barber.com";
    private final String USER_EMAIL = "user@barber.com";

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldRegisterAdminSuccessfully() {
        BarberDTO.RegisterRequest request = new BarberDTO.RegisterRequest("Admin", ADMIN_EMAIL, "password123");

        when(barberRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-hash");
        when(barberRepository.save(any(Barber.class))).thenAnswer(invocation -> {
            Barber barber = invocation.getArgument(0);
            return barber;
        });

        BarberDTO.Response response = barberService.registerAdmin(request);

        assertNotNull(response);
        assertEquals("Admin", response.name());
        assertEquals(ADMIN_EMAIL, response.email());
        assertEquals(BarberRole.ADMIN, response.role());
        verify(barberRepository).save(any(Barber.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExistsOnRegister() {
        BarberDTO.RegisterRequest request = new BarberDTO.RegisterRequest("Admin", ADMIN_EMAIL, "password123");
        when(barberRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(new Barber()));

        assertThrows(IllegalArgumentException.class, () -> barberService.registerAdmin(request));
        verify(barberRepository, never()).save(any());
    }

    @Test
    void shouldCreateBarberSuccessfully_WhenAdmin() {
        try (MockedStatic<SecurityContextHolder> securityHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(ADMIN_EMAIL);
            securityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Barber admin = new Barber(TENANT_ID, "Admin", ADMIN_EMAIL, "hash", BarberRole.ADMIN);
            when(barberRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
            when(barberRepository.findByEmail("new@barber.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("password123")).thenReturn("encoded-hash");
            when(barberRepository.save(any(Barber.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BarberDTO.CreateRequest request = new BarberDTO.CreateRequest("New User", "new@barber.com", "password123");
            BarberDTO.Response response = barberService.createBarber(request);

            assertNotNull(response);
            assertEquals("New User", response.name());
            assertEquals(BarberRole.USER, response.role());
        }
    }

    @Test
    void shouldThrowException_WhenCreateBarber_NonAdmin() {
        try (MockedStatic<SecurityContextHolder> securityHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(USER_EMAIL);
            securityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Barber user = new Barber(TENANT_ID, "User", USER_EMAIL, "hash", BarberRole.USER);
            when(barberRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

            BarberDTO.CreateRequest request = new BarberDTO.CreateRequest("Another", "another@barber.com", "password123");

            assertThrows(SecurityException.class, () -> barberService.createBarber(request));
            verify(barberRepository, never()).save(any());
        }
    }

    @Test
    void shouldListAllBarbers_ForTenant() {
        var page = mock(org.springframework.data.domain.Page.class);
        when(barberRepository.findAllByTenantId(TENANT_ID, org.springframework.data.domain.Pageable.unpaged()))
                .thenReturn(page);

        barberService.listAllBarbers(org.springframework.data.domain.Pageable.unpaged());

        verify(barberRepository).findAllByTenantId(TENANT_ID, org.springframework.data.domain.Pageable.unpaged());
    }

    @Test
    void shouldUpdateOwnProfile() {
        UUID barberId = UUID.randomUUID();

        try (MockedStatic<SecurityContextHolder> securityHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(ADMIN_EMAIL);
            securityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Barber existing = new Barber(TENANT_ID, "Old Name", ADMIN_EMAIL, "hash", BarberRole.ADMIN);
            when(barberRepository.findByIdAndTenantId(barberId, TENANT_ID)).thenReturn(Optional.of(existing));
            when(barberRepository.save(any(Barber.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BarberDTO.UpdateRequest request = new BarberDTO.UpdateRequest("New Name", null);
            BarberDTO.Response response = barberService.updateBarber(barberId, request);

            assertEquals("New Name", response.name());
            assertEquals(ADMIN_EMAIL, response.email());
        }
    }

    @Test
    void shouldThrowException_WhenUpdateOtherBarber() {
        UUID barberId = UUID.randomUUID();

        try (MockedStatic<SecurityContextHolder> securityHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(ADMIN_EMAIL);
            securityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Barber other = new Barber(TENANT_ID, "Other", "other@barber.com", "hash", BarberRole.USER);
            when(barberRepository.findByIdAndTenantId(barberId, TENANT_ID)).thenReturn(Optional.of(other));

            BarberDTO.UpdateRequest request = new BarberDTO.UpdateRequest("Hacker", null);

            assertThrows(IllegalArgumentException.class, () -> barberService.updateBarber(barberId, request));
        }
    }

    @Test
    void shouldDeleteBarber_WhenAdmin() {
        UUID barberId = UUID.randomUUID();

        try (MockedStatic<SecurityContextHolder> securityHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(ADMIN_EMAIL);
            securityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Barber admin = new Barber(TENANT_ID, "Admin", ADMIN_EMAIL, "hash", BarberRole.ADMIN);
            Barber target = new Barber(TENANT_ID, "Target", "target@barber.com", "hash", BarberRole.USER);

            when(barberRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
            when(barberRepository.findByIdAndTenantId(barberId, TENANT_ID)).thenReturn(Optional.of(target));

            barberService.deleteBarber(barberId);

            verify(barberRepository).delete(target);
        }
    }

    @Test
    void shouldThrowException_WhenDeleteSelf() {
        UUID barberId = UUID.randomUUID();

        try (MockedStatic<SecurityContextHolder> securityHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(ADMIN_EMAIL);
            securityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Barber admin = new Barber(TENANT_ID, "Admin", ADMIN_EMAIL, "hash", BarberRole.ADMIN);
            when(barberRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
            when(barberRepository.findByIdAndTenantId(barberId, TENANT_ID)).thenReturn(Optional.of(admin));

            assertThrows(IllegalArgumentException.class, () -> barberService.deleteBarber(barberId));
            verify(barberRepository, never()).delete(any());
        }
    }

    @Test
    void shouldThrowException_WhenDelete_NonAdmin() {
        UUID barberId = UUID.randomUUID();

        try (MockedStatic<SecurityContextHolder> securityHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(USER_EMAIL);
            securityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Barber target = new Barber(TENANT_ID, "Target", "target@barber.com", "hash", BarberRole.USER);
            Barber user = new Barber(TENANT_ID, "User", USER_EMAIL, "hash", BarberRole.USER);

            when(barberRepository.findByIdAndTenantId(barberId, TENANT_ID)).thenReturn(Optional.of(target));
            when(barberRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

            assertThrows(SecurityException.class, () -> barberService.deleteBarber(barberId));
            verify(barberRepository, never()).delete(any());
        }
    }

    @Test
    void shouldThrowException_WhenBarberNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        when(barberRepository.findByIdAndTenantId(nonExistentId, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> barberService.updateBarber(nonExistentId, new BarberDTO.UpdateRequest("Name", null)));
        verify(barberRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_WhenUpdatingToExistingEmail() {
        UUID barberId = UUID.randomUUID();

        try (MockedStatic<SecurityContextHolder> securityHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(ADMIN_EMAIL);
            securityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Barber self = new Barber(TENANT_ID, "Admin", ADMIN_EMAIL, "hash", BarberRole.ADMIN);
            Barber other = new Barber(TENANT_ID, "Other", "other@barber.com", "hash", BarberRole.USER);

            when(barberRepository.findByIdAndTenantId(barberId, TENANT_ID)).thenReturn(Optional.of(self));
            when(barberRepository.findByEmail("other@barber.com")).thenReturn(Optional.of(other));

            BarberDTO.UpdateRequest request = new BarberDTO.UpdateRequest("Admin", "other@barber.com");

            assertThrows(IllegalArgumentException.class, () -> barberService.updateBarber(barberId, request));
        }
    }

    @Test
    void shouldThrowException_WhenCreateBarber_EmailDuplicate() {
        try (MockedStatic<SecurityContextHolder> securityHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(ADMIN_EMAIL);
            securityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Barber admin = new Barber(TENANT_ID, "Admin", ADMIN_EMAIL, "hash", BarberRole.ADMIN);
            Barber existing = new Barber(TENANT_ID, "Existing", "existing@barber.com", "hash", BarberRole.USER);

            when(barberRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
            when(barberRepository.findByEmail("existing@barber.com")).thenReturn(Optional.of(existing));

            BarberDTO.CreateRequest request = new BarberDTO.CreateRequest("New", "existing@barber.com", "pass");

            assertThrows(IllegalArgumentException.class, () -> barberService.createBarber(request));
            verify(barberRepository, never()).save(any());
        }
    }

    @Test
    void shouldIsolateBarbersByTenant() {
        String anotherTenant = "other-tenant-456";
        UUID barberId = UUID.randomUUID();

        TenantContext.setTenantId(anotherTenant);

        when(barberRepository.findByIdAndTenantId(barberId, anotherTenant)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> barberService.updateBarber(barberId, new BarberDTO.UpdateRequest("Name", null)));
        verify(barberRepository, never()).save(any());
    }
}
