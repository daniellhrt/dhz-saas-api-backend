/**
 * Propósito: Contexto transacional da requisição.
 * Responsabilidade: Armazenar o ID da barbearia (tenant) logada durante a thread atual.
 * Papel na Arquitetura: Config / Security.
 */
package br.com.dht.apibackend.config;

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}