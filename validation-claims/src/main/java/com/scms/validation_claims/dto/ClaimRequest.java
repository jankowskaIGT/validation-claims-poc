package com.scms.validation_claims.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ClaimRequest",
        description = "Payload for updating claim status",
        allOf = {CheckRequest.class})

public class ClaimRequest extends CheckRequest {

    @Schema(description = "Target claim status value: 1 (in process) or 2 (paid)",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @Min(1) @Max(2)
    private Integer Desired_claim_value;

    @Schema(description = "Additional reference field 1", nullable = true, example = "channel_id")
    private String f1;

    @Schema(description = "Additional reference field 2", nullable = true, example = "device")
    private String f2;

    @Schema(description = "Additional reference field 3", nullable = true, example = "person")
    private String f3;

    @Schema(description = "Additional reference field 4", nullable = true, example = "external_ref")
    private String f4;
}
