# ClimaService API

API REST para gerenciamento de serviços de climatização e manutenção de ar-condicionado.

O **ClimaService** está sendo desenvolvido como um projeto SaaS voltado para empresas e profissionais que trabalham com instalação, manutenção preventiva e manutenção corretiva de equipamentos de climatização.

O objetivo do projeto é construir uma aplicação completa utilizando **Java, Spring Boot, Angular e PostgreSQL**, aplicando práticas utilizadas no desenvolvimento de sistemas reais.

## Tecnologias

### Backend

* Java 21
* Spring Boot
* Spring Web MVC
* Spring Data JPA
* Hibernate
* Bean Validation
* PostgreSQL
* Gradle

### Futuramente

* Spring Security
* JWT
* Flyway
* Docker
* Docker Compose
* Testcontainers
* JUnit
* Mockito
* OpenAPI / Swagger
* GitHub Actions
* Angular

##  Funcionalidades implementadas

Atualmente, a API possui gerenciamento de clientes com:

* Cadastro de clientes
* Listagem de clientes
* Consulta de cliente por ID
* Atualização de clientes
* Exclusão de clientes
* Validação dos dados de entrada
* DTOs para entrada e saída de dados
* Persistência utilizando PostgreSQL
* Cadastro e gerenciamento de equipamentos
* Associação de equipamentos aos clientes
* Consulta de equipamentos por cliente
* Ativação e inativação de equipamentos
* Validação dos dados de equipamentos

##  Cliente

Atualmente, um cliente possui os seguintes dados:

* ID
* Nome
* CPF/CNPJ
* Telefone
* E-mail

##  Endpoints

### Cadastrar cliente

```http
POST /clientes
```

Exemplo de requisição:

```json
{
  "nome": "João da Silva",
  "cpfCnpj": "12345678901",
  "telefone": "47999999999",
  "email": "joao@email.com"
}
```

Resposta esperada:

```http
201 Created
```

---

### Listar clientes

```http
GET /clientes
```

---

### Buscar cliente por ID

```http
GET /clientes/{id}
```

Exemplo:

```http
GET /clientes/1
```

Possíveis respostas:

```text
200 OK
404 Not Found
```

---

### Atualizar cliente

```http
PUT /clientes/{id}
```

Exemplo:

```http
PUT /clientes/1
```

Body:

```json
{
  "nome": "João da Silva",
  "cpfCnpj": "12345678901",
  "telefone": "47988887777",
  "email": "joao.novo@email.com"
}
```

---

### Excluir cliente

```http
DELETE /clientes/{id}
```

Exemplo:

```http
DELETE /clientes/1
```

Resposta esperada:

```http
204 No Content
```

## Validações

Os dados recebidos pela API são validados utilizando **Jakarta Bean Validation**.

Exemplos:

```java
@NotBlank
@Size
@Email
@Pattern
```

Entre as validações atuais estão:

* Nome obrigatório
* Limite de caracteres para o nome
* CPF/CNPJ com 11 ou 14 dígitos
* Validação do formato de e-mail
* Limites de tamanho dos campos

## Arquitetura atual

O backend está organizado em camadas:

```text
HTTP Request
     ↓
Controller
     ↓
Request DTO
     ↓
Service
     ↓
Entity
     ↓
Repository
     ↓
JPA / Hibernate
     ↓
PostgreSQL
```

Na resposta:

```text
PostgreSQL
     ↓
Entity
     ↓
Response DTO
     ↓
Controller
     ↓
JSON
```

Estrutura de packages:

```text
com.climaservice.api
│
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

##  Banco de dados

O projeto utiliza **PostgreSQL**.

As configurações sensíveis são fornecidas através de variáveis de ambiente:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Exemplo para ambiente local:

```text
DB_URL=jdbc:postgresql://localhost:5432/climaservice
DB_USERNAME=climaservice_user
DB_PASSWORD=sua_senha
```

> Não armazene credenciais reais diretamente no repositório.

## Executando o projeto

### Pré-requisitos

Tenha instalado:

* JDK 21
* PostgreSQL
* Git

O projeto utiliza o **Gradle Wrapper**, portanto não é necessário instalar o Gradle separadamente.

### Clone o projeto

```bash
git clone URL_DO_REPOSITORIO
```

Entre no diretório:

```bash
cd climaservice-api
```

Configure as variáveis de ambiente do banco de dados.

Depois, no Windows:

```bash
gradlew.bat bootRun
```

Ou em Linux/macOS:

```bash
./gradlew bootRun
```

Por padrão, a API ficará disponível em:

```text
http://localhost:8080
```

## Roadmap

Próximas etapas planejadas:

* [ ] Ordens de serviço
* [ ] Histórico de ordens de serviço
* [ ] Técnicos e usuários
* [ ] Orçamentos
* [ ] Produtos e peças
* [ ] Pagamentos
* [ ] Agenda de atendimentos
* [ ] Manutenção preventiva
* [ ] Autenticação e autorização
* [ ] Controle de permissões
* [ ] Multi-tenancy
* [ ] Auditoria
* [ ] Testes unitários
* [ ] Testes de integração com Testcontainers
* [ ] Docker
* [ ] Documentação OpenAPI / Swagger
* [ ] CI/CD com GitHub Actions
* [ ] Frontend com Angular

## Objetivo do projeto

Além de criar uma aplicação funcional para gestão de serviços de climatização, o projeto tem como objetivo aplicar e aprofundar conhecimentos em:

* Desenvolvimento backend com Java
* APIs REST
* Spring Boot
* Arquitetura em camadas
* Persistência de dados
* Modelagem de banco de dados
* Regras de negócio
* Segurança de APIs
* Testes automatizados
* Docker
* CI/CD
* Git e GitHub

## Status

🚧 **Projeto em desenvolvimento**

Primeiro módulo implementado: **Gerenciamento de Clientes**.
