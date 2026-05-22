/**
 * Propósito: Prevenir ataques de força bruta e abuso de requisições na camada de autenticação.
 * Responsabilidade: Interceptar requisições de login e aplicar limite de taxa (Rate Limiting) por IP, bloqueando acessos excessivos com HTTP 429.
 * Papel na Arquitetura: Security / Filter.
 */
package br.com.dht.apibackend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    // Armazena em memória o contador de requisições associado a cada IP
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Limita a verificação exclusivamente à rota de login, evitando penalizar rotas operacionais do sistema
        if ("/api/v1/auth/login".equals(request.getRequestURI())) {
            String ip = getClientIp(request);
            Bucket bucket = buckets.computeIfAbsent(ip, this::createNewBucket);

            // Tenta consumir 1 token do bucket. Se não houver tokens disponíveis, o limite foi excedido.
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

    private Bucket createNewBucket(String s) {
        // Regra de Segurança: 5 requisições a cada 1 minuto por IP.
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        // Resolve o IP real do cliente caso a API esteja atrás de um Proxy Reverso, WAF ou Load Balancer (ex: Nginx, AWS ALB)
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}