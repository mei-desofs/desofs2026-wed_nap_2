# Como Correr o ArcadeHaven do Zero

Guia completo para arrancar o projeto localmente usando Docker.

---

## Pré-requisitos

| Ferramenta | Versão mínima | Verificar |
|---|---|---|
| Docker | 24+ | `docker --version` |
| Docker Compose | v2 (incluído no Docker Desktop) | `docker compose version` |
| Git | qualquer | `git --version` |

> **Windows:** Recomenda-se o [Docker Desktop](https://www.docker.com/products/docker-desktop/). Certifica-te de que o Docker está em modo **Linux containers**.

---

## Problema comum em Windows — line endings do `mvnw`

Se clonaste o repositório no Windows e o `mvnw` ficou com line endings CRLF, o build vai falhar com:

```
/bin/sh: ./mvnw: not found
exit code: 127
```

**Correção (uma vez por clone):**

```bash
# Linux / macOS / Git Bash
sed -i 's/\r//' Api/mvnw

# PowerShell
(Get-Content Api\mvnw -Raw) -replace "`r`n", "`n" | Set-Content Api\mvnw -NoNewline
```

> O ficheiro `.gitattributes` na raiz do repositório já configura `Api/mvnw text eol=lf` para evitar este problema em futuros clones.

---

## Clonar e arrancar

```bash
# 1. Clonar
git clone <url-do-repositorio>
cd desofs2026-wed_nap_2

# 2. (Apenas Windows, se necessário — ver secção acima)
sed -i 's/\r//' Api/mvnw

# 3. Arrancar todos os serviços
docker compose up --build
```

O primeiro arranque demora alguns minutos porque:
- O Maven descarrega as dependências do Spring Boot
- O Keycloak inicializa e importa o realm `arcadehaven`

---

## Serviços disponíveis

| Serviço | URL | Credenciais padrão |
|---|---|---|
| **API Spring Boot** | http://localhost:8080 | — |
| **Keycloak Admin Console** | http://localhost:8180 | `admin` / `admin` |

---

## Variáveis de ambiente (opcionais)

O `docker-compose.yml` já tem valores padrão para tudo. Se precisares de alterar, cria um ficheiro `.env` na raiz:

```env
# Base de dados externa (padrão: servidor ISEP)
SPRING_DATASOURCE_URL=jdbc:postgresql://vsgate-s1.dei.isep.ipp.pt:10345/
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=LQ4QGqhh8gtS

# Keycloak
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# Utilizador admin da aplicação
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@arcadehaven.com
```

---

## Comandos úteis

```bash
# Arrancar em background (sem logs)
docker compose up --build -d

# Ver logs de um serviço específico
docker compose logs -f api
docker compose logs -f keycloak

# Parar tudo
docker compose down

# Parar e apagar volumes (reset completo da BD Keycloak)
docker compose down -v

# Reconstruir apenas a API
docker compose build api
docker compose up -d api
```

---

## Estrutura dos serviços

```
docker-compose.yml
├── api          → Spring Boot (porta 8080)
│                  ↳ depende do keycloak
├── keycloak     → Keycloak 26.2 (porta 8180)
│                  ↳ importa realm de ./keycloak/realm-export.json
│                  ↳ depende do keycloak-db (healthcheck)
└── keycloak-db  → PostgreSQL 16 (interno, sem porta exposta)
```

> A base de dados da aplicação (PostgreSQL) é **externa** e está no servidor da ISEP — não corre em Docker.

---

## Autenticação — como obter um token

```bash
# Obter token como admin
curl -s -X POST http://localhost:8180/realms/arcadehaven/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=arcadehaven-public" \
  -d "username=admin" \
  -d "password=Admin123!" \
  | jq '.access_token'

# Obter token como buyer (utilizador de teste)
curl -s -X POST http://localhost:8180/realms/arcadehaven/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=arcadehaven-public" \
  -d "username=buyer1" \
  -d "password=Password123!" \
  | jq '.access_token'
```

Usa o token obtido no header `Authorization: Bearer <token>` nas chamadas à API.

---

## Importar a coleção Postman

Na raiz do repositório existe o ficheiro `ArcadeHaven.postman_collection.json`. Para importar:

1. Abre o Postman
2. **Import** → seleciona `ArcadeHaven.postman_collection.json`
3. As variáveis já estão configuradas: `baseUrl=http://localhost:8080` e `keycloakUrl=http://localhost:8180`

A coleção inclui pedidos de autenticação para os três roles (admin, buyer1, publisher1) com a password `Password123!` (admin usa `Admin123!`).

---

## Resolução de problemas

| Sintoma | Causa provável | Solução |
|---|---|---|
| `/bin/sh: ./mvnw: not found` | `mvnw` com CRLF | Ver secção "Problema comum em Windows" |
| `Connection refused` na API | Keycloak ainda a inicializar | Aguarda ~60s e tenta novamente |
| `Unauthorized 401` | Token expirado ou em falta | Obtém novo token via Keycloak |
| `keycloak-db` não passa healthcheck | Porta 5432 ocupada localmente | `docker compose down -v` e tenta novamente |
| Build muito lento | Primeiro download de dependências Maven | Normal — seguintes builds usam cache |
