package com.scms.validationclaims.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idgame")
    private Integer idgame;

    @Column(name = "game_id", length = 3, nullable = false)
    private String gameId;

    @Column(name = "ticket_price", precision = 18, scale = 2)
    private BigDecimal ticketPrice;

    /** 1 = Blake2b, 2 = SHA256 */
    @Column(name = "hash_algorithm")
    private Integer hashAlgorithm;
}
