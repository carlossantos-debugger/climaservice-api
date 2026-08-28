# ClimaService API

API REST para gerenciamento de serviços de climatização e manutenção de ar-condicionado.

O **ClimaService** está sendo desenvolvido como um projeto SaaS voltado para empresas e profissionais que trabalham com instalação, manutenção preventiva e manutenção corretiva de equipamentos de climatização.

O objetivo é construir uma aplicação completa utilizando **Java, Spring Boot, Angular e PostgreSQL**, aplicando conceitos e práticas utilizadas no desenvolvimento de sistemas reais, como arquitetura em camadas, regras de negócio, validações, relacionamentos entre entidades, tratamento global de erros, segurança com JWT, autorização por perfis, workflows de status, versionamento de banco com Flyway, testes automatizados e isolamento multi-tenant.

---

## Tecnologias

### Backend

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation
- PostgreSQL
- Gradle
- Git
- GitHub
- Spring Security
- JWT
- BCrypt
- Flyway

### Testes

- JUnit 5
- Mockito
- Testcontainers
- PostgreSQL real em container para testes de integração

### Planejadas

- OpenAPI / Swagger
- Docker
- Docker Compose
- GitHub Actions
- Angular

---

# Funcionalidades implementadas

Atualmente, a API possui os seguintes módulos:

- Clientes
- Equipamentos
- Ordens de Serviço
- Histórico de Ordens de Serviço
- Agenda de Atendimentos
- Catálogo de Serviços
- Orçamentos
- Itens de Orçamento
- Catálogo de Produtos e Peças
- Pagamentos
- Usuários
- Autenticação e Autorização
- Empresas e contexto de tenant
- Migrações versionadas com Flyway
- Testes unitários e de integração

Além dos CRUDs, o sistema já possui regras de negócio relacionadas ao ciclo de vida das ordens de serviço, orçamentos e pagamentos, autenticação com JWT, controle de acesso baseado em perfis, auditoria de alterações e isolamento de dados por empresa nos principais módulos de negócio.

---

# Clientes

Permite gerenciar os clientes atendidos pela empresa.

### Dados do cliente

- ID
- Nome
- CPF/CNPJ
- Telefone
- E-mail

### Funcionalidades

- Cadastro de clientes
- Listagem de clientes
- Consulta por ID
- Atualização
- Exclusão
- Validação dos dados de entrada

### Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/clientes` | Cadastrar cliente |
| GET | `/clientes` | Listar clientes |
| GET | `/clientes/{id}` | Buscar cliente por ID |
| PUT | `/clientes/{id}` | Atualizar cliente |
| DELETE | `/clientes/{id}` | Excluir cliente |

### Exemplo de cadastro

```http
POST /clientes
```

```json
{
  "nome": "João da Silva",
  "cpfCnpj": "12345678901",
  "telefone": "47999999999",
  "email": "joao@email.com"
}
```

Resposta:

```text
201 Created
```

---

# Equipamentos

Cada equipamento pertence a um cliente e representa uma unidade física de climatização atendida pela empresa.

### Dados principais

- ID
- Marca
- Modelo
- Capacidade em BTUs
- Número de série
- Local de instalação
- Cliente
- Status

### Status

```text
ATIVO
INATIVO
```

### Funcionalidades

- Cadastro de equipamentos
- Associação do equipamento ao cliente
- Consulta por ID
- Consulta de equipamentos por cliente
- Consulta somente de equipamentos ativos
- Atualização
- Ativação
- Inativação
- Validação da existência do cliente

### Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/equipamentos` | Cadastrar equipamento |
| GET | `/equipamentos` | Listar equipamentos |
| GET | `/equipamentos/{id}` | Buscar equipamento |
| PUT | `/equipamentos/{id}` | Atualizar equipamento |
| GET | `/clientes/{clienteId}/equipamentos` | Equipamentos do cliente |
| GET | `/clientes/{clienteId}/equipamentos/ativos` | Equipamentos ativos do cliente |
| PATCH | `/equipamentos/{id}/ativar` | Ativar equipamento |
| PATCH | `/equipamentos/{id}/inativar` | Inativar equipamento |

Equipamentos inativos permanecem armazenados para preservar seu histórico.

---

# Ordens de Serviço

As Ordens de Serviço representam os atendimentos realizados em equipamentos dos clientes.

Uma OS possui vínculo com:

```text
Cliente
   ↓
Equipamento
   ↓
Ordem de Serviço
```

### Dados principais

- ID
- Cliente
- Equipamento
- Descrição do problema
- Diagnóstico
- Status
- Data de abertura
- Data de conclusão

### Status

```text
ABERTA
EM_ANDAMENTO
AGUARDANDO_CLIENTE
CONCLUIDA
CANCELADA
```

### Fluxo de status

```text
ABERTA
   ├──→ EM_ANDAMENTO
   └──→ CANCELADA

EM_ANDAMENTO
   ├──→ AGUARDANDO_CLIENTE
   ├──→ CONCLUIDA
   └──→ CANCELADA

AGUARDANDO_CLIENTE
   ├──→ EM_ANDAMENTO
   └──→ CANCELADA

CONCLUIDA
   └── estado final

CANCELADA
   └── estado final
```

### Regras de negócio

- O equipamento informado deve pertencer ao cliente informado.
- Equipamentos inativos não podem receber novas Ordens de Serviço.
- Toda nova OS começa com status `ABERTA`.
- A data de abertura é definida automaticamente pelo backend.
- A data de conclusão é definida automaticamente quando a OS é concluída.
- Ordens concluídas ou canceladas não podem retornar para estados anteriores.
- O diagnóstico não pode ser alterado após conclusão ou cancelamento.
- Todas as transições de status são validadas pelo backend.

### Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/ordens-servico` | Abrir OS |
| GET | `/ordens-servico` | Listar OS |
| GET | `/ordens-servico/{id}` | Buscar OS |
| GET | `/clientes/{clienteId}/ordens-servico` | OS de um cliente |
| GET | `/equipamentos/{equipamentoId}/ordens-servico` | Histórico de OS do equipamento |
| PATCH | `/ordens-servico/{id}/diagnostico` | Registrar/alterar diagnóstico |
| PATCH | `/ordens-servico/{id}/status` | Alterar status |
| GET | `/ordens-servico/{id}/historico` | Consultar histórico de status |

---

# Histórico de Ordens de Serviço

As alterações de status de uma Ordem de Serviço são registradas separadamente.

Exemplo:

```text
OS #15

ABERTA
   ↓
EM_ANDAMENTO
   ↓
AGUARDANDO_CLIENTE
   ↓
EM_ANDAMENTO
   ↓
CONCLUIDA
```

Cada registro armazena:

- Status anterior
- Novo status
- Data da alteração
- Usuário responsável pela alteração

O histórico funciona como trilha de auditoria das transições de status e permite identificar quem realizou cada alteração relevante.

---

# Agenda de Atendimentos

Cada Ordem de Serviço pode ter um ou mais agendamentos, vinculando um técnico a uma janela de data/hora.

```text
OrdemServico
   ↓
Agendamento
   ├── Técnico responsável
   ├── Data/hora de início e fim
   └── Status
```

### Dados principais

- ID
- Ordem de Serviço
- Técnico
- Data/hora de início
- Data/hora de fim
- Status
- Observação
- Data de criação

### Status

```text
AGENDADO
CONFIRMADO
EM_ATENDIMENTO
CONCLUIDO
CANCELADO
```

### Fluxo de status

```text
AGENDADO
   ├──→ CONFIRMADO
   └──→ CANCELADO

CONFIRMADO
   ├──→ EM_ATENDIMENTO
   └──→ CANCELADO

EM_ATENDIMENTO
   ├──→ CONCLUIDO
   └──→ CANCELADO

CONCLUIDO
   └── estado final

CANCELADO
   └── estado final
```

### Regras de negócio

- A ordem de serviço informada precisa pertencer à empresa autenticada.
- Ordens de serviço `CANCELADA` ou `CONCLUIDA` não podem receber novos agendamentos.
- O técnico informado precisa pertencer à empresa autenticada, possuir o perfil `TECNICO` e estar ativo.
- A data/hora de fim precisa ser posterior à data/hora de início.
- Não é permitida sobreposição de horário entre agendamentos ativos (`AGENDADO`, `CONFIRMADO`, `EM_ATENDIMENTO`) do mesmo técnico.
- Todo novo agendamento começa como `AGENDADO`.
- Agendamentos `CONCLUIDO` ou `CANCELADO` são estados finais e não podem ser reagendados nem mudar de status.
- Cada alteração de status é registrada em um histórico com o usuário responsável.

### Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/agendamentos` | Criar agendamento (`ADMIN`, `ATENDENTE`) |
| GET | `/agendamentos` | Listar agendamentos da empresa, com filtros opcionais (`dataInicial`, `dataFinal`, `tecnicoId`, `status`) |
| GET | `/agendamentos/{id}` | Buscar agendamento por ID |
| PATCH | `/agendamentos/{id}/status` | Alterar status (`ADMIN`, `TECNICO`) |
| PATCH | `/agendamentos/{id}/reagendar` | Alterar data/hora do agendamento (`ADMIN`, `ATENDENTE`) |
| GET | `/agendamentos/{id}/historico` | Consultar histórico de status |
| GET | `/tecnicos/{tecnicoId}/agendamentos` | Agenda de um técnico |
| GET | `/ordens-servico/{ordemServicoId}/agendamentos` | Agendamentos de uma ordem de serviço |

---

# Catálogo de Serviços

O sistema possui um catálogo de serviços oferecidos pela empresa.

Exemplos:

- Limpeza completa
- Instalação
- Desinstalação
- Manutenção preventiva
- Recarga
- Troca de componentes

### Dados

- ID
- Nome
- Descrição
- Valor padrão
- Ativo

Os valores monetários utilizam `BigDecimal`.

### Funcionalidades

- Cadastro
- Listagem
- Consulta por ID
- Atualização
- Ativação
- Inativação
- Consulta somente de serviços ativos

### Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/servicos` | Cadastrar serviço |
| GET | `/servicos` | Listar todos |
| GET | `/servicos/ativos` | Listar ativos |
| GET | `/servicos/{id}` | Buscar serviço |
| PUT | `/servicos/{id}` | Atualizar |
| PATCH | `/servicos/{id}/ativar` | Ativar |
| PATCH | `/servicos/{id}/inativar` | Inativar |

---

# Produtos e Peças

Produtos representam peças ou materiais utilizados na execução dos serviços.

Exemplos:

- Capacitor 35uF
- Sensor de temperatura
- Placa eletrônica
- Controle remoto

### Dados

- ID
- Nome
- Descrição
- Valor padrão
- Ativo

### Funcionalidades

- Cadastro de produtos
- Listagem
- Consulta por ID
- Atualização
- Ativação
- Inativação
- Consulta somente de produtos ativos
- Inclusão de produtos em orçamentos
- Utilização de valor padrão ou valor negociado

### Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/produtos` | Cadastrar produto |
| GET | `/produtos` | Listar todos |
| GET | `/produtos/ativos` | Listar ativos |
| GET | `/produtos/{id}` | Buscar produto |
| PUT | `/produtos/{id}` | Atualizar |
| PATCH | `/produtos/{id}/ativar` | Ativar |
| PATCH | `/produtos/{id}/inativar` | Inativar |

Produtos inativos continuam armazenados para preservar referências históricas em orçamentos antigos.

---

# Orçamentos

Uma Ordem de Serviço pode possuir múltiplos orçamentos.

Exemplo:

```text
OS #15
│
├── Orçamento #1
│   └── REJEITADO
│
└── Orçamento #2
    └── APROVADO
```

### Dados principais

- ID
- Ordem de Serviço
- Status
- Valor total
- Data de criação
- Data de envio
- Data de resposta
- Observação

### Status

```text
RASCUNHO
ENVIADO
APROVADO
REJEITADO
CANCELADO
```

### Workflow

```text
RASCUNHO
   │
   ├──→ ENVIADO
   │       │
   │       ├──→ APROVADO
   │       └──→ REJEITADO
   │
   └──→ CANCELADO
```

### Regras de negócio

- Todo orçamento começa como `RASCUNHO`.
- Um orçamento pode ser criado somente para uma OS válida.
- Ordens concluídas ou canceladas não podem receber novos orçamentos.
- Um orçamento sem itens não pode ser enviado.
- Somente orçamentos em `RASCUNHO` podem ser alterados.
- Após o envio, os itens ficam bloqueados para edição.
- Orçamentos aprovados, rejeitados ou cancelados são estados finais.
- A data de envio é definida automaticamente.
- A data de resposta é definida automaticamente após aprovação ou rejeição.
- O frontend não define o valor total do orçamento.
- O total é calculado pelo backend a partir dos itens.

### Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/ordens-servico/{ordemServicoId}/orcamentos` | Criar orçamento |
| GET | `/ordens-servico/{ordemServicoId}/orcamentos` | Listar orçamentos da OS |
| GET | `/orcamentos/{id}` | Buscar orçamento |
| PATCH | `/orcamentos/{id}/status` | Alterar status |
| GET | `/orcamentos/{orcamentoId}/itens` | Listar itens |

---

# Itens de Orçamento

Um orçamento pode possuir dois tipos de itens:

```text
SERVICO
PECA
```

Estrutura:

```text
Orcamento
   ↓
OrcamentoItem
   ├── SERVICO → Servico
   └── PECA    → Produto
```

Cada item possui:

- Tipo
- Descrição
- Quantidade
- Valor unitário
- Subtotal
- Serviço ou produto relacionado

### Inclusão de serviço

```http
POST /orcamentos/{orcamentoId}/itens/servicos
```

Exemplo:

```json
{
  "servicoId": 1,
  "quantidade": 2,
  "valorUnitario": 140.00
}
```

Caso `valorUnitario` não seja informado, o backend utiliza o valor padrão cadastrado no serviço.

---

### Inclusão de produto/peça

```http
POST /orcamentos/{orcamentoId}/itens/produtos
```

Exemplo:

```json
{
  "produtoId": 1,
  "quantidade": 2,
  "valorUnitario": 40.00
}
```

Caso o valor não seja informado, o valor padrão do produto é utilizado.

---

### Atualizar item

```http
PUT /orcamentos/{orcamentoId}/itens/{itemId}
```

---

### Remover item

```http
DELETE /orcamentos/{orcamentoId}/itens/{itemId}
```

---

## Cálculo de valores

O frontend não informa o subtotal nem o total final.

O subtotal é calculado através de:

```text
quantidade × valorUnitario = subtotal
```

Exemplo:

```text
2 × R$ 140,00
=
R$ 280,00
```

O total do orçamento é calculado pela soma dos subtotais:

```text
Serviço          R$ 280,00
Instalação       R$ 350,00
Peça              R$ 45,00
--------------------------
TOTAL            R$ 675,00
```

Os cálculos monetários utilizam `BigDecimal`.

---

# Snapshot de preços

Os itens do orçamento armazenam uma cópia do valor utilizado naquele momento.

Por exemplo:

```text
Serviço cadastrado:

Limpeza
Valor atual: R$ 150
```

Um orçamento pode registrar:

```text
Limpeza
Valor negociado: R$ 130
```

Se futuramente o valor padrão do serviço mudar para R$ 180, o orçamento antigo continua com:

```text
R$ 130
```

Isso preserva o histórico financeiro dos orçamentos.

---

# Pagamentos

O módulo de pagamentos registra valores recebidos a partir de orçamentos aprovados.

Um orçamento pode possuir múltiplos pagamentos, permitindo o controle de pagamentos parciais.

### Formas de pagamento

```text
DINHEIRO
PIX
CARTAO_CREDITO
CARTAO_DEBITO
BOLETO
TRANSFERENCIA
```

### Status

```text
PENDENTE
CONFIRMADO
CANCELADO
```

### Regras de negócio

- Somente orçamentos com status `APROVADO` podem receber pagamentos.
- Um orçamento pode possuir múltiplos pagamentos.
- Todo novo pagamento começa como `PENDENTE`.
- Apenas pagamentos pendentes podem ser confirmados ou cancelados.
- Pagamentos confirmados e pendentes comprometem o saldo disponível para novos registros.
- O sistema impede que a soma dos pagamentos ultrapasse o valor total do orçamento.
- A data de criação é registrada automaticamente.
- As datas de confirmação e cancelamento são registradas automaticamente.
- Pagamentos cancelados permanecem armazenados para preservar o histórico financeiro.

### Resumo financeiro

O backend calcula informações como:

- Valor total do orçamento
- Total confirmado
- Total pendente
- Saldo restante
- Valor disponível para um novo pagamento

### Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/orcamentos/{orcamentoId}/pagamentos` | Registrar pagamento |
| GET | `/orcamentos/{orcamentoId}/pagamentos` | Listar pagamentos do orçamento |
| GET | `/orcamentos/{orcamentoId}/pagamentos/resumo` | Consultar resumo financeiro |
| GET | `/pagamentos/{id}` | Buscar pagamento por ID |
| PATCH | `/pagamentos/{id}/confirmar` | Confirmar pagamento |
| PATCH | `/pagamentos/{id}/cancelar` | Cancelar pagamento |

---

# Usuários, Autenticação e Autorização

A API utiliza **Spring Security**, **BCrypt** e **JWT** para autenticação e controle de acesso.

### Perfis de usuário

```text
ADMIN
ATENDENTE
TECNICO
```

### Segurança de senhas

As senhas não são armazenadas em texto puro. O backend utiliza `BCryptPasswordEncoder` para gerar e validar o hash das credenciais.

```text
senha recebida
      ↓
BCrypt
      ↓
senha_hash
      ↓
PostgreSQL
```

### Onboarding de empresas

Uma nova empresa entra na aplicação através de um cadastro público, que cria a `Empresa` e seu primeiro usuário `ADMIN` na mesma transação:

```http
POST /auth/register-company
```

```json
{
  "empresaNome": "ClimaService Instalações",
  "empresaCpfCnpj": "12345678000199",
  "adminNome": "Maria Souza",
  "adminEmail": "maria@climaservice.com",
  "adminSenha": "senhaSegura123"
}
```

Regras:

- Endpoint público (não exige autenticação).
- A empresa é criada já ativa e o administrador já ativo.
- A senha é armazenada com `BCryptPasswordEncoder`.
- O e-mail do administrador é normalizado e precisa ser único em toda a aplicação.
- O CPF/CNPJ da empresa é opcional, mas quando informado deve possuir 11 ou 14 dígitos e ser único entre empresas.
- O cliente da API nunca informa `empresaId`: o tenant nasce junto com o cadastro.
- Qualquer falha (e-mail ou CPF/CNPJ duplicado, validação) desfaz o cadastro por completo — empresa e usuário não ficam órfãos.
- A resposta já inclui um JWT, permitindo que o frontend autentique o administrador imediatamente após o cadastro, sem precisar de um segundo login.

A empresa autenticada também pode consultar e atualizar seus próprios dados:

```http
GET   /empresa/me
PATCH /empresa/me
```

`GET /empresa/me` está disponível para qualquer perfil autenticado. `PATCH /empresa/me` é restrito ao perfil `ADMIN` e permite alterar nome e CPF/CNPJ da própria empresa, reaplicando a mesma validação de unicidade de CPF/CNPJ. Ambos os endpoints operam exclusivamente sobre a empresa do usuário autenticado — não é possível consultar ou alterar dados de outro tenant.

### Login e JWT

O login é realizado por e-mail e senha:

```http
POST /auth/login
```

Após a validação das credenciais, a API retorna um JWT. Nas próximas requisições, o cliente envia:

```http
Authorization: Bearer <token>
```

O `JwtAuthFilter` valida a assinatura e a expiração do token, identifica o usuário, verifica se a conta continua ativa e registra a autenticação no `SecurityContext`.

A aplicação utiliza `SessionCreationPolicy.STATELESS`, portanto o servidor não mantém sessão de login.

### Usuários ativos e inativos

Usuários podem ser inativados sem exclusão física. Usuários inativos não podem se autenticar, preservando referências futuras de histórico e auditoria.

### Endpoints de usuários e autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/auth/login` | Autenticar usuário e gerar JWT |
| POST | `/auth/register-company` | Cadastrar empresa e seu primeiro administrador (público) |
| GET | `/empresa/me` | Consultar dados da empresa autenticada |
| PATCH | `/empresa/me` | Atualizar dados da empresa autenticada (`ADMIN`) |
| POST | `/usuarios` | Cadastrar usuário |
| GET | `/usuarios` | Listar usuários |
| GET | `/usuarios/{id}` | Buscar usuário |
| PATCH | `/usuarios/{id}/ativar` | Ativar usuário |
| PATCH | `/usuarios/{id}/inativar` | Inativar usuário |

O gerenciamento de usuários é restrito ao perfil `ADMIN`.

### Matriz de permissões

| Operação | ADMIN | ATENDENTE | TECNICO |
|---|:---:|:---:|:---:|
| Cadastrar empresa (onboarding) | público | público | público |
| Consultar dados da própria empresa | ✅ | ✅ | ✅ |
| Atualizar dados da própria empresa | ✅ | ❌ | ❌ |
| Gerenciar usuários | ✅ | ❌ | ❌ |
| Consultar clientes | ✅ | ✅ | ✅ |
| Cadastrar/alterar clientes | ✅ | ✅ | ❌ |
| Excluir cliente | ✅ | ❌ | ❌ |
| Consultar equipamentos | ✅ | ✅ | ✅ |
| Gerenciar equipamentos | ✅ | ✅ | ❌ |
| Abrir OS | ✅ | ✅ | ❌ |
| Consultar OS | ✅ | ✅ | ✅ |
| Registrar diagnóstico | ✅ | ❌ | ✅ |
| Alterar status da OS | ✅ | ❌ | ✅ |
| Consultar serviços/produtos | ✅ | ✅ | ✅ |
| Gerenciar serviços/produtos | ✅ | ✅ | ❌ |
| Consultar orçamentos | ✅ | ✅ | ✅ |
| Gerenciar orçamentos | ✅ | ✅ | ❌ |
| Consultar pagamentos | ✅ | ✅ | ✅ |
| Registrar/confirmar/cancelar pagamentos | ✅ | ✅ | ❌ |
| Consultar agendamentos | ✅ | ✅ | ✅ |
| Criar/reagendar agendamentos | ✅ | ✅ | ❌ |
| Alterar status do agendamento | ✅ | ❌ | ✅ |

As autorizações específicas são aplicadas com `@PreAuthorize`, enquanto a `SecurityConfig` define as regras globais de autenticação.

---

# Multi-tenancy

O ClimaService utiliza uma estratégia **shared database / shared schema**, em que diferentes empresas utilizam a mesma aplicação e o mesmo schema PostgreSQL, mas os dados são isolados pelo tenant.

O tenant é representado pela entidade `Empresa` e é obtido a partir do usuário autenticado. O cliente da API não envia `empresaId` nos DTOs para escolher o tenant da operação.

```text
Usuário autenticado
        ↓
      Empresa
        ↓
   dados do tenant
```

O isolamento por empresa já é aplicado aos principais módulos de negócio:

- Clientes
- Equipamentos
- Ordens de Serviço
- Serviços
- Produtos e Peças
- Orçamentos
- Itens de Orçamento
- Pagamentos
- Históricos associados a esses recursos

As consultas sensíveis utilizam métodos de repository filtrados por empresa. Dessa forma, um recurso pertencente a outro tenant é tratado como inexistente para o usuário atual.

Exemplo:

```text
Empresa A                       Empresa B
├── Cliente A                   ├── Cliente B
├── Equipamento A               ├── Equipamento B
├── OS A                        ├── OS B
├── Orçamento A                 ├── Orçamento B
└── Pagamento A                 └── Pagamento B

Usuário da Empresa A
        ↓
não acessa recursos da Empresa B
```

No módulo de pagamentos, o tenant é validado através do relacionamento:

```text
Pagamento
   ↓
Orçamento
   ↓
Empresa
```

Os usuários já possuem vínculo com `Empresa`. A revisão final do isolamento das operações administrativas de `UsuarioService` é a próxima etapa de segurança antes de avançar para novas funcionalidades.

---

# Validações

Os dados recebidos pela API são validados utilizando **Jakarta Bean Validation**.

Entre as annotations utilizadas estão:

```java
@NotNull
@NotBlank
@Size
@Email
@Pattern
@Positive
@DecimalMin
```

Exemplos de validações:

- Campos obrigatórios
- Limites de caracteres
- CPF/CNPJ com 11 ou 14 dígitos
- Formato de e-mail
- Quantidades maiores que zero
- Valores monetários positivos

---

# Tratamento de erros

A aplicação possui tratamento global de exceções utilizando:

```java
@RestControllerAdvice
```

## Recurso não encontrado

Exemplo:

```text
404 Not Found
```

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Produto com ID 999 não encontrado",
  "path": "/produtos/999"
}
```

---

## Violação de regra de negócio

Exemplo:

```text
400 Bad Request
```

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Somente orçamentos em rascunho podem ser alterados",
  "path": "/orcamentos/1/itens/produtos"
}
```

---

## Erro de validação

Exemplo:

```text
400 Bad Request
```

```json
{
  "status": 400,
  "error": "Validation Error",
  "fields": {
    "quantidade": "A quantidade deve ser maior que zero"
  }
}
```

---

# Arquitetura

O backend utiliza arquitetura em camadas e uma cadeia de segurança para as rotas protegidas.

```text
HTTP Request
     ↓
JwtAuthFilter
     ↓
Spring Security / SecurityContext
     ↓
@PreAuthorize
     ↓
Controller
     ↓
Request DTO
     ↓
Service
     ↓
Repository
     ↓
JPA / Hibernate
     ↓
PostgreSQL
```

O `JwtAuthFilter` identifica o usuário autenticado a partir do JWT. O `Service` manipula as entidades, aplica as regras de negócio e resolve o contexto da empresa autenticada antes de acessar recursos protegidos por tenant nos repositories.

Na resposta:

```text
PostgreSQL
     ↓
Repository
     ↓
Entity
     ↓
Service
     ↓
Response DTO
     ↓
Controller
     ↓
JSON
```

### Responsabilidade das camadas

#### Controller

Responsável por receber requisições HTTP e devolver respostas da API.

#### DTO

Define quais dados podem entrar e sair da aplicação.

#### Service

Concentra as regras de negócio e coordena as operações da aplicação.

#### Repository

Responsável pelo acesso e persistência dos dados utilizando Spring Data JPA.

#### Entity

Representa as entidades persistidas no banco de dados.

#### Exception

Representa erros e violações de regras da aplicação.

---

# Estrutura de packages

```text
com.climaservice.api
│
├── config
├── controller
│
├── dto
│
├── entity
│
├── exception
│
├── repository
│
└── service
```

---

# Modelo de domínio atual

As principais entidades são:

```text
Empresa
│
├── Usuario
│    └── RoleUsuario
│
├── Cliente
│    └── Equipamento
│         └── OrdemServico
│              ├── OrdemServicoHistorico
│              ├── OrdemServicoDiagnosticoHistorico
│              ├── Agendamento
│              │    └── AgendamentoHistorico
│              └── Orcamento
│                   ├── OrcamentoHistorico
│                   ├── OrcamentoItem
│                   │    ├── Servico
│                   │    └── Produto
│                   └── Pagamento
│                        └── PagamentoHistorico
│
├── Servico
└── Produto
```

Principais relacionamentos:

```text
Cliente
  1
  │
  N
Equipamento
```

```text
Cliente + Equipamento
        ↓
OrdemServico
```

```text
OrdemServico
     1
     │
     N
Orcamento
```

```text
Orcamento
   1
   │
   N
OrcamentoItem
```

```text
Orcamento
   1
   │
   N
Pagamento
```

---

# Banco de dados

O projeto utiliza **PostgreSQL**.

As configurações sensíveis são fornecidas através de variáveis de ambiente:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000
```

Exemplo de configuração local:

```text
DB_URL=jdbc:postgresql://localhost:5432/climaservice
DB_USERNAME=climaservice_user
DB_PASSWORD=sua_senha
JWT_SECRET=sua_chave_base64
```

> Não armazene credenciais reais diretamente no repositório.

O schema do banco é versionado com **Flyway**. As alterações estruturais são aplicadas através de migrations SQL localizadas em:

```text
src/main/resources/db/migration
```

O Hibernate é utilizado com validação do schema, evitando alterações automáticas da estrutura em runtime:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

As migrations já cobrem a criação inicial do schema e a evolução do modelo multi-tenant, incluindo os vínculos de `Empresa` com usuários, clientes, equipamentos, ordens de serviço, serviços, produtos e orçamentos.

---

# Executando o projeto

## Pré-requisitos

Tenha instalado:

- JDK 21
- PostgreSQL
- Git

O projeto utiliza o **Gradle Wrapper**, portanto não é necessário instalar o Gradle separadamente.

---

## Clone o projeto

```bash
git clone URL_DO_REPOSITORIO
```

Entre no diretório:

```bash
cd climaservice-api
```

Configure as variáveis de ambiente:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
```

No Windows:

```bash
gradlew.bat bootRun
```

No Linux/macOS:

```bash
./gradlew bootRun
```

Por padrão, a API estará disponível em:

```text
http://localhost:8080
```

---

# Práticas aplicadas no projeto

Até o momento, o projeto utiliza conceitos como:

- API REST
- Arquitetura em camadas
- DTOs de Request e Response
- Spring Data JPA
- Hibernate
- Relacionamentos entre entidades
- Jakarta Bean Validation
- Tratamento global de exceções
- Regras de negócio no Service
- Transações com `@Transactional`
- Enums para representação de estados
- Workflows controlados de status
- Histórico de alterações
- Cálculos monetários utilizando `BigDecimal`
- Snapshot de valores financeiros
- Ativação e inativação de registros
- Spring Security
- Hash de senhas com BCrypt
- Autenticação stateless com JWT
- Filtro JWT por requisição
- Controle de acesso baseado em roles
- Autorização por método com `@PreAuthorize`
- Multi-tenancy com shared database / shared schema
- Isolamento de dados por empresa
- Tenant derivado do usuário autenticado
- Flyway para versionamento do schema
- JUnit 5 para testes automatizados
- Mockito para testes unitários
- Testcontainers com PostgreSQL real em testes de integração
- Auditoria de alterações associada ao usuário autenticado
- Git com desenvolvimento por feature branches
- Pull Requests para integração com a branch principal

---

# Roadmap

## Implementado

- [x] Cadastro de clientes
- [x] Cadastro de equipamentos
- [x] Associação entre cliente e equipamento
- [x] Ativação e inativação de equipamentos
- [x] Ordens de serviço
- [x] Diagnóstico técnico
- [x] Workflow de status da OS
- [x] Histórico de status da OS
- [x] Catálogo de serviços
- [x] Ativação e inativação de serviços
- [x] Orçamentos
- [x] Múltiplos orçamentos por OS
- [x] Itens de orçamento
- [x] Preço padrão e preço negociado
- [x] Cálculo automático de subtotal
- [x] Cálculo automático do total
- [x] Workflow de orçamento
- [x] Aprovação e rejeição de orçamento
- [x] Bloqueio de alterações após envio
- [x] Catálogo de produtos e peças
- [x] Ativação e inativação de produtos
- [x] Inclusão de peças em orçamentos
- [x] Pagamentos
- [x] Pagamentos parciais
- [x] Controle de saldo e valores pendentes
- [x] Usuários e perfis
- [x] Autenticação por e-mail e senha
- [x] Hash de senhas com BCrypt
- [x] Spring Security
- [x] JWT
- [x] Autenticação stateless
- [x] Controle de usuários ativos/inativos
- [x] Controle de permissões por role
- [x] Autorização com `@PreAuthorize`
- [x] Auditoria de alterações por usuário nos históricos
- [x] Flyway
- [x] Testes unitários com JUnit e Mockito
- [x] Testes de integração com Testcontainers
- [x] Entidade Empresa e contexto de tenant
- [x] Multi-tenancy em clientes
- [x] Multi-tenancy em equipamentos
- [x] Multi-tenancy em ordens de serviço
- [x] Multi-tenancy em serviços
- [x] Multi-tenancy em produtos
- [x] Multi-tenancy em orçamentos
- [x] Isolamento de pagamentos através de Orçamento → Empresa
- [x] Onboarding público de empresas (`POST /auth/register-company`)
- [x] Consulta e atualização dos dados da própria empresa (`GET`/`PATCH /empresa/me`)
- [x] Agenda de atendimentos, com técnico, workflow de status e histórico
- [x] Bloqueio de sobreposição de horário por técnico

## Próximas etapas

- [ ] Revisão final do isolamento multi-tenant no gerenciamento de usuários
- [ ] Manutenção preventiva
- [ ] OpenAPI / Swagger
- [ ] Docker
- [ ] Docker Compose
- [ ] CI/CD com GitHub Actions
- [ ] Frontend com Angular

---

# Objetivo do projeto

Além de desenvolver uma aplicação funcional para gestão de serviços de climatização, o projeto tem como objetivo aplicar e aprofundar conhecimentos em:

- Desenvolvimento backend com Java
- APIs REST
- Spring Boot
- Orientação a Objetos
- Arquitetura em camadas
- Persistência de dados
- Spring Data JPA
- Modelagem de banco de dados
- Regras de negócio
- Integração entre entidades
- Validação de dados
- Tratamento de erros
- Segurança de APIs
- Testes automatizados
- Docker
- CI/CD
- Git e GitHub
- Desenvolvimento frontend com Angular

---

# Status

🚧 **Projeto em desenvolvimento**

Atualmente, o backend já cobre o fluxo principal de atendimento e financeiro:

```text
Cliente
   ↓
Equipamento
   ↓
Ordem de Serviço
   ↓
Diagnóstico
   ↓
Orçamento
   ├── Serviços
   └── Peças
   ↓
Aprovação / Rejeição
   ↓
Pagamento
```

A API também possui uma camada de segurança e isolamento de dados compatível com o estágio atual do projeto:

```text
Login
  ↓
BCrypt
  ↓
JWT
  ↓
JwtAuthFilter
  ↓
SecurityContext
  ↓
Roles / @PreAuthorize
  ↓
Empresa do usuário autenticado
  ↓
Isolamento multi-tenant
```

O backend também utiliza **Flyway** para versionamento do banco e uma suíte de testes com **JUnit 5, Mockito e Testcontainers**, executando cenários de integração contra PostgreSQL real em container.

A próxima etapa é concluir a revisão de isolamento do `UsuarioService`. Depois disso, o projeto poderá avançar para agenda de atendimentos, manutenção preventiva, OpenAPI/Swagger, Docker, CI/CD e frontend com Angular.
