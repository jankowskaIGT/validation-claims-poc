package com.scms.validationclaims.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Schema(name = "CheckResponse", description = "Result of the win check")
@AllArgsConstructor
public class CheckResponse {
    @Schema(description = "Indicates whether the ticket is a winner", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean found;
    @Schema(description = "Calculated ticket hash", example = "a1b2c3...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ticket_hash;
    @Schema(description = "Winning tier. Can be null for a losing ticket", nullable = true, example = "1")
    private String tier;        // nullable
    @Schema(description = "Claim status (0, 1, 2). For a losing ticket: 0", example = "0")
    private Integer claim_status;
    @Schema(description = "Message (e.g., 'not winner'). Can be null", nullable = true, example = "not winner")
    private String message;     // nullable
}
