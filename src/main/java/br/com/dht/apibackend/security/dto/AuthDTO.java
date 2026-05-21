/**
 * Propósito: Objetos de transferência para o fluxo de login.
 * Papel na Arquitetura: Security / DTO.
 */
package br.com.dht.apibackend.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDTO {
    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record TokenResponse(
            String token,
            String type // Ex: "Bearer"
    ) {}
}