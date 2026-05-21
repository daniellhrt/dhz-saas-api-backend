-- Arquivo: src/main/resources/db/migration/V1__create_table_clients.sql
-- Propósito: Estruturar a tabela base de clientes do SaaS multi-tenant.

CREATE TABLE clients (
                         id UUID PRIMARY KEY,
                         tenant_id VARCHAR(255) NOT NULL,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL,
                         phone VARCHAR(50) NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices Críticos para Performance e Isolamento

-- 1. Acelera consultas filtradas pelo tenant_id (toda query usará isso)
CREATE INDEX idx_clients_tenant_id ON clients(tenant_id);

-- 2. Restrição de unicidade: O mesmo email não pode ser repetido DENTRO da mesma barbearia.
-- O email do cliente pode se repetir na tabela geral, desde que em barbearias (tenant_id) diferentes.
ALTER TABLE clients ADD CONSTRAINT uk_clients_tenant_email UNIQUE (tenant_id, email);