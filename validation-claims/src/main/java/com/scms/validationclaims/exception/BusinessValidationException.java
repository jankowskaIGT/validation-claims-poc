
package com.scms.validationclaims.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class BusinessValidationException extends RuntimeException {

    private final String code;
    private final Map<String, Object> details;

    public BusinessValidationException(String code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = details;
    }
}
