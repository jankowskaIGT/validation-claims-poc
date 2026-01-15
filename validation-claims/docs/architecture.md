
# Architecture & Design — Validation & Claims (Java PoC)

## 1. Purpose & Scope
This Proof of Concept demonstrates a minimal Spring Boot microservice for:
- **WIN** — validate if a ticket is a winner (read-only).
- **CLAIM** — update claim status and append an audit log entry with a chained signature.

**Non-goals:** performance tuning, DevOps/CI, advanced security, rich UI, pack activation checks.  
**Principle:** functionality over performance.

---

## 2. Layered Architecture
**Entry Point**
- `ValidationClaimsApplication` — boots Spring context and starts the embedded server.

**Controller Layer (`controller/`)**
- Handles HTTP requests (`/api/win/check`, `/api/claim`).
- Validates input, maps DTOs, returns responses.

**DTO Layer (`dto/`)**
- Defines request/response objects for API.
- Keeps external contract stable and decoupled from database entities.

**Service Layer (`service/`)**
- Business logic:
  - Build ticket serial `{YY}{GGG}{BB}{PPPPPPP}{TTT}`.
  - Compute hash (BLAKE2b or SHA-256).
  - Apply claim rules (monotonic, no decrease).
  - Append audit log with chained signature.

**Repository Layer (`repository/`)**
- Spring Data JPA interfaces for DB access.

**Model Layer (`model/`)**
- JPA entities mapped to tables: `games`, `winners`, `claimlog`.

**Resources**
- `application.yml` — profiles (H2, PostgreSQL).
- `schema.sql` — DB schema init.
- `data.sql` — sample data.
- `static/` — optional simple UI.

**Test Layer**
- Unit tests (e.g., hashing), integration tests using H2.

---

## 3. Deterministic Hashing
**Serial format:** `{YY}{GGG}{BB}{PPPPPPP}{TTT}`
- YY = `customer_id` (2), GGG = `game_id` (3), BB = `batch_id` (2)
- PPPPPPP = `pack_id` (0..7 → left-padded to 7; optional)
- TTT = `ticket_id` (3)

**Algorithm per game (`games.hash_algoritham`):**
- `1` → BLAKE2b-512 → 128 hex chars
- `2` → SHA-256 → 64 hex chars

---

## 4. Claim State Machine
- Allowed transitions: **0 → 1 → 2** only.
- Decrease (e.g., 2→1) is blocked with a clear message.
- Idempotent requests (x→x) are no-op by default.

---

## 5. Audit Trail (claimlog)
Each successful claim update appends **one** row:
- Captures `tx_*` fields, `old_claim_value`, `new_claim_value`, optional `foreign_ref1..4`.
- `signature` is a chain: `BLAKE2b(payload + previous_signature)` → tamper-evident history.

---

## 6. Runtime Profiles & Multi-tenancy Plan
- **Default:** H2 in-memory for local runs and tests.
- **postgres:** local Postgres for runtime demo (loads `schema.sql`).
- **Next:** CSV-driven tenants + routing based on `customer_id`.

---

## 7. Why This Design
- Clear separation of concerns → easier maintenance and testing.
- DB-agnostic model (works with PostgreSQL/H2; MySQL compatible).
- Deterministic hashing aligned with pre-generated winner data.
- Minimal surface area now; ready for production hardening later.
