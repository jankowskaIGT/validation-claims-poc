
package com.scms.validationclaims.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import com.scms.validationclaims.dto.CheckRequest;
import com.scms.validationclaims.dto.CheckResponse;

import com.scms.validationclaims.service.CheckClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ValidationController {

    private final CheckClaimService service;

    @Operation(
            summary = "Win check",
            description = "Computes ticket_hash from inputs and looks up the 'winners' table. No writes."
    )

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CheckRequest.class),
                    examples = @ExampleObject(
                            name = "Typical win-check",
                            value = """
                {
                  "customer_id":"11",
                  "game_id":"101",
                  "batch_id":"01",
                  "ticket_id":"007",
                  "pack_id":"0000123"
                }
                """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Win check result",
                    content = @Content(
                            schema = @Schema(implementation = CheckResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Winner",
                                            value = """
                        {
                          "found": true,
                          "ticket_hash": "d34db33fd8...9a",
                          "tier": "1",
                          "claim_status": 0,
                          "message": null
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "Not winner",
                                            value = """
                        {
                          "found": false,
                          "ticket_hash": "0f1e2d3c4b...aa",
                          "tier": null,
                          "claim_status": 0,
                          "message": "not winner"
                        }
                        """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Validation error / unknown or inactive tenant")
    })
    @PostMapping("/win/check")
    public ResponseEntity<CheckResponse> check(
            @Valid @RequestBody CheckRequest req
    ) {
        return ResponseEntity.ok(service.check(req));
    }
}
