# ClimaService API

API REST para gerenciamento de serviços de climatização e manutenção de ar-condicionado.

O **ClimaService** está sendo desenvolvido como um projeto SaaS voltado para empresas e profissionais que trabalham com instalação, manutenção preventiva e manutenção corretiva de equipamentos de climatização.

O objetivo é construir uma aplicação completa utilizando **Java, Spring Boot, Angular e PostgreSQL**, aplicando conceitos e práticas utilizadas no desenvolvimento de sistemas reais, como arquitetura em camadas, regras de negócio, validações, relacionamentos entre entidades, tratamento global de erros, segurança com JWT, autorização por perfis e workflows de status.

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

### Planejadas

- Flyway
- JUnit
- Mockito
- Testcontainers
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
- Catálogo de Serviços
- Orçamentos
- Itens de Orçamento
- Catálogo de Produtos e Peças
- Pagamentos
- Usuários
- Autenticação e Autorização

Além dos CRUDs, o sistema já possui regras de negócio relacionadas ao ciclo de vida das ordens de serviço, orçamentos e pagamentos, além de autenticação com JWT e controle de acesso baseado em perfis.

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

Esse histórico permitirá futuramente adicionar auditoria por usuário.

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
| POST | `/usuarios` | Cadastrar usuário |
| GET | `/usuarios` | Listar usuários |
| GET | `/usuarios/{id}` | Buscar usuário |
| PATCH | `/usuarios/{id}/ativar` | Ativar usuário |
| PATCH | `/usuarios/{id}/inativar` | Inativar usuário |

O gerenciamento de usuários é restrito ao perfil `ADMIN`.

### Matriz de permissões

| Operação | ADMIN | ATENDENTE | TECNICO |
|---|:---:|:---:|:---:|
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

As autorizações específicas são aplicadas com `@PreAuthorize`, enquanto a `SecurityConfig` define as regras globais de autenticação.

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

O `JwtAuthFilter` identifica o usuário autenticado a partir do JWT. O `Service` manipula as entidades e aplica as regras de negócio antes de utilizar os repositories.

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
Cliente
│
└── Equipamento
       │
       └── OrdemServico
              │
              ├── OrdemServicoHistorico
              │
              └── Orcamento
                     │
                     ├── OrcamentoItem
                     │     ├── Servico
                     │     └── Produto
                     │
                     └── Pagamento

Usuario
└── RoleUsuario
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

Durante o desenvolvimento inicial, o Hibernate realiza a atualização do schema.

A migração do banco para **Flyway** está planejada para uma etapa futura.

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

## Próximas etapas

- [ ] Auditoria por usuário
- [ ] Multi-tenancy
- [ ] Agenda de atendimentos
- [ ] Manutenção preventiva
- [ ] Flyway
- [ ] Testes unitários com JUnit e Mockito
- [ ] Testes de integração com Testcontainers
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

A API também possui uma camada de segurança completa para o estágio atual do projeto:

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
```

A próxima evolução planejada é adicionar **auditoria por usuário**, registrando quem executou alterações relevantes no sistema. Em seguida, o projeto poderá avançar para **multi-tenancy**, agenda de atendimentos, manutenção preventiva, testes automatizados e infraestrutura de entrega.
