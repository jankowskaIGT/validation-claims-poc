
package com.scms.validationclaims.exception;

import lombok.Getter;

@Getter
public class ClaimDecreaseForbiddenException extends RuntimeException {

    private final int oldValue;
    private final int requested;
    private final String ticketHash;

    public ClaimDecreaseForbiddenException(int oldValue, int requested, String ticketHash) {
        super("cannot decrease claim status");
        this.oldValue = oldValue;
        this.requested = requested;
        this.ticketHash = ticketHash;
    }
}
