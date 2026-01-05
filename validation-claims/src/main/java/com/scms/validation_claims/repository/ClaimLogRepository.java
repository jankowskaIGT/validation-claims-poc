package com.scms.validation_claims.repository;

import com.scms.validation_claims.model.ClaimLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClaimLogRepository extends JpaRepository<ClaimLog, Integer> {

    @Query("select c.signature from ClaimLog c order by c.idclaimlog desc")
    Optional<String> findLastSignature();
}
