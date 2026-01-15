# Validation & Claims – Java PoC

## 📌 Purpose
A proof-of-concept microservice for scratch-card ticket **validation** (`win`) and **claims processing** (`claim`) across multiple tenants (lotteries).
This PoC focuses on **functionality over performance** and deliberately limits scope to the **win/claim** workflow.

---

## Key Features
- **Language:** Java 17+, Spring Boot 3.x
- **Persistence:** JPA/Hibernate
- **Database:** PostgreSQL (runtime), H2 (tests/local)
- **Endpoints:**
    - `POST /api/win/check` – validate ticket
    - `POST /api/claim` – process claim (monotonic 0→1→2, no decreases)
- **Ticket hashing:** `{YY}{GGG}{BB}{PPPPPPP}{TTT}` + algorithm per game (`1=BLAKE2b`, `2=SHA-256`)
- **Audit trail:** ClaimLog entries with chained signature
- **Simple Web UI:** Available at http://localhost:8080/index.html

## How to Use
### 1. Run locally with H2 (default profile)
```bash
cd validation-claims
mvn spring-boot:run
```
Access the simple UI: http://localhost:8080/index.html
### 2. Run with PostgreSQL
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```
Ensure:
* Database exists (e.g., customer_22)
* Credentials match `application.yml`
* `schema.sql` will be auto-loaded from `resources`

## Repo Structure
```
validation-claims-poc/
├── validation-claims/
│   ├── docs/                # Documentation (see links below)
│   ├── src/
│   │   ├── main/java/...    # Controllers, DTOs, Services, Repositories, Models
│   │   ├── main/resources/  # application.yml, schema.sql, data.sql, static UI
│   │   └── test/java/...    # Unit & integration tests (H2)
│   └── pom.xml
└── README.md
```

## Documentation
- [Architecture & Design](validation-claims/docs/architecture.md)
System layers, hashing logic, claim rules, audit trail.
- [API Contract](validation-claims/docs/api-contract.md)
Request/response formats, error codes
- [User Manual](validation-claims/docs/user-manual.md)
How to run(H2/Postgres)
