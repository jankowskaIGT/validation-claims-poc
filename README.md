# Validation & Claims – Java PoC


## 📌 Purpose
A proof-of-concept microservice for scratch-card ticket **validation** (`win`) and **claims processing** (`claim`) across multiple tenants (lotteries), each backed by its own database (PostgreSQL or H2 for local runs).  
This PoC focuses on **functionality over performance** and deliberately **limits scope** to the win/claim workflow.


## Key Features
- **Language:** Java 17+, Spring Boot 3.x
- **Persistence:** JPA/Hibernate
- **Database:** PostgreSQL (runtime), H2 (tests/local)
- **Endpoints:**
    - `POST /api/win/check` – validate ticket
    - `POST /api/claim` – process claim
- **Ticket hashing:** `{YY}{GGG}{BB}{PPPPPPP}{TTT}` + algorithm per game (`1=BLAKE2b`, `2=SHA-256`)
- **Audit trail:** ClaimLog entries with chained signature
- **Simple Web UI:** Available at http://localhost:8080/index.html


## Repo Structure
```
validation-claims-poc/
├── .idea/ # Local IntelliJ IDEA configuration (IDE-specific, not required for build/runtime)
├── validation-claims/ # Main Spring Boot application module
│   ├── .mvn/ # Maven Wrapper files (allows running Maven without a local installation)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/scms/validationclaims/
│   │   │   │   ├── controller/ # REST controllers (HTTP request/response handling)
│   │   │   │   ├── dto/        # Data Transfer Objects (request and response payloads)
│   │   │   │   ├── model/      # JPA entities (Game, Winner, ClaimLog)
│   │   │   │   ├── repository/ # Spring Data JPA repositories (database access layer)
│   │   │   │   ├── service/    # Business logic (e.g. CheckClaimService, HashingService)
│   │   │   │   └── ValidationClaimsApplication # Spring Boot application entry point
│   │   │   └── resources/
│   │   │       ├── static/
│   │   │       │   └── # Static assets (simple HTML UI, e.g. index.html)
│   │   │       ├── application.yml # Application configuration (profiles: default H2, PostgreSQL)
│   │   │       │   └── 
│   │   │       ├── schema.sql      # Database schema initialization
│   │   │       │   └── 
│   │           └── data.sql        # Optional sample data for local development/testing
│   └── test/
│       ├── java/com/scms/validationclaims/    # Unit and integration tests
│       └── resources/application-test.yml      # Test profile configuration (H2 in-memory database)
├── pom.xml   # Maven build configuration (dependencies, plugins)
└── README.md # Project overview and usage documentation
```
## How to Use
### 1. Run locally with H2 (default profile)
```bash
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