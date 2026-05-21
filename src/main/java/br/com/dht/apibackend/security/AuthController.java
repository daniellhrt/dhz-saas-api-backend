/**
 * Propósito: Ponto de entrada REST para login.
 * Responsabilidade: Expor a rota pública para geração de tokens.
 * Papel na Arquitetura: Security / Controller.
 */
package br.com.dht.apibackend.security;

import br.com.dht.apibackend.security.dto.AuthDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthDTO.TokenResponse> login(@RequestBody @Valid AuthDTO.LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}