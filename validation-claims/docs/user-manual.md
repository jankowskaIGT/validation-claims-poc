
# User Manual — Validation & Claims (Java PoC)

This guide shows how to run the service locally (H2 and PostgreSQL), call the API, and troubleshoot common issues.  
See also: [`docs/architecture.md`](architecture.md) (how it works) and [`docs/api-contract.md`](api-contract.md) (payloads, error model).

---

## 1. Prerequisites

- **Java** 17 or 21 (Spring Boot 3.x requires Java ≥ 17)
- **Maven** (or use the Maven Wrapper shipped with the repo)
- **PostgreSQL** (only if you want to run with `-Dspring-boot.run.profiles=postgres`)
- Terminal with `curl` (or Postman)

Check:
```bash
java -version
mvn -v
```

## 2. Quick Start (H2 in‑memory, default profile)
**Start:**
```bash
mvn spring-boot:run
```
**Access:**
* App root: http://localhost:8080

**Stop:**
* Ctrl + C in the terminal.

**Notes:**

* H2 in‑memory DB starts empty at each run.
* schema.sql and (optionally) data.sql under src/main/resources/ will initialize tables and sample data if present.

### 3. Run with PostgreSQL (runtime profile)
**Start:**
```
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```
**Make sure:**
* A database exists (e.g., customer_22).
* Credentials in src/main/resources/application.yml are correct for the postgres profile.
schema.sql (and optionally data.sql) will auto‑execute on startup.