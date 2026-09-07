# ClimaService Web

Frontend Angular do [ClimaService](../README.md) — SaaS de gestão de serviços de climatização.
Consome a API REST do backend (Spring Boot) já existente na raiz deste repositório.

## Stack

Angular 21 (standalone, zoneless — sem `zone.js`), TypeScript, Angular Router, HttpClient, Reactive
Forms, RxJS, SCSS, Angular Material (tema Material 3, paleta azure-blue).

## Pré-requisitos

- Node.js 20.19+/22.12+/24+ e npm
- Backend do ClimaService rodando em `http://localhost:8080` (ver README da raiz)

## Instalação

```bash
npm install
```

> Use `npm install`, não `npm ci`: o `package-lock.json` deste repositório foi gerado no Windows e
> nunca resolveu o fallback WASM do `lightningcss` para Linux (pacotes `@emnapi/*`, peer dependency
> de `@napi-rs/wasm-runtime`) — `npm ci` falha com `EUSAGE` fora do Windows (confirmado tanto no
> build da imagem Docker quanto na CI). `npm install` resolve normalmente.

## Executando em desenvolvimento

```bash
npm start
```

Abre em `http://localhost:4200`. O backend precisa estar rodando e liberar CORS para essa origem
(já é o padrão em `app.cors.allowed-origins`, ver README da raiz).

## Variáveis / ambientes

A URL da API fica em `src/environments/environment.ts` (produção, usada por padrão em `ng build`) e
`src/environments/environment.development.ts` (usado por `ng serve`). Nenhum segredo é armazenado
aqui — apenas a URL base pública da API.

## Build de produção

```bash
npm run build
```

Gera os artefatos em `dist/climaservice-web/browser`.

## Lint e testes

```bash
npm run lint
npm test
```

Os testes rodam com [Vitest](https://vitest.dev/) (builder oficial do Angular CLI), sem depender de
um navegador instalado. Cobertura: todos os services (contrato HTTP), guards, o interceptor de
autenticação/erros, `AuthService` e uma amostra de componentes representando os padrões de lista e
formulário usados nas telas de negócio.

## Docker

```bash
docker build -t climaservice-web .
docker run -p 4200:8080 climaservice-web
```

Build multi-stage (Node para compilar, nginx não-root para servir os artefatos estáticos com
fallback de SPA). Também disponível via `docker compose up` na raiz do repositório, junto com a API
e o PostgreSQL.

## CI

GitHub Actions (`.github/workflows/frontend-ci.yml`) roda lint, testes e build a cada push/PR para
`main` que toque em `frontend/**`.

## Estrutura

```text
src/app
  core/
    guards/         # authGuard, guestGuard, roleGuard
    interceptors/   # jwtInterceptor — Authorization header + tratamento global de erros HTTP
    models/         # interfaces espelhando os DTOs do backend, 1:1 por módulo
    services/       # um service HTTP por módulo (mesmo nome dos models)
    utils/          # extractErrorMessage, conversão de Date para filtro de período
  shared/
    components/     # confirm-dialog, empty-state, error-message, loading, not-found, acesso-negado
  layout/            # casca da aplicação: sidebar, header, main-layout
  features/
    auth/            # tela de login
    dashboard/       # resumo, financeiro e operacional
    clientes/
    equipamentos/
    ordens-servico/
    agendamentos/
    manutencoes-preventivas/
    servicos/
    produtos/
    orcamentos/
    pagamentos/
    usuarios/
    empresa/
```

Cada módulo de negócio segue o mesmo padrão: uma lista paginada (ou não, quando o backend não pagina
o endpoint) com filtros, um formulário de criação/edição quando o backend permite, e uma tela de
detalhe para os módulos com workflow de status (ordens de serviço, agendamentos, orçamentos).
Módulos sem endpoint de edição (usuários, planos de manutenção quanto ao equipamento) refletem essa
mesma limitação na UI, em vez de simular algo que o backend não suporta.

## Status

Frontend V1 concluído: as 12 áreas de negócio do backend, autenticação, controle de acesso por
perfil (visual — a autorização real é sempre do backend), 404 e acesso-negado dedicados, tratamento
global de erros HTTP, testes unitários, containerização e CI. Ver o roadmap completo no `README.md`
da raiz do projeto.
