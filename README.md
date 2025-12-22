# Validation & Claims – Java PoC

## Purpose
A proof-of-concept microservice for scratch-card ticket **validation** (“win”) and **claims** processing (“claim”) across multiple tenants (lotteries), each backed by its own database (MySQL or PostgreSQL). This PoC focuses on **functionality over performance** and deliberately **limits scope** to the win/claim workflow.

## Key Facts
- Language: **Java** (Spring Boot recommended)
- Persistence: **JPA/Hibernate**, database-agnostic
- Multi-tenant via **CSV config**
- Endpoints: `POST /api/win/check`, `POST /api/claim`
- Ticket hashing: `{YY}{GGG}{BB}{PPPPPPP}{TTT}` + algorithm per game (`1=Blake2b`, `2=SHA256`)
- Audit trail: ClaimLog entries with chained signature

## Repo Structure

### How to Use
1. Configure tenants in `/config/tenants.csv`.
2. Load schemas from `/config/mysql/schema.sql` and `/config/postgres/schema.sql`.
3. Review `/docs` for architecture, API contract, and test cases.
4. Implement Java microservice guided by these docs.
``
