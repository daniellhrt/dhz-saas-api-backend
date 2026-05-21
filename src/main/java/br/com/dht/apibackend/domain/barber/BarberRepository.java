package br.com.dht.apibackend.domain.barber;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarberRepository extends JpaRepository<Barber, UUID> {
    // Busca o barbeiro pelo e-mail global para realizar o login
    Optional<Barber> findByEmail(String email);
}