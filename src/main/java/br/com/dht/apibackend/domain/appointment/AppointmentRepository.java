/**
 * Propósito: Acesso a dados para agendamentos.
 * Responsabilidade: Executar a query de intersecção para evitar Double-Booking.
 * Papel na Arquitetura: Domain / Repository.
 */
package br.com.dht.apibackend.domain.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // Lógica de Overlap: Se o novo início for ANTES de um fim existente, 
    // E o novo fim for DEPOIS de um início existente, há conflito.
    // Ignora agendamentos cancelados.
    @Query("""
           SELECT COUNT(a) > 0 FROM Appointment a 
           WHERE a.tenantId = :tenantId 
           AND a.status != 'CANCELED' 
           AND a.startTime < :newEndTime 
           AND a.endTime > :newStartTime
           """)
    boolean hasOverlappingAppointment(
            @Param("tenantId") String tenantId,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime
    );
}