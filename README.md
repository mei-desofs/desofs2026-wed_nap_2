# 🎮 ArcadeHaven - DESOFS Project 2026

> Plataforma de venda e distribuição digital de videojogos

---

## Agregados DDD

| Agregado | Responsabilidades |
|---|---|
| **Utilizador** | Registo, autenticação, perfil, roles |
| **Jogo** | Catálogo, categorias, preço, ficheiros e imagens |
| **Encomenda** | Compra, histórico, chaves de ativação, fatura |
| **Library** | Coleção pessoal de jogos adquiridos, chaves de ativação, estado de cada entrada |

---

## Roles

### 🔧 Admin
Gestão total do sistema, utilizadores e relatórios.

### 🏢 Publisher
Adiciona e gere os seus jogos; consulta métricas de vendas.

### 🛒 Comprador
Navega no catálogo, compra jogos, acede à sua Library pessoal, consulta histórico e faz download de faturas.

---

## Fluxo Principal

```
PUBLISHER cria Jogo → PENDING
       ↓
ADMIN aprova Jogo → ACTIVE
       ↓
BUYER cria Encomenda com o Jogo
       ↓
Encomenda completada → gera fatura + chave de ativação
       ↓
Jogo adicionado à Library do BUYER
```

---

## Operações com Ficheiros

- 📄 Geração de faturas em **PDF**
- 🖼️ Upload de imagens e capturas de ecrã dos jogos
- 🔑 Geração e armazenamento de **chaves de ativação** em ficheiro
- 📁 Criação automática de estrutura de diretórios no servidor

---

## Base de Dados Relacional

```
users
games
orders
order_items
libraries
library_entries
```

---

## Stack Tecnológica

| Componente | Tecnologia |
|---|---|
| Back-end | Java + Spring Boot |
| Segurança | Spring Security + JWT |
| Base de Dados | PostgreSQL |
| ORM | JPA / Hibernate |
| Migrações | Flyway |
| Testes | JUnit + Mockito |
| Pipeline | GitHub Actions |
| SAST | SonarQube |
| Contentorização | Docker + Docker Compose |

---

## API Externa

| Serviço | Descrição |
|---|---|
| [**RAWG.io**](https://rawg.io/) | Base de dados de jogos — gratuita, REST API |

---

## Estrutura do Repositório

```
desofs2026-wed_nap_2/
├── src/
│   └── main/java/com/arcadehaven/
│       ├── domain/          # Agregados DDD
│       │   ├── user/
│       │   ├── game/
│       │   ├── order/
│       │   └── library/
│       ├── application/     # Serviços / Casos de uso
│       ├── infrastructure/  # Persistência, Segurança, Ficheiros
│       └── api/             # Controllers REST
├── Deliverables/            # Entregas por fase/sprint
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

