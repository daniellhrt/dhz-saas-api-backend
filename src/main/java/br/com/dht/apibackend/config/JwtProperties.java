/**
 * Propósito: Mapear as configurações customizadas do application.yml.
 * Responsabilidade: Fornecer os valores do JWT de forma fortemente tipada.
 * Papel na Arquitetura: Config.
 */
package br.com.dht.apibackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.security.jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private Long expirationMs;
}