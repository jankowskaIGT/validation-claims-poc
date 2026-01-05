package com.scms.validationclaims.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import com.scms.validationclaims.dto.ClaimRequest;
import com.scms.validationclaims.dto.ClaimResponse;
import com.scms.validationclaims.service.CheckClaimService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClaimController {

    private final CheckClaimService service;

    @Operation(
            summary = "Claim update (monotonic 0→1→2)",
            description = "Updates winner.claim_status and appends exactly one claimlog row with chained BLAKE2b signature."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = ClaimRequest.class),
                    examples = @ExampleObject(
                            name = "Typical claim update",
                            value = """
            {
              "customer_id":"11",
              "game_id":"101",
              "batch_id":"01",
              "ticket_id":"007",
              "pack_id":"0000123",
              "desired_claim_value": 1,
              "f1":"channel_id",
              "f2":"device",
              "f3":"person",
              "f4":"external_ref"
            }
            """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Claim update result",
                    content = @Content(
                            schema = @Schema(implementation = ClaimResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Updated 0→1",
                                            value = """
                    {
                      "found": true,
                      "updated": true,
                      "ticket_hash": "cf32e868ec36d1b1a9...f86",
                      "old_claim_value": 0,
                      "new_claim_value": 1,
                      "message": null
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Decrease blocked 2→1",
                                            value = """
                    {
                      "found": true,
                      "updated": false,
                      "ticket_hash": "cf32e868ec36d1b1a9...f86",
                      "old_claim_value": 2,
                      "new_claim_value": 2,
                      "message": "cannot decrease claim status"
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Non existent – not winner",
                                            value = """
                    {
                      "found": false,
                      "updated": false,
                      "ticket_hash": "cf32e868ec36d1b1a9...f86",
                      "old_claim_value": 0,
                      "new_claim_value": 0,
                      "message": "non existent – not winner"
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "No-op (idempotent)",
                                            value = """
                    {
                      "found": true,
                      "updated": false,
                      "ticket_hash": "cf32e868ec36d1b1a9...f86",
                      "old_claim_value": 1,
                      "new_claim_value": 1,
                      "message": "no-op"
                    }
                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Validation error / unknown tenant / decrease blocked")
    })
    @PostMapping("/claim")
    public ResponseEntity<ClaimResponse> claim(@Valid @RequestBody ClaimRequest req) {
        return ResponseEntity.ok(service.claim(req));


    }
}
