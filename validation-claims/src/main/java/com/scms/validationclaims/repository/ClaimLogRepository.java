package com.scms.validationclaims.repository;

import com.scms.validationclaims.model.ClaimLog;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface ClaimLogRepository extends JpaRepository<ClaimLog, Integer> {

    Optional<ClaimLog>
    findTopByTxCustomerIdAndTxGameIdAndTxBatchIdAndTxPackIdAndTxTicketIdOrderByIdclaimlogDesc(
            String customerId, String gameId, String batchId, String packId, String ticketId
    );
}
