package com.scms.validation_claims.repository;

import com.scms.validation_claims.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {
    Optional<Game> findByGameId(String gameId);
}
