/**
 * Propósito: Estrutura padronizada para respostas de erro da API.
 * Responsabilidade: Formatar o payload JSON devolvido ao frontend quando ocorre uma falha.
 * Papel na Arquitetura: Exception / DTO.
 */
package br.com.dht.apibackend.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StandardError {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
}