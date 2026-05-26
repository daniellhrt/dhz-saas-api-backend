-- Propósito: Adicionar coluna finalized_at para registrar o horário de conclusão da comanda/agendamento.
ALTER TABLE appointments ADD COLUMN finalized_at TIMESTAMP;
