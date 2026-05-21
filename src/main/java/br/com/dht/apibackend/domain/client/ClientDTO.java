/**
 * Propósito: Objetos de transferência de dados do Cliente.
 * Responsabilidade: Validar entrada (Request) e formatar saída (Response).
 * Papel na Arquitetura: Domain / DTO.
 */
package br.com.dht.apibackend.domain.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class ClientDTO {

    public record Request(
            @NotBlank(message = "O nome é obrigatório") String name,
            @NotBlank(message = "O e-mail é obrigatório") @Email(message = "Formato de e-mail inválido") String email,
            @NotBlank(message = "O telefone é obrigatório") String phone
    ) {}

    public record Response(
            UUID id,
            String name,
            String email,
            String phone
            // Não retornamos o tenantId nem o createdAt no MVP para simplificar o payload
    ) {
        public static Response fromEntity(Client client) {
            return new Response(client.getId(), client.getName(), client.getEmail(), client.getPhone());
        }
    }
}