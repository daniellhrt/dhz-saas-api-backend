# Plano de Execução: Fase 8 — Frontend Deploy & Integração

Este plano descreve o checklist de tarefas e validações necessárias para consolidar a integração completa do frontend (Next.js na Vercel) com o backend (Spring Boot na Google Cloud Run), o banco de dados (Neon DB) e o rate-limiting (Upstash Redis).

---

## 🎯 1. Objetivo da Fase
Garantir que a comunicação de ponta a ponta (End-to-End) entre o frontend e o backend esteja ocorrendo perfeitamente em produção, sem barreiras de segurança (CORS) ou erros de conexão de rede, viabilizando o uso do MVP.

---

## 🛠️ 2. Abordagem de Integração

1. **Validação de CORS no Backend (GCP Cloud Run):**
   - Assegurar que a variável `CORS_ALLOWED_ORIGINS` no Cloud Run esteja preenchida exatamente com a URL de produção da Vercel (`https://barber-saas-web-three.vercel.app`) e do ambiente de desenvolvimento local (`http://localhost:3000`).

2. **Validação das Variáveis do Frontend (Vercel & Local):**
   - Garantir que a Vercel esteja compilando o frontend com a variável `NEXT_PUBLIC_API_URL` apontando para o backend GCP (`https://dhzapi-621062243139.southamerica-east1.run.app`).
   - Certificar que o arquivo local `.env.local` na pasta `C:\Users\ferna\barber-saas-web` está consumindo a mesma URL da GCP para testes híbridos.

3. **Verificação de Fluxos Críticos (E2E):**
   - **Autenticação**: Realizar login na interface usando o usuário cadastrado no Neon DB (`daniellhrt@hotmail.com` / `Dota0102@` sob o tenant `salao-daniel`) para verificar a geração, retorno e armazenamento do token JWT no browser.
   - **Banco de Dados (Neon)**: Efetuar um agendamento no sistema para validar se a persistência de dados está salvando com sucesso no Neon DB.

---

## 📋 3. Checklist de Tarefas

- [ ] **Task 1: Validar CORS no Console do Google Cloud Run**
  - Acessar o console do Cloud Run.
  - Editar as variáveis e confirmar que `CORS_ALLOWED_ORIGINS` está preenchido com `https://barber-saas-web-three.vercel.app,http://localhost:3000`.

- [ ] **Task 2: Validar Variáveis de Ambiente no Painel da Vercel**
  - Acessar as configurações de Environment Variables do projeto na Vercel.
  - Confirmar a chave `NEXT_PUBLIC_API_URL` configurada com `https://dhzapi-621062243139.southamerica-east1.run.app`.
  - Disparar um Redeploy se necessário para aplicar as variáveis.

- [ ] **Task 3: Testar Autenticação E2E (Login)**
  - Abrir o frontend (local ou produção na Vercel).
  - Preencher o formulário de login com o e-mail `daniellhrt@hotmail.com` e a senha `Dota0102@`.
  - Confirmar o login bem-sucedido e a leitura do JWT contendo o claim `tenantId` como `salao-daniel`.

- [ ] **Task 4: Validar Persistência no Neon DB (Agendamento)**
  - Realizar um novo agendamento de teste através da interface.
  - Verificar se a gravação ocorre com sucesso e o banco Neon DB registra a nova entrada de dados sem erros.

---

## ✅ 4. Critérios de Homologação (UAT)

1. **UAT-1 (Segurança de CORS)**: O console do navegador (Developer Tools) não deve apresentar nenhum erro de barreira de CORS (Access-Control-Allow-Origin) ao fazer requisições a partir do domínio oficial da Vercel.
2. **UAT-2 (Autenticação do Admin)**: O login do administrador `daniellhrt@hotmail.com` deve ser realizado com sucesso e as credenciais salvas em segurança.
3. **UAT-3 (Gravação de Dados)**: Novos agendamentos devem ser inseridos e listados corretamente a partir do Neon DB.
