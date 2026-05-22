/**
 * Propósito: Níveis de acesso dos barbeiros no sistema.
 * Responsabilidade: Diferenciar permissões entre ADMIN (dono da barbearia) e USER (funcionário).
 * Papel na Arquitetura: Domain / Enum.
 */
package br.com.dht.apibackend.domain.barber;

public enum BarberRole {
    ADMIN,
    USER
}
