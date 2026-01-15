
package com.scms.validationclaims.service;

import com.scms.validationclaims.dto.CheckRequest;
import com.scms.validationclaims.dto.CheckResponse;
import com.scms.validationclaims.dto.ClaimRequest;
import com.scms.validationclaims.dto.ClaimResponse;

import com.scms.validationclaims.exception.BusinessValidationException;
import com.scms.validationclaims.exception.ClaimDecreaseForbiddenException;
import com.scms.validationclaims.exception.NotWinnerException;

import com.scms.validationclaims.model.ClaimLog;
import com.scms.validationclaims.model.Game;
import com.scms.validationclaims.model.Winner;

import com.scms.validationclaims.repository.ClaimLogRepository;
import com.scms.validationclaims.repository.GameRepository;
import com.scms.validationclaims.repository.WinnerRepository;

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

        // NORMALIZACJA – zawsze zapisz jako zera z lewej
        String customer = zfillDigits(Optional.ofNullable(req.getCustomer_id()).orElse(""), 2); // <--
        String game     = zfillDigits(Optional.ofNullable(req.getGame_id()).orElse(""), 3);     // <--
        String batch    = zfillDigits(Optional.ofNullable(req.getBatch_id()).orElse(""), 2);    // <--
        String pack     = zfillDigits(Optional.ofNullable(req.getPack_id()).orElse(""), 7);     // <--
        String ticket   = zfillDigits(Optional.ofNullable(req.getTicket_id()).orElse(""), 3);   // <--

        String serial = hashing.buildSerial(customer, game, batch, pack, ticket);
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

        // NORMALIZACJA – identycznie jak w check()
        String customer = zfillDigits(Optional.ofNullable(req.getCustomer_id()).orElse(""), 2); // <--
        String game     = zfillDigits(Optional.ofNullable(req.getGame_id()).orElse(""), 3);     // <--
        String batch    = zfillDigits(Optional.ofNullable(req.getBatch_id()).orElse(""), 2);    // <--
        String pack     = zfillDigits(Optional.ofNullable(req.getPack_id()).orElse(""), 7);     // <--
        String ticket   = zfillDigits(Optional.ofNullable(req.getTicket_id()).orElse(""), 3);   // <--

        String serial = hashing.buildSerial(customer, game, batch, pack, ticket);
        String th = hashing.hash(alg, serial);

        Winner w = winnerRepo.findById(th).orElse(null);
        if (w == null) {
            // -> 404 Not Found (GlobalExceptionHandler)
            throw new NotWinnerException(th);
        }

        int oldClaim = Optional.ofNullable(w.getTicketClaimStatus()).orElse(0);

        Integer desiredRaw = req.getDesired_claim_value();
        int desired = Optional.ofNullable(desiredRaw).orElse(oldClaim);

        if (desiredRaw != null && desired != 1 && desired != 2) {
            throw new BusinessValidationException(
                    "CLAIM_VALUE_INVALID",
                    "desired_claim_value must be 1 or 2",
                    java.util.Map.of("requested", desiredRaw)
            );
        }

        if (desired < oldClaim) {
            throw new ClaimDecreaseForbiddenException(oldClaim, desired, th);
        }

        // If desired equals current, treat as idempotent no-op (no state change).
        boolean updated = desired > oldClaim;
        if (updated) {
            w.setTicketClaimStatus(desired);
            winnerRepo.save(w);
        }

        // Używaj ZNORMALIZOWANYCH kluczy także do odczytu poprzedniego podpisu:
        String prevSig = logRepo.findLatestByTicket(
                customer,   // <--
                game,       // <--
                batch,      // <--
                pack,       // <--
                ticket,     // <--
                PageRequest.of(0, 1)
        ).stream().findFirst().orElse("");

        // Zapisuj do loga ZNORMALIZOWANE wartości:
        ClaimLog log = new ClaimLog();
        log.setTxDate(LocalDate.now());
        log.setTxTime(LocalTime.now().withNano(0));
        log.setTxCustomerId(customer);  // <--
        log.setTxGameId(game);          // <--
        log.setTxBatchId(batch);        // <--
        log.setTxPackId(pack);          // <--
        log.setTxTicketId(ticket);      // <--
        log.setOldClaimValue(oldClaim);
        log.setNewClaimValue(desired);
        log.setForeignRef1(req.getF1());
        log.setForeignRef2(req.getF2());
        log.setForeignRef3(req.getF3());
        log.setForeignRef4(req.getF4());
        log.setSignature(chainSignature(prevSig, log));
        logRepo.save(log);

        return new ClaimResponse(true, updated, th, oldClaim, desired, null);
    }

    /** Zero-fill digits with validation. Accepts empty string as "no value". */
    private static String zfillDigits(String s, int width) {
        String v = (s == null ? "" : s).trim();
        if (!v.matches("\\d*")) throw new IllegalArgumentException("digits only: " + v);
        if (v.length() > width) throw new IllegalArgumentException("too long: " + v);
        return "0".repeat(width - v.length()) + v;
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
