# Como Correr o ArcadeHaven do Zero

Guia completo para arrancar o projeto localmente usando Docker, incluindo Keycloak, base de dados e gestão de volumes.

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

> O ficheiro `.gitattributes` na raiz configura `Api/mvnw text eol=lf` para evitar este problema em futuros clones.

---

## Passo 1 — Clonar o repositório

```bash
git clone <url-do-repositorio>
cd desofs2026-wed_nap_2

# Apenas Windows, se necessário — ver secção acima
(Get-Content Api\mvnw -Raw) -replace "`r`n", "`n" | Set-Content Api\mvnw -NoNewline
```

---

## Passo 2 — Criar o ficheiro `.env`

O ficheiro `Api/.env` **não está no repositório** (está no `.gitignore`). Tens de o criar manualmente.

Cria o ficheiro `Api/.env` com o seguinte conteúdo:

```env
# ─── Base de dados da aplicação (PostgreSQL externo — servidor ISEP) ───────────
SPRING_DATASOURCE_URL=jdbc:postgresql://vsgate-s1.dei.isep.ipp.pt:10345/
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=LQ4QGqhh8gtS

# ─── Keycloak — ligação do Spring Boot ao Keycloak ────────────────────────────
KEYCLOAK_JWK_SET_URI=http://keycloak:8080/realms/arcadehaven/protocol/openid-connect/certs
KEYCLOAK_SERVER_URL=http://keycloak:8080
KEYCLOAK_REALM=arcadehaven
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# ─── Backend service-account client secret (deve coincidir com realm-export.json em dev) ─
# PRODUÇÃO: rodar via Keycloak Admin Console e actualizar este valor
KEYCLOAK_BACKEND_CLIENT_SECRET=backend-secret-dev-CHANGE-IN-PRODUCTION

# ─── Utilizador admin da aplicação (criado no primeiro arranque) ──────────────
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@arcadehaven.com

# ─── SFTP (armazenamento de ficheiros externo) ────────────────────────────────
SFTP_HOST=130.61.124.73
SFTP_PORT=22
SFTP_USERNAME=storageuser
SFTP_PASSWORD=GpevrNWixHvAja9*
SFTP_REMOTE_DIR=/srv/file-storage

# ─── Base de dados interna do Keycloak (PostgreSQL em Docker) ─────────────────
KC_DB=postgres
KC_DB_URL=jdbc:postgresql://keycloak-db:5432/keycloak
KC_DB_USERNAME=keycloak
KC_DB_PASSWORD=keycloak
KC_HOSTNAME_STRICT=false
KC_HTTP_ENABLED=true
KC_HEALTH_ENABLED=true

POSTGRES_DB=keycloak
POSTGRES_USER=keycloak
POSTGRES_PASSWORD=keycloak
```

> **Nota:** Se o servidor SFTP não estiver acessível, podes usar armazenamento local adicionando `STORAGE_BACKEND=local` ao `.env`. Nesse caso os ficheiros são guardados em `Api/storage/` dentro do container.

---

## Passo 3 — Arrancar todos os serviços

**Todos os comandos `docker compose` têm de ser corridos dentro da pasta `Api/`**, porque é aí que está o `docker-compose.yml`.

```bash
cd Api
docker compose up --build
```

O primeiro arranque demora alguns minutos porque:
- O Maven descarrega todas as dependências do Spring Boot
- O Keycloak inicializa, cria o esquema na base de dados e importa o realm `arcadehaven`
- A API aguarda o Keycloak estar saudável antes de arrancar

**Ordem de arranque garantida pelo Docker Compose:**
```
keycloak-db  →  keycloak (aguarda healthcheck)  →  api (aguarda healthcheck)
```

---

## Serviços disponíveis

| Serviço | URL | Credenciais |
|---|---|---|
| **API Spring Boot** | http://localhost:8080 | token JWT (ver secção Autenticação) |
| **Keycloak Admin Console** | http://localhost:8180 | `admin` / `admin` |
| **Actuator (healthcheck)** | http://localhost:8080/actuator/health | público |

---

## Utilizadores e credenciais

Estes utilizadores são importados automaticamente pelo Keycloak a partir de `keycloak/realm-export.json`.

| Username | Password | Role | Notas |
|---|---|---|---|
| `admin` | `Admin123!` | ADMIN | MFA (TOTP) obrigatório no browser flow |
| `publisher1` | `Xtr4Safe#2026XY` | PUBLISHER | — |
| `buyer1` | `Xtr4Safe#2026XY` | BUYER | — |

> **Atenção MFA — utilizador `admin`:**
> O flow de browser do Keycloak exige TOTP para utilizadores com o role `ADMIN` (ASVS V6.3.3).
> Isto aplica-se ao login no frontend/browser. Para chamadas à API (Direct Grant), **não é exigido TOTP**.
> O Keycloak Admin Console (`admin`/`admin`) é um utilizador diferente do realm `master` — sem TOTP.

---

## Keycloak — Volumes e importação do realm

### Como funciona a importação

O Keycloak arranca com a flag `--import-realm`. O ficheiro `keycloak/realm-export.json` (na raiz do repositório) é montado no container em `/opt/keycloak/data/import`.

**Comportamento:**
- **Primeiro arranque (sem volume):** O realm `arcadehaven` é criado com todos os utilizadores, roles, flows de autenticação e clientes.
- **Arranques subsequentes (volume existe):** A importação é **ignorada** — o Keycloak usa os dados já existentes no volume `keycloak_db_data`. Alterações ao `realm-export.json` **não são aplicadas** automaticamente.

### Quando apagar o volume do Keycloak

| Situação | Acção |
|---|---|
| Primeira instalação | Não é necessário — volume não existe |
| Quero repor o realm ao estado original | `docker compose down -v` depois `docker compose up --build` |
| Modifiquei o `realm-export.json` e quero reimportar | `docker compose down -v` depois `docker compose up --build` |
| `keycloak-db` não passa o healthcheck | `docker compose down -v` depois `docker compose up --build` |
| Quero parar sem perder dados | `docker compose down` (sem `-v`) |

> **Atenção:** `docker compose down -v` apaga o volume `keycloak_db_data`. Quaisquer utilizadores, configurações ou tokens criados manualmente no Keycloak serão perdidos. Os utilizadores definidos no `realm-export.json` são recriados no próximo arranque.

---

## Comandos úteis

```bash
# Entrar na pasta certa (obrigatório para todos os comandos docker compose)
cd Api

# Arrancar (com logs visíveis)
docker compose up --build

# Arrancar em background
docker compose up --build -d

# Ver logs de um serviço
docker compose logs -f api
docker compose logs -f keycloak
docker compose logs -f keycloak-db

# Parar tudo (preserva volumes e dados)
docker compose down

# Reset completo — apaga volumes do Keycloak
docker compose down -v

# Reconstruir só a API (sem tocar no Keycloak)
docker compose build api
docker compose up -d api

# Ver estado dos serviços
docker compose ps

# Ver utilização de recursos
docker stats
```

---

## Estrutura dos serviços

```
Api/docker-compose.yml
├── api          → Spring Boot (porta 8080)
│                  ↳ depende do keycloak (healthcheck antes de arrancar)
│                  ↳ volume: ./storage → /app/storage
├── keycloak     → Keycloak 26.2 (porta 8180 externa / 8080 interna)
│                  ↳ importa realm de ../keycloak/realm-export.json
│                  ↳ depende do keycloak-db (healthcheck)
└── keycloak-db  → PostgreSQL 16 (interno, sem porta exposta)
                   ↳ volume: keycloak_db_data (persistente)
```

**Dependências externas (não correm em Docker):**
- **Base de dados da aplicação:** PostgreSQL no servidor ISEP (`vsgate-s1.dei.isep.ipp.pt:10345`)
- **SFTP:** Servidor em `130.61.124.73` para armazenamento de ficheiros

---

## Autenticação — como obter um token JWT

Os tokens são obtidos diretamente do Keycloak via Direct Grant (sem TOTP).

```bash
# Token como admin (ADMIN role)
curl -s -X POST http://localhost:8180/realms/arcadehaven/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=arcadehaven-public" \
  -d "username=admin" \
  -d "password=Admin123!" \
  | jq '.access_token'

# Token como publisher1 (PUBLISHER role)
curl -s -X POST http://localhost:8180/realms/arcadehaven/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=arcadehaven-public" \
  -d "username=publisher1" \
  -d "password=Xtr4Safe#2026XY" \
  | jq '.access_token'

# Token como buyer1 (BUYER role)
curl -s -X POST http://localhost:8180/realms/arcadehaven/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=arcadehaven-public" \
  -d "username=buyer1" \
  -d "password=Xtr4Safe#2026XY" \
  | jq '.access_token'
```

Usa o token no header de todas as chamadas à API:

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/games
```

---

## Importar a coleção Postman

Na raiz do repositório existe o ficheiro `ArcadeHaven.postman_collection.json`. Para importar:

1. Abre o Postman
2. **Import** → seleciona `ArcadeHaven.postman_collection.json`
3. As variáveis já estão configuradas: `baseUrl=http://localhost:8080` e `keycloakUrl=http://localhost:8180`

Credenciais para os três roles:

| Utilizador | Password |
|---|---|
| `admin` | `Admin123!` |
| `publisher1` | `Xtr4Safe#2026XY` |
| `buyer1` | `Xtr4Safe#2026XY` |

---

## Armazenamento de ficheiros (SFTP vs local)

Por defeito, a API usa o servidor SFTP externo. Se não tiveres acesso ao SFTP, podes usar armazenamento local:

```env
# Adiciona ao Api/.env
STORAGE_BACKEND=local
```

Com `STORAGE_BACKEND=local`, os ficheiros são guardados em `Api/storage/` dentro do container (volume `./storage`). **Não usar em produção.**

---

## Cobertura de Testes — JaCoCo

O JaCoCo está configurado no `Api/pom.xml`. Corre os testes com Maven dentro da pasta `Api/`:

```bash
cd Api

# Corre todos os testes e gera relatório HTML
./mvnw verify        # Linux / macOS / Git Bash
.\mvnw.cmd verify    # Windows PowerShell
```

O relatório é gerado em:

```
Api/target/site/jacoco/index.html
```

### Cobertura actual (unit tests)

```
INSTRUCTION:  24%  (1197 / 4983)
BRANCH:       25%  (47 / 190)
LINE:         26%  (284 / 1091)
METHOD:       22%  (69 / 314)
CLASS:        36%  (23 / 64)
```

> Os testes actuais são *unit tests* sem Spring context. Controllers, repositórios JPA e serviços com dependências externas (Keycloak, SFTP) requerem testes de integração com `@SpringBootTest` e Testcontainers para aumentar a cobertura.

---

## Resolução de problemas

| Sintoma | Causa provável | Solução |
|---|---|---|
| `/bin/sh: ./mvnw: not found` | `mvnw` com CRLF | Ver secção "Problema comum em Windows" |
| `Api/.env: no such file` | Ficheiro `.env` não existe | Criar `Api/.env` conforme Passo 2 |
| `Connection refused` na API | Keycloak ainda a inicializar | Aguarda ~2-4 min; healthcheck garante ordem |
| `Unauthorized 401` | Token expirado ou em falta | Obtém novo token via Keycloak |
| `keycloak-db` não passa healthcheck | Volume corrompido ou porta ocupada | `docker compose down -v` e tenta novamente |
| Keycloak arranca mas realm não existe | Volume existe mas realm nunca foi importado | `docker compose down -v && docker compose up --build` |
| Alterei `realm-export.json` mas as mudanças não aparecem | Keycloak não reimporta realm existente | `docker compose down -v && docker compose up --build` |
| `SFTP connection refused` | Servidor SFTP inacessível | Adiciona `STORAGE_BACKEND=local` ao `.env` |
| Build muito lento | Primeiro download de dependências Maven | Normal — seguintes builds usam cache Docker |
| Admin não consegue fazer login no browser | TOTP não configurado | Configura um app TOTP (Google Authenticator) ou usa a API (Direct Grant não requer TOTP) |
| Porta 5432 já ocupada localmente | PostgreSQL local a correr | `keycloak-db` é interno, não expõe portas — verificar outro serviço |
