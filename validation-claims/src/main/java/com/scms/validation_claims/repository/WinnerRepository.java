package com.scms.validation_claims.repository;

import com.scms.validation_claims.model.Winner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WinnerRepository extends JpaRepository<Winner, String> { // PK = ticketHash
    List<Winner> findByGameIdAndBatchId(String gameId, String batchId);
}
