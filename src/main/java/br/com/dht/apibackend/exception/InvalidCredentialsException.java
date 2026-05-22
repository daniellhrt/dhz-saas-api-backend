/**
 * Propósito: Exceção para credenciais inválidas no login.
 * Responsabilidade: Sinalizar falha de autenticação com mensagem personalizada.
 * Papel na Arquitetura: Exception / Model.
 */
package br.com.dht.apibackend.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}