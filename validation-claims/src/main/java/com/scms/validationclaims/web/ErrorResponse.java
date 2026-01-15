
package com.scms.validationclaims.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Data
@Schema(name = "ErrorResponse", description = "Consistent error response for API")
public class ErrorResponse {
    @Schema(example = "2026-01-05T15:26:12+01:00")
    private String timestamp;
    @Schema(example = "404")
    private int status;
    @Schema(example = "Not Found")
    private String error;
    @Schema(example = "NOT_WINNER")
    private String code;
    @Schema(example = "not winner")
    private String message;

    private Map<String, Object> details;

    @Schema(example = "Only monotonic transitions allowed: 0→1→2")
    private String hint;

    @Schema(example = "2df3c2e4-8b5a-47c8-9c2b-8a1f0e847d1a")
    private String correlation_id;

    public static ErrorResponse of(HttpStatus status, String code, String message,
                                   Map<String, Object> details, WebRequest req) {
        ErrorResponse er = new ErrorResponse();
        er.timestamp = OffsetDateTime.now().toString();
        er.status = status.value();
        er.error = status.getReasonPhrase();
        er.code = code;
        er.message = (message == null || message.isBlank()) ? status.getReasonPhrase() : message;
        er.details = details;
        er.correlation_id = Optional.ofNullable(req.getHeader("X-Correlation-Id"))
                .orElse(UUID.randomUUID().toString());
        return er;
    }

    public ErrorResponse withHint(String hint) { this.hint = hint; return this; }
}
