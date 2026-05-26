# Phase 8 Discussion Log

- **User**: "ok tudo cadastrado e agora como configuro o vercel"
- **User**: `/gsd-discuss-phase 8`
- **Agent**: Reconhecido o início da Fase 8. O frontend já está deployado na Vercel no domínio `barber-saas-web-three.vercel.app` e localmente em `C:\Users\ferna\barber-saas-web`.
- **Decision (CORS)**: Definido o uso de CORS seguro e restrito apenas para o domínio da Vercel (`https://barber-saas-web-three.vercel.app`) e de testes locais (`http://localhost:3000`).
- **Integration**: Backend na GCP (`https://dhzapi-621062243139.southamerica-east1.run.app`), banco Neon DB e rate limiting com Upstash Redis.
- **Admin Setup**: Gerado comando SQL de inserção direta no Neon DB com senha BCrypt criptografada para o administrador `daniellhrt@hotmail.com` sob o tenant `salao-daniel`.
- **Validation**: Verificação de login e criação de agendamento em testes manuais E2E.
