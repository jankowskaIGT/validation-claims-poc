package com.scms.validation_claims.service;

import org.bouncycastle.jcajce.provider.digest.Blake2b;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * HashingService builds the ticket serial and hashes it using either:
 * 1 = BLAKE2b-512, 2 = SHA-256 (default if unknown).
 *
 * Serial format (concatenation, no delimiters):
 *   YY(2) + GGG(3) + BB(2) + PPPPPPP(7) + TTT(3)
 * Example: customer_id=11, game_id=101, batch_id=01, pack_id=0000123, ticket_id=007
 * serial = "11" + "101" + "01" + "0000123" + "007" = "11101010000123007"
 */
@Service
public class HashingService {

    /**
     * Build the serial string with zero-filling rules.
     */
    public String buildSerial(String customerId,
                              String gameId,
                              String batchId,
                              String packId,
                              String ticketId) {

        final String yy  = nzfill(customerId, 2);
        final String ggg = nzfill(gameId, 3);
        final String bb  = nzfill(batchId, 2);
        final String ppppppp = zfill(nullToEmpty(packId), 7);
        final String ttt = zfill(ticketId, 3);

        return yy + ggg + bb + ppppppp + ttt;
    }

    /**
     * Compute hash for the given algorithm (1=BLAKE2b, 2=SHA-256).
     * Returns lowercase hex string.
     */
    public String hash(int alg, String serial) {
        byte[] data = serial.getBytes(StandardCharsets.UTF_8);
        byte[] digest;
        if (alg == 1) { // BLAKE2b-512
            digest = new Blake2b.Blake2b512().digest(data);
        } else {        // 2 = SHA-256 (default)
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                digest = md.digest(data);
            } catch (Exception e) {
                throw new RuntimeException("SHA-256 digest unavailable", e);
            }
        }
        return toHex(digest);
    }

    /* ---------- helpers ---------- */

    private static String nzfill(String s, int width) {
        // Normalize numeric string: trim, parse, zero-fill to width (drops leading spaces/zeros from input).
        return String.format("%0" + width + "d", Integer.parseInt(Objects.requireNonNull(s).trim()));
    }

    private static String zfill(String s, int width) {
        String v = Objects.requireNonNull(s).trim();
        // Keep existing leading zeros in string inputs; pad to width with leading zeros.
        if (v.length() >= width) return v;
        StringBuilder sb = new StringBuilder(width);
        for (int i = v.length(); i < width; i++) sb.append('0');
        sb.append(v);
        return sb.toString();
    }

    private static String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
