package com.scms.validationclaims.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "ClaimResponse", description = "Result of claim status update")
public class ClaimResponse {

    @Schema(description = "Indicates whether the ticket exists in winners", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean found;

    @Schema(description = "Indicates whether the claim status was updated", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean updated;

    @Schema(description = "Calculated ticket hash", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ticketHash;

    @Schema(description = "Previous claim status (0, 1, or 2)", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer oldClaimValue;

    @Schema(description = "New claim status (1 or 2)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer newClaimValue;

    @Schema(description = "Message (e.g., 'cannot decrease claim status'). Can be null", nullable = true)
    private String message;
}
