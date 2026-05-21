-- Propósito: Estruturar a tabela de agendamentos relacionando clientes e serviços.

CREATE TABLE appointments
(
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(255) NOT NULL,
    client_id       UUID         NOT NULL,
    service_item_id UUID         NOT NULL,
    start_time      TIMESTAMP    NOT NULL,
    end_time        TIMESTAMP    NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_appointments_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_appointments_service FOREIGN KEY (service_item_id) REFERENCES service_items (id)
);

-- Índices Críticos
-- 1. Agiliza a busca de agendamentos por tenant (usado na agenda do dia)
CREATE INDEX idx_appointments_tenant_time ON appointments (tenant_id, start_time);

-- 2. Agiliza a busca do histórico do cliente
CREATE INDEX idx_appointments_client ON appointments (client_id);