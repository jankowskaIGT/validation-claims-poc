package com.scms.validationclaims.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "claimlog")
public class ClaimLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idclaimlog")
    private Integer idclaimlog;

    @Column(name = "tx_date", nullable = false)
    private LocalDate txDate;

    @Column(name = "tx_time", nullable = false)
    private LocalTime txTime;

    @Column(name = "tx_customer_id", length = 2)
    private String txCustomerId;

    @Column(name = "tx_game_id", length = 3)
    private String txGameId;

    @Column(name = "tx_batch_id", length = 2)
    private String txBatchId;

    @Column(name = "tx_pack_id", length = 7)
    private String txPackId;

    @Column(name = "tx_ticket_id", length = 3)
    private String txTicketId;

    @Column(name = "old_claim_value")
    private Integer oldClaimValue;

    @Column(name = "new_claim_value")
    private Integer newClaimValue;

    @Column(name = "foreign_ref1", length = 45)
    private String foreignRef1;
    @Column(name = "foreign_ref2", length = 45)
    private String foreignRef2;
    @Column(name = "foreign_ref3", length = 45)
    private String foreignRef3;
    @Column(name = "foreign_ref4", length = 45)
    private String foreignRef4;

    @Column(name = "signature", length = 128)
    private String signature;
}
