/**
 * Propósito: Gerenciamento do ciclo de vida dos tokens JWT.
 * Responsabilidade: Criar, assinar, validar e extrair informações (claims) dos tokens.
 * Papel na Arquitetura: Security / Service.
 */
package br.com.dht.apibackend.security;

import br.com.dht.apibackend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProperties jwtProperties;
    private static final String TENANT_CLAIM = "tenantId";
    private static final String ROLE_CLAIM = "role";

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String tenantId, String role) {
        return Jwts.builder()
                .subject(email)
                .claim(TENANT_CLAIM, tenantId)
                .claim(ROLE_CLAIM, role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(getSecretKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getTenantIdFromToken(String token) {
        return getClaims(token).get(TENANT_CLAIM, String.class);
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get(ROLE_CLAIM, String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}