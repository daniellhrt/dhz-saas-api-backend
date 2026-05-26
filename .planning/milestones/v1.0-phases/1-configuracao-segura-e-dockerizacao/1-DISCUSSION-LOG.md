# Discussion Log: Phase 1 - Configuração Segura & Dockerização

*Decisões tomadas automaticamente no modo YOLO/Auto com base em melhores práticas para Spring Boot 3.*

## Areas Discussed

### Gestão de Variáveis de Ambiente e Perfis
- **Option Selected:** Usar variáveis em `application.yml` resolvidas a partir do sistema/`.env` e criar o `application-prod.yml`.
- **Notes:** O Spring Boot 3 possui excelente suporte a resolução via ambiente. Usaremos um template `.env.example` para que os desenvolvedores saibam o que preencher, mas os segredos não vão mais para o repositório.

### Estrutura do Dockerfile
- **Option Selected:** Build Multi-stage com Alpine JRE.
- **Notes:** Reduz drásticamente o tamanho da imagem e melhora a segurança ao não empacotar ferramentas de build (JDK) no runtime de produção.

## Deferred Ideas
- Nenhuma ideia adiada nesta fase técnica.
