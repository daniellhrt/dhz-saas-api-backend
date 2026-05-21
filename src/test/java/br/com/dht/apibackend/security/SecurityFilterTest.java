package br.com.dht.apibackend.security;

import br.com.dht.apibackend.config.TenantContext;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private SecurityFilter securityFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void shouldInjectTenantIdFromValidTokenAndClearAfterwards() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid_token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        FilterChain filterChain = mock(FilterChain.class);

        when(tokenService.isTokenValid("valid_token")).thenReturn(true);
        when(tokenService.getEmailFromToken("valid_token")).thenReturn("barber@test.com");
        when(tokenService.getTenantIdFromToken("valid_token")).thenReturn("tenant-123");

        // Act - we intercept the chain.doFilter to check if context was set correctly DURING the filter
        doAnswer(invocation -> {
            assertEquals("tenant-123", TenantContext.getTenantId(), "TenantContext deve estar populado durante o processamento da requisição");
            return null;
        }).when(filterChain).doFilter(request, response);

        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(TenantContext.getTenantId(), "TenantContext deve ser limpo APÓS o processamento da requisição (finally block)");
    }

    @Test
    void shouldNotInjectTenantIfTokenIsInvalid() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid_token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        FilterChain filterChain = mock(FilterChain.class);

        when(tokenService.isTokenValid("invalid_token")).thenReturn(false);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(TenantContext.getTenantId());
        verify(tokenService, never()).getTenantIdFromToken(anyString());
    }
}
