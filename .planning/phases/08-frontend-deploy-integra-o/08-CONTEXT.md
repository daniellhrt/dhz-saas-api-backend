# Phase 8 Context: Frontend Deploy & Integração

## 1. Goal
Garantir que o frontend (hospedado na Vercel) esteja configurado com as variáveis de ambiente apontando para a API de produção (hospedada no Railway) e que a comunicação End-to-End esteja ocorrendo com sucesso sem erros de CORS ou rede.

## 2. Business Context
O deploy do backend foi concluído e os serviços estão em execução na nuvem (Railway). O frontend já foi previamente hospedado pelo usuário na Vercel. Esta fase marca a conexão final entre as duas pontas, viabilizando o uso do MVP.

## 3. Technical Constraints & Decisions
- O código do frontend possivelmente reside em outro repositório ou pasta não mapeada ativamente neste workspace do backend.
- A responsabilidade no backend para esta fase é apenas validar a entrada dos domínios da Vercel na propriedade `CORS_ALLOWED_ORIGINS` da nuvem, se não estiver usando o fallback `*`.
- A principal atividade aqui é o teste manual de End-to-End (E2E) relatado pelo usuário.

## 4. Required Inputs
- A URL final gerada pelo Railway (ex: `https://dhz-saas-api-production.up.railway.app`).
- A URL final do frontend na Vercel (ex: `https://barbersaas.vercel.app`).
- Confirmação de que as chamadas de API (login, agendamento) estão sendo resolvidas sem erros 403/500 no console do navegador.
