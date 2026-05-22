/**
 * Propósito: Regra de negócio para autenticação de usuários.
 * Responsabilidade: Validar credenciais e emitir o JWT.
 * Papel na Arquitetura: Security / Service.
 */
package br.com.dht.apibackend.security;

import br.com.dht.apibackend.domain.barber.Barber;
import br.com.dht.apibackend.domain.barber.BarberRepository;
import br.com.dht.apibackend.exception.InvalidCredentialsException;
import br.com.dht.apibackend.security.dto.AuthDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final BarberRepository barberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthDTO.TokenResponse authenticate(AuthDTO.LoginRequest request) {
        // 1. Busca o barbeiro pelo e-mail
        Barber barber = barberRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas."));

        if (!passwordEncoder.matches(request.password(), barber.getPassword())) {
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        String jwt = tokenService.generateToken(barber.getEmail(), barber.getTenantId());

        return new AuthDTO.TokenResponse(jwt, "Bearer");
    }
}
