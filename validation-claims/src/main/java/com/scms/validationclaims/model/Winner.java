package com.scms.validationclaims.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "winners")
public class Winner {

    @Transient
    @Column(name = "idwinners", unique = true)
    private Integer idwinners;

    @Column(name = "game_id", length = 3, nullable = false)
    private String gameId;

    @Column(name = "batch_id", length = 2, nullable = false)
    private String batchId;

    /** Primary key is ticket_hash */
    @Id
    @Column(name = "ticket_hash", length = 128, nullable = false)
    private String ticketHash;

    @Column(name = "ticket_winning_tier_id", length = 1, nullable = false)
    private String ticketWinningTierId;

    @Column(name = "ticket_claim_status", nullable = false)
    private Integer ticketClaimStatus = 0;

    @Column(name = "ticket_winning_prize", precision = 18, scale = 2)
    private BigDecimal ticketWinningPrize;

}
