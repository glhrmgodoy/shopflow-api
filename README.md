# 🛒 ShopFlow API

API REST para gerenciamento de pedidos de e-commerce, desenvolvida com Java 21 e Spring Boot.

## 📋 Sobre o Projeto

Sistema de pedidos que permite o cadastro de clientes, categorias e produtos, além do gerenciamento completo do ciclo de vida de um pedido — desde a criação até a entrega ou cancelamento. O estoque é controlado automaticamente a cada pedido criado ou cancelado, e o preço dos itens é preservado no momento da compra.

## 🚀 Tecnologias

- **Java 21**
- **Spring Boot 3.3**
- **Spring Data JPA**
- **PostgreSQL**
- **MapStruct**
- **Lombok**
- **Bean Validation**
- **SpringDoc OpenAPI (Swagger)**
- **JUnit 5 + Mockito**
- **Docker + Docker Compose**

## 📐 Arquitetura

```
src/main/java/com/empresa/order/
├── config/
├── controller/
├── service/
├── repository/
├── domain/
│   ├── entity/
│   └── enums/
├── dto/
│   ├── request/
│   └── response/
├── mapper/
└── exception/
```

## 🗄️ Entidades

| Entidade | Descrição |
|----------|-----------|
| `Customer` | Clientes que realizam pedidos |
| `Category` | Categorias dos produtos |
| `Product` | Produtos com preço e estoque |
| `Order` | Pedido com status, forma de pagamento e valor total |
| `OrderItem` | Itens do pedido com snapshot de preço no momento da compra |

## 📌 Endpoints

### Customers
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/v1/customers` | Cadastrar cliente |
| GET | `/api/v1/customers` | Listar clientes |
| GET | `/api/v1/customers/{id}` | Buscar por ID |
| PUT | `/api/v1/customers/{id}` | Atualizar cliente |
| DELETE | `/api/v1/customers/{id}` | Inativar cliente |

### Categories
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/v1/categories` | Cadastrar categoria |
| GET | `/api/v1/categories` | Listar categorias |
| GET | `/api/v1/categories/{id}` | Buscar por ID |
| PUT | `/api/v1/categories/{id}` | Atualizar categoria |
| DELETE | `/api/v1/categories/{id}` | Inativar categoria |

### Products
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/v1/products` | Cadastrar produto |
| GET | `/api/v1/products` | Listar produtos |
| GET | `/api/v1/products/{id}` | Buscar por ID |
| GET | `/api/v1/products/category/{categoryId}` | Produtos por categoria |
| PUT | `/api/v1/products/{id}` | Atualizar produto |
| PATCH | `/api/v1/products/{id}/stock` | Atualizar estoque |
| DELETE | `/api/v1/products/{id}` | Inativar produto |

### Orders
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/v1/orders` | Criar pedido |
| GET | `/api/v1/orders` | Listar pedidos |
| GET | `/api/v1/orders/{id}` | Buscar por ID |
| GET | `/api/v1/orders/customer/{customerId}` | Pedidos do cliente |
| PATCH | `/api/v1/orders/{id}/confirm` | Confirmar pedido |
| PATCH | `/api/v1/orders/{id}/ship` | Enviar pedido |
| PATCH | `/api/v1/orders/{id}/deliver` | Entregar pedido |
| DELETE | `/api/v1/orders/{id}` | Cancelar pedido |

## ⚙️ Regras de Negócio

- Cliente deve estar ativo para realizar pedidos
- Pedido deve ter pelo menos um item
- Produto deve estar ativo e com estoque suficiente
- Preço do item salvo no momento da compra — não muda se o produto mudar de preço depois
- Estoque decrementado automaticamente ao criar o pedido
- Estoque devolvido automaticamente ao cancelar o pedido
- Fluxo de status: `PENDING → CONFIRMED → SHIPPED → DELIVERED`
- Cancelamento permitido apenas para pedidos `PENDING` ou `CONFIRMED`
- Categoria com produtos ativos não pode ser inativada
- Soft delete em todas as entidades — registros nunca são deletados do banco

## 🧪 Testes

O projeto possui **67 testes unitários** cobrindo todos os services com JUnit 5 e Mockito:

| Service | Testes |
|---------|--------|
| `OrderServiceTest` | 25 |
| `ProductServiceTest` | 17 |
| `CustomerServiceTest` | 13 |
| `CategoryServiceTest` | 12 |
| **Total** | **67** |

Para rodar os testes:
```bash
mvn test
```

## 🐳 Como Rodar

### Pré-requisitos
- Docker e Docker Compose instalados
- Java 21
- Maven

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/shopflow-api.git
cd shopflow-api
```

### 2. Configure o `.env`
```env
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin
POSTGRES_DB=shopflow_db
```

### 3. Suba o banco com Docker
```bash
docker-compose up -d
```

### 4. Rode a aplicação
```bash
mvn spring-boot:run
```

### 5. Acesse o Swagger
```
http://localhost:8080/swagger-ui/index.html
```
