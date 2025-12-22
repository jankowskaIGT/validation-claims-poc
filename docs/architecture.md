
# Architecture & Design

## Goals
- Implement **win** and **claim** functionality only.
- Database-agnostic via JPA/Hibernate.
- Multi-tenant: tenants defined in CSV.
- No pack activation validation.

## Components
- REST API: `/api/win/check`, `/api/claim`
- Tenant config: `/config/tenants.csv`
- Databases: MySQL/PostgreSQL with tables `games`, `winners`, `claimlog`

## Data Model
- games: idgame, game_id, ticket_price, hash_algoritham
- winners: ticket_hash (PK), game_id, batch_id, tier, claim_status, prize
- claimlog: idclaimlog, tx_date, tx_time, old/new claim values, foreign refs, signature

## Hashing
Serial: `{YY}{GGG}{BB}{PPPPPPP}{TTT}` widths: 2/3/2/7/3
Algorithm: 1=Blake2b, 2=SHA256

## Tenancy
Load tenants from CSV; route DB ops per tenant.

## Persistence
JPA/Hibernate mappings; identity strategy; indexes on ticket_hash.
