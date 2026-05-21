/**
 * Propósito: Definir os estados possíveis de um agendamento.
 * Papel na Arquitetura: Domain / Enum.
 */
package br.com.dht.apibackend.domain.appointment;

public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELED
}