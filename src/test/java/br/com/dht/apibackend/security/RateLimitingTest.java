package br.com.dht.apibackend.security;

import br.com.dht.apibackend.security.dto.AuthDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do RateLimitingFilter.
 * Testa diretamente o filtro com MockHttpServletRequest/Response,
 * evitando contaminação de estado de bucket entre testes.
 */
class RateLimitingTest {

    private RateLimitingFilter rateLimitingFilter;
    private FilterChain filterChain;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Fresh filter with in-memory buckets (no Redis ProxyManager)
        rateLimitingFilter = new RateLimitingFilter(null);
        filterChain = mock(FilterChain.class);
    }

    private MockHttpServletRequest createLoginRequest(String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setContentType("application/json");
        request.setRemoteAddr(ip);
        AuthDTO.LoginRequest loginBody = new AuthDTO.LoginRequest("test@barber.com", "wrongpass");
        request.setContent(objectMapper.writeValueAsBytes(loginBody));
        return request;
    }

    @Test
    void shouldAllowFiveLoginsFromSameIp() throws Exception {
        String ip = "192.168.1.100";

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = createLoginRequest(ip);
            MockHttpServletResponse response = new MockHttpServletResponse();

            rateLimitingFilter.doFilter(request, response, filterChain);

            // Should pass through to the filter chain (not blocked)
            assertNotEquals(429, response.getStatus(),
                    "Request " + (i + 1) + " should not be rate-limited");
        }

        // Verify all 5 requests were forwarded to the filter chain
        verify(filterChain, times(5)).doFilter(any(), any());
    }

    @Test
    void shouldBlockSixthLoginFromSameIpWith429() throws Exception {
        String ip = "10.0.0.50";

        // First 5 should pass through
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = createLoginRequest(ip);
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitingFilter.doFilter(request, response, filterChain);
            assertNotEquals(429, response.getStatus(),
                    "Request " + (i + 1) + " should not be rate-limited");
        }

        // 6th request should be blocked
        MockHttpServletRequest blockedRequest = createLoginRequest(ip);
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        rateLimitingFilter.doFilter(blockedRequest, blockedResponse, filterChain);

        assertEquals(429, blockedResponse.getStatus(),
                "6th request should be rate-limited with 429");

        // Verify only 5 requests were forwarded, 6th was blocked
        verify(filterChain, times(5)).doFilter(any(), any());
    }

    @Test
    void shouldAllowLoginsFromDifferentIpsWithoutBlocking() throws Exception {
        // 6 requests from 6 different IPs — all should pass
        for (int i = 0; i < 6; i++) {
            String distinctIp = "172.16.0." + (i + 1);
            MockHttpServletRequest request = createLoginRequest(distinctIp);
            MockHttpServletResponse response = new MockHttpServletResponse();

            rateLimitingFilter.doFilter(request, response, filterChain);

            assertNotEquals(429, response.getStatus(),
                    "Request from " + distinctIp + " should not be rate-limited");
        }

        verify(filterChain, times(6)).doFilter(any(), any());
    }
}
