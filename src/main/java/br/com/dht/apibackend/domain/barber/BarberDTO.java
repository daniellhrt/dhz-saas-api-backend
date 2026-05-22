/**
 * Propósito: Objetos de transferência de dados do Barbeiro.
 * Responsabilidade: Validar entrada (RegisterRequest, CreateRequest, UpdateRequest) e formatar saída (Response).
 * Papel na Arquitetura: Domain / DTO.
 */
package br.com.dht.apibackend.domain.barber;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class BarberDTO {

    public record RegisterRequest(
            @NotBlank(message = "O nome é obrigatório") String name,
            @NotBlank(message = "O e-mail é obrigatório") @Email(message = "Formato de e-mail inválido") String email,
            @NotBlank(message = "A senha é obrigatória") @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres") String password
    ) {}

    public record CreateRequest(
            @NotBlank(message = "O nome é obrigatório") String name,
            @NotBlank(message = "O e-mail é obrigatório") @Email(message = "Formato de e-mail inválido") String email,
            @NotBlank(message = "A senha é obrigatória") @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres") String password
    ) {}

    public record UpdateRequest(
            String name,
            @Email(message = "Formato de e-mail inválido") String email
    ) {}

    public record Response(
            UUID id,
            String name,
            String email,
            BarberRole role
    ) {
        public static Response fromEntity(Barber barber) {
            return new Response(barber.getId(), barber.getName(), barber.getEmail(), barber.getRole());
        }
    }
}
