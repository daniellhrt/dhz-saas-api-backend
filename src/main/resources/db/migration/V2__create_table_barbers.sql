-- Propósito: Estruturar a tabela de usuários (barbeiros) do sistema.

CREATE TABLE barbers (
                         id UUID PRIMARY KEY,
                         tenant_id VARCHAR(255) NOT NULL,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE, -- Login deve ser único globalmente
                         password VARCHAR(255) NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_barbers_tenant_id ON barbers(tenant_id);