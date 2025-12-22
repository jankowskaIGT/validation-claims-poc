# Test Cases

TC01: Winner found (SHA256)
TC02: Winner found (Blake2b)
TC03: Not a winner
TC04: Missing pack_id
TC05: Unknown tenant
TC06: Claim 0→1
TC07: Claim 1→2
TC08: Disallow decrease
TC09: Idempotent same state
TC10: Invalid payload types
TC11: Foreign refs captured
TC12: Hash algorithm per game
TC13: Add game & import winners
TC14: Load winners CSV (headers OK)
TC15: Load winners CSV (headers missing)
TC16: Remove winners subset
TC17: Remove game safe
TC18: Reset claims to 0
TC19: Report totals
TC20: Verify claimlog chain
TC21: Verify last N rows
TC22: Tamper detection
TC23: Tenant 11 end-to-end
TC24: Tenant 22 end-to-end
TC25: Unknown game_id
TC26: Bad DSN in config
TC27: Large/invalid field lengths
