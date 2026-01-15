
package com.scms.validationclaims.web;

import com.scms.validationclaims.exception.BusinessValidationException;
import com.scms.validationclaims.exception.ClaimDecreaseForbiddenException;
import com.scms.validationclaims.exception.NotWinnerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 – ticket is not a winner
    @ExceptionHandler(NotWinnerException.class)
    public ResponseEntity<ErrorResponse> notWinner(NotWinnerException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND, "NOT_WINNER", ex.getMessage(),
                        Map.of("ticket_hash", ex.getTicketHash()),
                        req
                ));
    }

    // 409 – attempt to reduce the status of a claim
    @ExceptionHandler(ClaimDecreaseForbiddenException.class)
    public ResponseEntity<ErrorResponse> decrease(ClaimDecreaseForbiddenException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        HttpStatus.CONFLICT, "CLAIM_DECREASE_FORBIDDEN", ex.getMessage(),
                        Map.of("old_claim", ex.getOldValue(),
                                "requested", ex.getRequested(),
                                "ticket_hash", ex.getTicketHash()),
                        req
                ).withHint("Only monotonic transitions allowed: 0→1→2"));
    }

    // 422 – syntactically OK, but they break business rules
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> business(BusinessValidationException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(
                        HttpStatus.UNPROCESSABLE_ENTITY, ex.getCode(), ex.getMessage(),
                        ex.getDetails(), req
                ));
    }

    // 400 – validation error DTO (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> dto(MethodArgumentNotValidException ex, WebRequest req) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "error", fe.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST, "DTO_VALIDATION_ERROR", "Invalid request fields",
                        Map.of("field_errors", fieldErrors), req
                ));
    }

    // (Optional)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalArg(IllegalArgumentException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST, "ILLEGAL_ARGUMENT", ex.getMessage(),
                        Map.of("exception", ex.getClass().getName()), req
                ));
    }

    // 500 – unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> any(Exception ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", ex.getMessage(),
                        Map.of("exception", ex.getClass().getName()), req
                ));
    }
}
