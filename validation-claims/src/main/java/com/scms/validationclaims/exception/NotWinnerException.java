package com.scms.validationclaims.exception;

import lombok.Getter;

@Getter
public class NotWinnerException extends RuntimeException {

    private final String ticketHash;

    public NotWinnerException(String ticketHash) {
        super("not winner");
        this.ticketHash = ticketHash;
    }
}
