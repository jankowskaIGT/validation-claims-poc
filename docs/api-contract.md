
# API Contract

## POST /api/win/check
Request:
{
"customer_id": "11",
"game_id": "101",
"batch_id": "01",
"ticket_id": "007",
"pack_id": "0000123"
}

Response (winner):
{
"found": true,
"ticket_hash": "...",
"tier": "1",
"prize": 1.50,
"claim_status": 0
}

Response (not winner):
{
"found": false,
"message": "not winner",
"ticket_hash": "..."
}

## POST /api/claim
Request:
{
"customer_id": "11",
"game_id": "101",
"batch_id": "01",
"ticket_id": "007",
"desired_claim_value": 1,
"pack_id": "0000123",
"f1": "channel id",
"f2": "device",
"f3": "person",
"f4": "external_ref"
}

Response:
{
"found": true,
"updated": true,
"ticket_hash": "...",
"old_claim_value": 0,
"new_claim_value": 1
}
