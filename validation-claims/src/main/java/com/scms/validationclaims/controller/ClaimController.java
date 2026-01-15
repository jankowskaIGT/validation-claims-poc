package com.scms.validationclaims.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import com.scms.validationclaims.dto.ClaimRequest;
import com.scms.validationclaims.dto.ClaimResponse;
import com.scms.validationclaims.service.CheckClaimService;
import com.scms.validationclaims.web.ErrorResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Claims", description = "API for claim updates (CLAIM) and auditing")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClaimController {

    private final CheckClaimService service;

    @Operation(
            summary = "Claim update (monotonic 0→1→2)",
            description = "Updates winner.claim_status and appends exactly one claimlog row with chained BLAKE2b signature.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Claim update result (happy path)",
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
                                            name = "No-op (idempotent)",
                                            value = """
                                                    {
                                                      "found": true,
                                                      "updated": false,
                                                      "ticket_hash": "cf32e868ec36d1b1a9...f86",
                                                      "old_claim_value": 1,
                                                      "new_claim_value": 1,
                                                      "message": "no change"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            // Błędy obsługiwane przez GlobalExceptionHandler:
            @ApiResponse(
                    responseCode = "404",
                    description = "Not winner",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Not winner",
                                    value = """
                                            {
                                              "timestamp": "2026-01-05T15:20:10+01:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "code": "NOT_WINNER",
                                              "message": "not winner",
                                              "details": { "ticket_hash": "..." },
                                              "correlation_id": "test-404"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Decrease forbidden (monotonicity violation)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Decrease 2→1 blocked",
                                    value = """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "code": "CLAIM_DECREASE_FORBIDDEN",
                                              "message": "cannot decrease claim status",
                                              "details": { "old_claim": 2, "requested": 1, "ticket_hash": "..." },
                                              "hint": "Only monotonic transitions allowed: 0→1→2",
                                              "correlation_id": "test-409"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Business rule violated (e.g., desired_claim_value ∉ {1,2})",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Invalid desired_claim_value",
                                    value = """
                                            {
                                              "status": 422,
                                              "error": "Unprocessable Entity",
                                              "code": "CLAIM_VALUE_INVALID",
                                              "message": "desired_claim_value must be 1 or 2",
                                              "details": { "requested": 3 },
                                              "correlation_id": "test-422"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "DTO validation error / IllegalArgumentException",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Bad Request",
                                    value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "code": "DTO_VALIDATION_ERROR",
                                              "message": "Invalid request fields",
                                              "details": { "field_errors": [ { "field": "game_id", "error": "must match ^\\d{3}$" } ] },
                                              "correlation_id": "test-400"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping(path = "/claim")
    public ResponseEntity<ClaimResponse> claim(@Valid @RequestBody ClaimRequest req) {
        return ResponseEntity.ok(service.claim(req));
    }
}