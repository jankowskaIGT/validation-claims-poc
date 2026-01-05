package com.scms.validationclaims.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@Schema(name = "CheckRequest", description = "Win check")
public class CheckRequest {

    @NotBlank
    @Schema(description = "Client identification (YY)", pattern = "^\\d{2}$",
           requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @Pattern(regexp = "\\d{1,2}")
    private String customer_id;

    @NotBlank
    @Schema(description = "Game ID (GGG)", pattern = "^\\d{3}$",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    @Pattern(regexp = "\\d{1,3}")
    private String game_id;

    @NotBlank
    @Schema(description = "Batch ID (BB)", pattern = "^\\d{2}$",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "01")
    @Pattern(regexp = "\\d{1,2}")
    private String batch_id;

    @NotBlank
    @Schema(description = "Ticket ID (TTT)", pattern = "^\\d{3}$",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "007")
    @Pattern(regexp = "\\d{1,3}")
    private String ticket_id;


    @Schema(description = "Pack ID (PPPPPPP) — opotional", pattern = "^\\d{0,7}$",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "0000123")
    @Pattern(regexp = "\\d{1,7}")
    private String pack_id;
}

