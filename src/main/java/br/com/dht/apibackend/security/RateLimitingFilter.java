/**
 * Propósito: Prevenir ataques de força bruta e abuso de requisições na camada de autenticação.
 * Responsabilidade: Interceptar requisições de login e aplicar limite de taxa (Rate Limiting) por IP,
 *                  bloqueando acessos excessivos com HTTP 429. Usa Redis distribuído quando disponível,
 *                  com fallback para ConcurrentHashMap em memória.
 * Papel na Arquitetura: Security / Filter.
 */
package br.com.dht.apibackend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    private static final int LIMIT_PER_MINUTE = 5;

    private final ProxyManager<byte[]> proxyManager;
    private final Map<String, Bucket> localBuckets;

    public RateLimitingFilter(@Autowired(required = false) ProxyManager<byte[]> proxyManager) {
        this.proxyManager = proxyManager;
        this.localBuckets = proxyManager != null ? null : new ConcurrentHashMap<>();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if ("/api/v1/auth/login".equals(request.getRequestURI())) {
            String ip = getClientIp(request);
            Bucket bucket = resolveBucket(ip);

            if (bucket.tryConsume(1)) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests. Please try again later.");
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private Bucket resolveBucket(String ip) {
        if (proxyManager != null) {
            byte[] key = ip.getBytes(StandardCharsets.UTF_8);
            BucketConfiguration config = BucketConfiguration.builder()
                    .addLimit(Bandwidth.classic(LIMIT_PER_MINUTE, Refill.greedy(LIMIT_PER_MINUTE, Duration.ofMinutes(1))))
                    .build();
            return proxyManager.builder().build(key, () -> config);
        }
        return localBuckets.computeIfAbsent(ip, this::createNewBucket);
    }

    private Bucket createNewBucket(String ip) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(LIMIT_PER_MINUTE, Refill.greedy(LIMIT_PER_MINUTE, Duration.ofMinutes(1))))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}