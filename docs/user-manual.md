# User Manual

## Purpose
Validate tickets and update claim status for multi-tenant deployments.

## Configure Tenants
Edit `/config/tenants.csv`:
customer_ID,customer_Name,customer_Status,customer_Database_connect_string
11,Big Lottery,1,jdbc:mysql://localhost:3306/customer_11?user=root&password=root
22,Small Lottery,1,jdbc:postgresql://localhost:5432/customer_22?user=postgres&password=root

## API Usage
- POST /api/win/check → check if ticket is winner
- POST /api/claim → update claim status and log audit

## Notes
- Hashing rule: {YY}{GGG}{BB}{PPPPPPP}{TTT}
- Algorithm per game: 1=Blake2b, 2=SHA256
