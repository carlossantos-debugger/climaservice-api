# ClimaService Web

Frontend Angular do [ClimaService](../README.md) — SaaS de gestão de serviços de climatização.
Consome a API REST do backend (Spring Boot) já existente na raiz deste repositório.

> Em construção, branch por branch. Ver o roadmap completo no `README.md` da raiz do projeto.
> Um README completo desta pasta (estrutura, integração com o backend, funcionalidades) chega
> na branch `release/frontend-v1`.

## Stack

Angular 21 (standalone, zoneless), TypeScript, Angular Router, HttpClient, Reactive Forms, RxJS,
SCSS, Angular Material (tema Material 3).

## Pré-requisitos

- Node.js 20.19+/22.12+/24+ e npm
- Backend do ClimaService rodando em `http://localhost:8080` (ver README da raiz)

## Instalação

```bash
npm ci
```

## Executando em desenvolvimento

```bash
npm start
```

Abre em `http://localhost:4200`. O backend precisa estar rodando e liberar CORS para essa origem
(já é o padrão em `app.cors.allowed-origins`, ver README da raiz).

## Variáveis / ambientes

A URL da API fica em `src/environments/environment.ts` (produção) e
`src/environments/environment.development.ts` (usado por `ng serve`). Nenhum segredo é armazenado
aqui — apenas a URL base pública da API.

## Build de produção

```bash
npm run build
```

Gera os artefatos em `dist/climaservice-web`.

## Lint e testes

```bash
npm run lint
npm test
```

Os testes rodam com [Vitest](https://vitest.dev/) (builder oficial do Angular CLI), sem depender de
um navegador instalado.

## Estrutura

```text
src/app
  core/       # guards, models e (em breve) auth/interceptors/services — infraestrutura sem UI
  shared/     # componentes, pipes, diretivas e validators reutilizáveis entre features
  features/   # uma pasta por módulo de negócio (clientes, ordens-servico, ...) — chega a partir da branch 4
  layout/     # casca da aplicação: sidebar, header, main-layout
```
