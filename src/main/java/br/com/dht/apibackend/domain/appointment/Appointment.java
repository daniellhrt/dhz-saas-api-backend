/**
 * Propósito: Mapeamento ORM da tabela de agendamentos.
 * Responsabilidade: Relacionar o Cliente, o Serviço e o bloco de tempo, com controle de status.
 * Papel na Arquitetura: Domain / Entity.
 */
package br.com.dht.apibackend.domain.appointment;

import br.com.dht.apibackend.domain.catalog.ServiceItem;
import br.com.dht.apibackend.domain.client.Client;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_item_id", nullable = false)
    private ServiceItem serviceItem;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Setter
    @Column(name = "cancel_reason")
    private String cancelReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Appointment(String tenantId, Client client, ServiceItem serviceItem, LocalDateTime startTime, LocalDateTime endTime) {
        this.tenantId = tenantId;
        this.client = client;
        this.serviceItem = serviceItem;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AppointmentStatus.PENDING;
    }
}