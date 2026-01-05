INSERT INTO games (game_id, ticket_price, hash_algorithm) VALUES
  ('101', 1.00, 2),  -- SHA-256
  ('202', 2.00, 1);  -- BLAKE2b-512

INSERT INTO winners (ticket_hash, game_id, batch_id, ticket_winning_tier_id, ticket_claim_status, ticket_winning_prize) VALUES
  -- From your CSVs (works with /win and /claim)
  ('dbedcc2af1e9694684e67ed28f15394e47bc3131eb7b2170e8bc2970df47d4dc496422231a88e909e84474a956bbd82a7244380b06302f4c61b30aad9d26bce5', '101', '01', '4', 0, 0.00),
  ('f993d06b32cdae6966a62c3bc00aec88da593a7c2a4ada06ae7ec75eadae99250266a184bcb35bfb132c5064836e7273e33453f734937a00a88ce71529b5be0a', '101', '01', '4', 0, 0.00),
  ('a9565fa647df9e0fdb4d683627377681174572e449ed1f3087254fc8a3da0a55', '202', '01', '4', 0, 0.00),
  ('7d2989959dfd7412ffd0e847c6ebffd7835c68801475117491259b45ff4a22ce', '202', '01', '4', 0, 0.00);
