package com.scms.validationclaims.repository;

import com.scms.validationclaims.model.Winner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WinnerRepository extends JpaRepository<Winner, String> { // PK = ticketHash
    List<Winner> findByGameIdAndBatchId(String gameId, String batchId);

    Optional<Winner> findByTicketHashAndGameIdAndBatchId(String ticketHash, String gameId, String batchId);
}
