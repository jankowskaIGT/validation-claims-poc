
# API Contract — Validation & Claims (Java PoC)

Base path: `/api`  
Content type: `application/json`  

---

## 1) POST `/api/win/check`
### Purpose
Read-only winner lookup. Computes `ticket_hash` from the deterministic serial and returns winner metadata if found.

### Request (JSON) - non-winner example
```json
{
  "customer_id": "11",
  "game_id": "101",
  "batch_id": "01",
  "ticket_id": "007",
  "pack_id": "0000123"
}
```
### Request (JSON) - winner example
```json
{
  "customer_id": "22",
  "game_id": "202",
  "batch_id": "01",
  "ticket_id": "000",
  "pack_id": "0000000"
}
```
**Field rules**
* customer_id — string, **2 digits**
* game_id — string, **3 digits**
* batch_id — string, **2 digits**
* ticket_id — string, **3 digits**
* pack_id — string, **0..7 digits**, optional (zero-padded to 7 in serial)

### Response — winner
```json
{
"found": true,
"ticket_hash": "…",
"tier": "...",
"claim_status": 0,
"message": null
}
```
### Response — not winner
```json
{
"found": false,
"ticket_hash": "…",
"tier": null,
"claim_status": 0,
"message": "not winner"
}
```
**Notes**

* Serial format: {YY}{GGG}{BB}{PPPPPPP}{TTT} (widths 2/3/2/7/3).
* Hash algorithm is per game (games.hash_algoritham): 1=BLAKE2b-512 (128 hex), 2=SHA-256 (64 hex).

## 2) POST `/api/claim`
### Purpose
Monotonic claim transition **(0→1→2 only)**. Appends exactly one row to claimlog with a chained signature if an update occurs.
### Request (JSON)
```json
{
  "customer_id": "11",
  "game_id": "101",
  "batch_id": "01",
  "ticket_id": "007",
  "desired_claim_value": 2,
  "pack_id": "0000123",
  "f1": "channel",
  "f2": "device",
  "f3": "person",
  "f4": "external_ref"
}
```
**Field rules**
* Same identity fields as `/win/check`.
* `desired_claim_value` — integer, allowed values: 1 or 2.
* `f1..f4` — optional strings captured in the audit row.

### Response — updated
```json
{
"found": true,
"updated": true,
"ticket_hash": "…",
"old_claim_value": 0,
"new_claim_value": 1,
"message": null
}
```
### Response — idempotent (x→x)
```json
{
"found": true,
"updated": false,
"ticket_hash": "...",
"old_claim_value": 2,
"new_claim_value": 2,
"message": null
}
```

## 3) Error Model
- **400 Bad Request** — validation issues (wrong lengths, missing fields, non-integer `desired_claim_value`, etc.)
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "code": "DTO_VALIDATION_ERROR",
  "message": "Invalid request fields",
  "details": {
    "field_errors": [
      {
        "field": "batch_id",
        "error": "must match \"\\d{1,2}\""
      }
    ]
  },
  "hint": null,
  "correlation_id": "..."
}
```
- **404 Not Found** — *optional policy* for unknown `game_id` / missing winner (PoC defaults to 200 with `found=false`)
### Response — not winner (404)
```json
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "code": "NOT_WINNER",
  "message": "not winner",
  "details": {
    "ticket_hash": "..."
  },
  "hint": null,
  "correlation_id": "..."
}
```
- **409 Conflict** — **decrease attempt** (e.g., 2→1). Business rule enforced via exception → handler → 409.
### Response — blocked decrease (409)
```json
{
  "timestamp": "...",
  "status": 409,
  "error": "Conflict",
  "code": "CLAIM_DECREASE_FORBIDDEN",
  "message": "cannot decrease claim status",
  "details": {
    "requested": 1,
    "ticket_hash": "...",
    "old_claim": 2
  },
  "hint": "Only monotonic transitions allowed: 0→1→2",
  "correlation_id": "..."
}
```
- **500 Internal Server Error** — unexpected server/database error (should not be used for business rule violations)