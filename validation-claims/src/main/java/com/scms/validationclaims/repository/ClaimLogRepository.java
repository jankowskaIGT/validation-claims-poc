package com.scms.validationclaims.repository;

import com.scms.validationclaims.model.ClaimLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.List;


public interface ClaimLogRepository extends JpaRepository<ClaimLog, Integer> {


    @Query("""
        SELECT c.signature
        FROM ClaimLog c
        WHERE c.txCustomerId = :customerId
          AND c.txGameId     = :gameId
          AND c.txBatchId    = :batchId
          AND c.txPackId     = :packId
          AND c.txTicketId   = :ticketId
        ORDER BY c.idclaimlog DESC
    """)
    List<ClaimLog> findLatestByTicket(String customerId, String gameId, String batchId, String packId, String ticketId, Pageable pageable);

}
