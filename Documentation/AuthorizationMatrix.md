# ArcadeHaven — Authorization Matrix

**ASVS 5.0 | V8.1.1 (Function-Level) · V8.1.2 (Field-Level)**
Generated: 2026-06-16

This document defines the authorization rules for all ArcadeHaven API endpoints and response fields. It is the normative reference for V8.1.1 (function-level and data-specific access) and V8.1.2 (field-level read/write restrictions).

**Implementation references:**
- Endpoint rules: [`Security/SecurityConfig.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Security/SecurityConfig.java) (path-level) + `@PreAuthorize("hasRole(...)")` in each controller
- Ownership checks: [`Service/GameService.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Service/GameService.java), [`Service/OrderService.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Service/OrderService.java), [`Service/LibraryService.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Service/LibraryService.java)
- Field-level filtering: [`Dto/Response/`](../Api/src/main/java/isep/desosfs/arcadehaven/Dto/Response/) (dedicated response DTOs per entity; JPA entities never serialized directly)
- Role extraction: [`Security/KeycloakRoleConverter.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Security/KeycloakRoleConverter.java)

---

## Roles

| Role | Description | Assignment |
|------|-------------|------------|
| **ANON** | Unauthenticated request (no JWT) | — |
| **BUYER** | Authenticated user who can browse, purchase, and manage their library | Keycloak realm role `BUYER` |
| **PUBLISHER** | Authenticated user who can submit and manage game listings | Keycloak realm role `PUBLISHER` |
| **ADMIN** | Platform administrator | Keycloak realm role `ADMIN` — protected by TOTP (V6.3.3) |

All roles are extracted from the JWT `realm_access.roles` claim by `KeycloakRoleConverter` and mapped to Spring `GrantedAuthority` objects (prefix `ROLE_`). The deny-all default in `SecurityConfig` ensures any endpoint not explicitly listed below requires authentication.

---

## V8.1.1 — Endpoint-Level Authorization Matrix

Legend: ✅ Allowed · ❌ Denied · — Not applicable

| Endpoint | Method | ANON | BUYER | PUBLISHER | ADMIN | Data Ownership |
|----------|--------|:----:|:-----:|:---------:|:-----:|----------------|
| `/api/auth/login` | POST | ✅ | ✅ | ✅ | ✅ | — |
| `/api/auth/register` | POST | ✅ | ✅ | ✅ | ✅ | — |
| `/api/auth/logout` | POST | ❌ | ✅ | ✅ | ✅ | JWT principal → revokes own sessions |
| `/api/games` | GET | ✅ | ✅ | ✅ | ✅ | — (public listing, ACTIVE games only) |
| `/api/games/{id}` | GET | ✅ | ✅ | ✅ | ✅ | — (public) |
| `/api/orders` | GET | ❌ | ✅ | ❌ | ❌ | Scoped to JWT `sub` — only own orders |
| `/api/orders/{id}` | GET | ❌ | ✅ | ❌ | ❌ | `OrderService` verifies `order.buyerId == JWT sub` |
| `/api/orders` | POST | ❌ | ✅ | ❌ | ❌ | Order created under JWT `sub` |
| `/api/orders/{id}/complete` | POST | ❌ | ✅ | ❌ | ❌ | `OrderService` verifies ownership |
| `/api/orders/{id}/cancel` | POST | ❌ | ✅ | ❌ | ❌ | `OrderService` verifies ownership |
| `/api/orders/{id}/items` | POST | ❌ | ✅ | ❌ | ❌ | `OrderService` verifies ownership |
| `/api/orders/{id}/items/{gameId}` | DELETE | ❌ | ✅ | ❌ | ❌ | `OrderService` verifies ownership |
| `/api/orders/{id}/invoice` | GET | ❌ | ✅ | ❌ | ❌ | `OrderService` verifies ownership |
| `/api/orders/{id}/keycard` | GET | ❌ | ✅ | ❌ | ❌ | `OrderService` verifies ownership |
| `/api/library` | GET | ❌ | ✅ | ❌ | ❌ | Scoped to JWT `sub` |
| `/api/library/import-key` | POST | ❌ | ✅ | ❌ | ❌ | Scoped to JWT `sub` |
| `/api/publisher/games` | GET | ❌ | ❌ | ✅ | ❌ | `GameService.findByIdAndPublisher()` — own games only |
| `/api/publisher/games` | POST | ❌ | ❌ | ✅ | ❌ | Game assigned to JWT `sub` publisher |
| `/api/publisher/games/{id}` | PUT | ❌ | ❌ | ✅ | ❌ | `GameService.findByIdAndPublisher()` — own game only |
| `/api/publisher/games/{gameId}/{fileId}` | GET | ❌ | ❌ | ✅ | ❌ | `GameService.findByIdAndPublisher()` — own game only |
| `/api/publisher/games/{id}/metrics` | GET | ❌ | ❌ | ✅ | ❌ | `GameService.findByIdAndPublisher()` — own game only |
| `/api/publisher/games/{id}/files` | POST | ❌ | ❌ | ✅ | ❌ | `GameService.findByIdAndPublisher()` — own game only |
| `/api/profile` | GET | ❌ | ✅ | ✅ | ✅ | Scoped to JWT `sub` |
| `/api/profile` | PATCH | ❌ | ✅ | ✅ | ✅ | Scoped to JWT `sub` |
| `/api/profile/password` | PATCH | ❌ | ✅ | ✅ | ✅ | Scoped to JWT `sub`; requires current password |
| `/api/admin/users` | GET | ❌ | ❌ | ❌ | ✅ | All users (admin only) |
| `/api/admin/users/{id}` | GET | ❌ | ❌ | ❌ | ✅ | All users (admin only) |
| `/api/admin/users/{id}/deactivate` | PATCH | ❌ | ❌ | ❌ | ✅ | Admin action |
| `/api/admin/users/{id}/activate` | PATCH | ❌ | ❌ | ❌ | ✅ | Admin action |
| `/api/admin/users/{id}/role` | PATCH | ❌ | ❌ | ❌ | ✅ | Admin action |
| `/api/admin/games` | GET | ❌ | ❌ | ❌ | ✅ | All games (admin only) |
| `/api/admin/games/{id}/approve` | PATCH | ❌ | ❌ | ❌ | ✅ | Admin action |
| `/api/admin/games/{id}/reject` | PATCH | ❌ | ❌ | ❌ | ✅ | Admin action |
| `/api/admin/games/{id}/remove` | PATCH | ❌ | ❌ | ❌ | ✅ | Admin action |
| `/api/admin/users/{userId}/library/{entryId}/suspend` | PATCH | ❌ | ❌ | ❌ | ✅ | Admin action |
| `/api/admin/users/{userId}/library/{entryId}/revoke` | PATCH | ❌ | ❌ | ❌ | ✅ | Admin action |
| `/actuator/health` | GET | ✅ | ✅ | ✅ | ✅ | — (no details exposed; `show-details=never`) |
| `/v3/api-docs/**` | GET | ❌ | ✅ | ✅ | ✅ | JWT required |
| `/swagger-ui/**` | GET | ❌ | ✅ | ✅ | ✅ | JWT required |

### BOLA / IDOR Prevention

Data-level ownership is enforced at the **service layer**, not just by path-level role checks:

| Service | Check | Implementation |
|---------|-------|----------------|
| `GameService` | Publisher can only read/write own games | `findByIdAndPublisher(id, publisherUsername)` throws `ResourceNotFoundException` if the game belongs to another publisher |
| `OrderService` | Buyer can only access own orders | `findByIdAndBuyer(id, buyerUsername)` throws if order belongs to another user |
| `LibraryService` | Buyer can only access own library | All queries scoped to authenticated username |
| `ProfileService` | User can only modify own profile | Username derived from JWT `sub`; never from request body |

---

## V8.1.2 — Field-Level Authorization Matrix

JPA domain entities are **never serialized directly**. Every endpoint uses a dedicated response DTO that explicitly selects the fields to expose. This prevents accidental mass-data exposure (BOPLA) and ensures sensitive fields are excluded by default.

### User / Profile

| Field | Source | BUYER (own) | PUBLISHER (own) | ADMIN (all) | Writable by |
|-------|--------|:-----------:|:---------------:|:-----------:|-------------|
| `id` | `UserResponse` | ✅ | ✅ | ✅ | — (system-generated) |
| `username` | `UserResponse` | ✅ | ✅ | ✅ | Own user via `PATCH /api/profile` |
| `email` | `UserResponse` | ✅ | ✅ | ✅ | Own user via `PATCH /api/profile` |
| `role` | `UserResponse` | ✅ | ✅ | ✅ | ADMIN only via `PATCH /api/admin/users/{id}/role` |
| `active` | `UserResponse` | ✅ | ✅ | ✅ | ADMIN only via activate/deactivate endpoints |
| `createdAt` | `UserResponse` | ✅ | ✅ | ✅ | — (system-generated) |
| `passwordHash` | — | ❌ | ❌ | ❌ | Managed by Keycloak — never exposed |
| `keycloakId` | — | ❌ | ❌ | ❌ | Internal ID used for Keycloak API calls — never in any response |

### Game

| Field | Source | ANON | BUYER | PUBLISHER (own) | ADMIN | Writable by |
|-------|--------|:----:|:-----:|:---------------:|:-----:|-------------|
| `id` | `GameResponse` | ✅ | ✅ | ✅ | ✅ | — |
| `title` | `GameResponse` | ✅ | ✅ | ✅ | ✅ | PUBLISHER (own game) |
| `description` | `GameResponse` | ✅ | ✅ | ✅ | ✅ | PUBLISHER (own game) |
| `price` | `GameResponse` | ✅ | ✅ | ✅ | ✅ | PUBLISHER (own game) |
| `status` | `GameResponse` | ✅¹ | ✅¹ | ✅ | ✅ | ADMIN (approve/reject/remove) |
| `rawgApiId` | `GameResponse` | ✅ | ✅ | ✅ | ✅ | PUBLISHER (own game) |
| `category` | `GameResponse` | ✅ | ✅ | ✅ | ✅ | PUBLISHER (own game) |
| `publisherUsername` | `GameResponse` | ✅ | ✅ | ✅ | ✅ | — |
| `createdAt` | `GameResponse` | ✅ | ✅ | ✅ | ✅ | — |
| `publisher.email` | — | ❌ | ❌ | ❌ | ❌ | Not in any game response |
| `publisher.passwordHash` | — | ❌ | ❌ | ❌ | ❌ | Never exposed |

¹ Public endpoints (`GET /api/games`) only return games with `status = ACTIVE`. PENDING and REJECTED games are not visible to ANON or BUYER.

### Game Metrics

| Field | Source | BUYER | PUBLISHER (own) | ADMIN |
|-------|--------|:-----:|:---------------:|:-----:|
| `gameId` | `GameMetricsResponse` | ❌ | ✅ | ❌² |
| `gameTitle` | `GameMetricsResponse` | ❌ | ✅ | ❌² |
| `unitsSold` | `GameMetricsResponse` | ❌ | ✅ | ❌² |
| `totalRevenue` | `GameMetricsResponse` | ❌ | ✅ | ❌² |

² Metrics endpoint is under `/api/publisher/**` — scoped to PUBLISHER role only. ADMIN does not use this endpoint (admin has full game management via `/api/admin/games`).

### Order

| Field | Source | BUYER (own) | PUBLISHER | ADMIN |
|-------|--------|:-----------:|:---------:|:-----:|
| `id` | `OrderResponse` | ✅ | ❌ | ❌ |
| `status` | `OrderResponse` | ✅ | ❌ | ❌ |
| `totalPrice` | `OrderResponse` | ✅ | ❌ | ❌ |
| `items` | `OrderResponse` | ✅ | ❌ | ❌ |
| `createdAt` | `OrderResponse` | ✅ | ❌ | ❌ |
| `buyerId` / `buyerUsername` | — | ❌ | ❌ | ❌ | Implicit in JWT scope — not in response |

### Order Item

| Field | Source | BUYER (own order) | PUBLISHER | ADMIN |
|-------|--------|:-----------------:|:---------:|:-----:|
| `id` | `OrderItemResponse` | ✅ | ❌ | ❌ |
| `gameId` | `OrderItemResponse` | ✅ | ❌ | ❌ |
| `gameTitle` | `OrderItemResponse` | ✅ | ❌ | ❌ |
| `price` | `OrderItemResponse` | ✅ | ❌ | ❌ |
| `activationKey` | `OrderItemResponse` | ✅ | ❌ | ❌ |

### Library Entry

| Field | Source | BUYER (own) | PUBLISHER | ADMIN |
|-------|--------|:-----------:|:---------:|:-----:|
| `id` | `LibraryEntryResponse` | ✅ | ❌ | ❌³ |
| `gameId` | `LibraryEntryResponse` | ✅ | ❌ | ❌³ |
| `gameTitle` | `LibraryEntryResponse` | ✅ | ❌ | ❌³ |
| `activationKey` | `LibraryEntryResponse` | ✅ | ❌ | ❌³ |
| `status` | `LibraryEntryResponse` | ✅ | ❌ | ❌³ |
| `acquiredAt` | `LibraryEntryResponse` | ✅ | ❌ | ❌³ |
| `userId` | — | ❌ | ❌ | ❌ | Implicit in JWT scope — never in response |

³ ADMIN manages library entries via `/api/admin/users/{userId}/library/{entryId}/suspend|revoke` (status change only) — does not read the full entry response.

---

## Summary

| ASVS Control | Mechanism | Reference |
|-------------|-----------|-----------|
| **V8.1.1** — Function-level access | Path rules in `SecurityConfig` + `@PreAuthorize` on controllers; deny-all default | [`SecurityConfig.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Security/SecurityConfig.java) |
| **V8.1.1** — Data-level access (BOLA/IDOR) | Ownership checks at service layer — username from JWT, never from request body | `GameService`, `OrderService`, `LibraryService` |
| **V8.1.2** — Field-level filtering | Dedicated response DTOs per entity; no JPA entity serialization | [`Dto/Response/`](../Api/src/main/java/isep/desosfs/arcadehaven/Dto/Response/) |
| **V8.1.2** — Sensitive field exclusion | `passwordHash`, `keycloakId`, `buyerId` absent from all response DTOs | `UserResponse`, `OrderResponse`, `LibraryEntryResponse` |
