package com.scms.validation_claims.service;

import com.scms.validation_claims.dto.CheckRequest;
import com.scms.validation_claims.dto.CheckResponse;
import com.scms.validation_claims.dto.ClaimRequest;
import com.scms.validation_claims.dto.ClaimResponse;
import com.scms.validation_claims.model.ClaimLog;
import com.scms.validation_claims.model.Game;
import com.scms.validation_claims.model.Winner;
import com.scms.validation_claims.repository.ClaimLogRepository;
import com.scms.validation_claims.repository.GameRepository;
import com.scms.validation_claims.repository.WinnerRepository;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.jcajce.provider.digest.Blake2b;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CheckClaimService {

    private final GameRepository gameRepo;
    private final WinnerRepository winnerRepo;
    private final ClaimLogRepository logRepo;
    private final HashingService hashing;

    /** Resolve per-game hashing algorithm (1=BLAKE2b, 2=SHA-256; default 1). */
    private int gameHashAlg(String gameId) {
        Optional<Game> g = gameRepo.findByGameId(gameId);
        return g.map(x -> Optional.ofNullable(x.getHashAlgorithm()).orElse(1))
                .orElse(1);
    }

    /** Read-only check: compute hash, look up winner, return metadata. */
    @Transactional(readOnly = true)
    public CheckResponse check(CheckRequest req) {
        int alg = gameHashAlg(req.getGame_id());
        String serial = hashing.buildSerial(
                req.getCustomer_id(), req.getGame_id(), req.getBatch_id(),
                req.getPack_id(), req.getTicket_id());
        String th = hashing.hash(alg, serial);

        return winnerRepo.findById(th)
                .map(w -> new CheckResponse(
                        true,
                        th,
                        w.getTicketWinningTierId(),
                        Optional.ofNullable(w.getTicketClaimStatus()).orElse(0),
                        null))
                .orElse(new CheckResponse(false, th, null, 0, "not winner"));
    }

    /** Claim: update claim status (no decrease), then write one audit row with chained signature. */
    @Transactional
    public ClaimResponse claim(ClaimRequest req) {
        int alg = gameHashAlg(req.getGame_id());
        String serial = hashing.buildSerial(
                req.getCustomer_id(), req.getGame_id(), req.getBatch_id(),
                req.getPack_id(), req.getTicket_id());
        String th = hashing.hash(alg, serial);

        Winner w = winnerRepo.findById(th).orElse(null);
        if (w == null) {
            return new ClaimResponse(false, false, th, 0, 0, "non existent – not winner");
        }

        int oldClaim = Optional.ofNullable(w.getTicketClaimStatus()).orElse(0);
        int desired = Optional.ofNullable(req.getDesired_claim_value()).orElse(oldClaim);

        if (desired < oldClaim) {
            return new ClaimResponse(true, false, th, oldClaim, oldClaim, "cannot decrease claim status");
        }

        // If desired equals current, treat as idempotent no-op (no state change).
        boolean updated = desired > oldClaim;
        if (updated) {
            w.setTicketClaimStatus(desired);
            winnerRepo.save(w);
        }

        // Append a single audit log row with chained signature
        String previous = logRepo.findLastSignature().orElse("");

        ClaimLog log = new ClaimLog();
        log.setTxDate(LocalDate.now());
        log.setTxTime(LocalTime.now().withNano(0));
        log.setTxCustomerId(req.getCustomer_id());
        log.setTxGameId(req.getGame_id());
        log.setTxBatchId(req.getBatch_id());
        log.setTxPackId(Optional.ofNullable(req.getPack_id()).orElse(""));
        log.setTxTicketId(req.getTicket_id());
        log.setOldClaimValue(oldClaim);
        log.setNewClaimValue(desired);
        log.setForeignRef1(req.getF1());
        log.setForeignRef2(req.getF2());
        log.setForeignRef3(req.getF3());
        log.setForeignRef4(req.getF4());
        log.setSignature(chainSignature(previous, log));
        logRepo.save(log);

        return new ClaimResponse(true, updated, th, oldClaim, desired, null);
    }

    /** Build chained BLAKE2b signature over textual payload + previous signature. */
    private String chainSignature(String prev, ClaimLog e) {
        String payload = Stream.of(
                e.getTxDate(),
                e.getTxTime(),
                e.getTxCustomerId(),
                e.getTxGameId(),
                e.getTxBatchId(),
                e.getTxPackId(),
                e.getTxTicketId(),
                e.getOldClaimValue(),
                e.getNewClaimValue(),
                e.getForeignRef1(),
                e.getForeignRef2(),
                e.getForeignRef3(),
                e.getForeignRef4(),
                Optional.ofNullable(prev).orElse("")
        ).map(v -> v == null ? "" : v.toString().trim()).collect(Collectors.joining("\n"));

        byte[] digest = new Blake2b.Blake2b512().digest(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
