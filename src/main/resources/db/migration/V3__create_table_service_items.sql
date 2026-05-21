-- Propósito: Estruturar o catálogo de serviços oferecidos pelas barbearias.

CREATE TABLE service_items
(
    id               UUID PRIMARY KEY,
    tenant_id        VARCHAR(255)   NOT NULL,
    name             VARCHAR(100)   NOT NULL,
    description      VARCHAR(255),
    price            NUMERIC(10, 2) NOT NULL,
    duration_minutes INT            NOT NULL,
    active           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_items_tenant ON service_items (tenant_id);

-- Garante que o barbeiro não crie dois serviços com o mesmo nome por engano
ALTER TABLE service_items
    ADD CONSTRAINT uk_service_items_tenant_name UNIQUE (tenant_id, name);