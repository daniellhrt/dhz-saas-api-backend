# Phase 8 Context: Frontend Deploy & Integração

## 1. Goal
Garantir que o frontend (hospedado na Vercel) esteja configurado com as variáveis de ambiente apontando para a API de produção (hospedada no Google Cloud Run) e que a comunicação End-to-End esteja ocorrendo com sucesso sem erros de CORS ou rede.

## 2. Business Context
O deploy do backend foi concluído com sucesso e os serviços estão em execução ativa na nuvem da Google Cloud (Cloud Run) com persistência de dados no Neon DB. O frontend foi hospedado na Vercel. Esta fase marca a conexão final entre as duas pontas, viabilizando o uso do MVP completo.

## 3. Technical Constraints & Decisions
- **CORS Lock (Segurança)**: Ficou decidido que a política de CORS da API no Cloud Run será restrita apenas ao domínio de produção da Vercel (`https://barber-saas-web-three.vercel.app`) e ao ambiente de testes local (`http://localhost:3000`), garantindo proteção corporativa contra acessos não autorizados.
- **Frontend Host**: Vercel (Domínio de produção: `barber-saas-web-three.vercel.app`).
- **Backend Host**: Google Cloud Run (URL de produção: `dhzapi-621062243139.southamerica-east1.run.app`).
- **Banco de Dados**: Neon DB (PostgreSQL Serverless).
- **Redis (Rate Limiting)**: Upstash Redis (Serverless).

## 4. Required Inputs
- A URL final gerada pelo Cloud Run: `https://dhzapi-621062243139.southamerica-east1.run.app`.
- A URL do frontend na Vercel: `https://barber-saas-web-three.vercel.app`.
- Confirmação de que as chamadas de API (login, listagens, agendamentos) estão funcionando de ponta a ponta sem erros de rede.

## 5. Verification Plan (E2E Flows)
- **Autenticação Completa**: Login do Administrador (`daniellhrt@hotmail.com` com tenant `salao-daniel`), verificação e decodificação do JWT token e resolução correta do Tenant.
- **Agendamento Completo**: Criação de um novo agendamento e verificação de concorrência ou conflitos de horário.
- **Segurança de CORS**: Garantir que chamadas de origens estranhas ou domínios não mapeados sejam devidamente rejeitadas pelo backend.
