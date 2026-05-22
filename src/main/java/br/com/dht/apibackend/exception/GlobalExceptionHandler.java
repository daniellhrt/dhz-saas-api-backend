/**
 * Propósito: Interceptador global de exceções da aplicação.
 * Responsabilidade: Capturar falhas e traduzi-las para o formato StandardError com o status HTTP adequado.
 * Papel na Arquitetura: Exception / Handler.
 */
package br.com.dht.apibackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<StandardError> handleInvalidCredentialsException(InvalidCredentialsException ex, HttpServletRequest request) {
        log.warn("Tentativa de login com credenciais inválidas: {}", ex.getMessage());
        
        StandardError error = StandardError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Tratamento para violações de regra de negócio (ex: E-mail duplicado)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<StandardError> handleBusinessRuleException(RuntimeException ex, HttpServletRequest request) {
        log.warn("Violação de regra de negócio: {}", ex.getMessage());
        
        StandardError error = StandardError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Business Rule Violation")
                .message(ex.getMessage()) // A mensagem limpa que definimos no Service
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Tratamento para falhas de validação de DTO (ex: @NotBlank, @Email)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Concatena todos os erros de campo em uma única string legível
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Erro de validação: {}", errors);

        StandardError error = StandardError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message(errors)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Tratamento de fallback de segurança para erros não mapeados (Impede vazamento de stacktrace)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Erro interno não tratado", ex);

        StandardError error = StandardError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ocorreu um erro inesperado. Contate o suporte.") // Mensagem genérica e segura
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}