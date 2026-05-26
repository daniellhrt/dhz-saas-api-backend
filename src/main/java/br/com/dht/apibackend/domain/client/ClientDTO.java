/**
 * Propósito: Objetos de transferência de dados do Cliente.
 * Responsabilidade: Validar entrada (Request) e formatar saída (Response).
 * Papel na Arquitetura: Domain / DTO.
 */
package br.com.dht.apibackend.domain.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public class ClientDTO {

    public record Request(
            @NotBlank(message = "O nome é obrigatório") String name,
            @NotBlank(message = "O e-mail é obrigatório") @Email(message = "Formato de e-mail inválido") String email,
            @NotBlank(message = "O telefone é obrigatório") @Pattern(regexp = "^\\(\\d{2}\\)\\s?9?\\d{4}-?\\d{4}$", message = "Formato de telefone inválido (ex: (11) 99999-8888)") String phone,
            String cpf,
            java.time.LocalDate birthDate,
            String notes
    ) {}

    public record Response(
            UUID id,
            String name,
            String email,
            String phone,
            String cpf,
            java.time.LocalDate birthDate,
            String notes
    ) {
        public static Response fromEntity(Client client) {
            return new Response(client.getId(), client.getName(), client.getEmail(), client.getPhone(), client.getCpf(), client.getBirthDate(), client.getNotes());
        }
    }
}