/**
 * Propósito: Filtro de interceptação de requisições HTTP.
 * Responsabilidade: Extrair o JWT do cabeçalho, validar e configurar o contexto de segurança e de tenant.
 * Papel na Arquitetura: Security / Filter.
 */
package br.com.dht.apibackend.security;

import br.com.dht.apibackend.config.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null && tokenService.isTokenValid(token)) {
                String email = tokenService.getEmailFromToken(token);
                String tenantId = tokenService.getTenantIdFromToken(token);

                // 1. Injeta o TenantId na Thread atual (Nossa blindagem de banco de dados)
                TenantContext.setTenantId(tenantId);

                // 2. Avisa o Spring Security que o usuário está autenticado
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // Segue o fluxo para o Controller
            filterChain.doFilter(request, response);

        } finally {
            // CRÍTICO: Previne vazamento de tenant entre requisições que reaproveitam a mesma Thread (Thread Pool)
            TenantContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove a palavra "Bearer "
        }
        return null;
    }
}