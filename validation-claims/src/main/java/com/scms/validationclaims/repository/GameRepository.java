package com.scms.validationclaims.repository;

import com.scms.validationclaims.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {
    Optional<Game> findByGameId(String gameId);
}
