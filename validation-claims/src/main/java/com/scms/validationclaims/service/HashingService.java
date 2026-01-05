
package com.scms.validationclaims.service;

import org.bouncycastle.jcajce.provider.digest.Blake2b;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * HashingService builds the ticket serial and hashes it using either:
 *   1 = BLAKE2b-512, 2 = SHA-256 (default if unknown).
 *
 * Serial format (concatenation, no delimiters):
 *   YY(2) + GGG(3) + BB(2) + PPPPPPP(7) + TTT(3)
 * Example:
 *   customer_id=11, game_id=101, batch_id=01, pack_id=0000123, ticket_id=007
 *   serial = "11" + "101" + "01" + "0000123" + "007" = "11101010000123007"
 */
@Service
public class HashingService {

    public String buildSerial(String customerId,
                              String gameId,
                              String batchId,
                              String packId,
                              String ticketId) {

        final String yy      = nzfillDigits(customerId, 2);      // 2 digits
        final String ggg     = nzfillDigits(gameId, 3);          // 3 digits
        final String bb      = nzfillDigits(batchId, 2);         // 2 digits
        final String ppppppp = zfillDigits(nullToEmpty(packId), 7);  // 7 digits
        final String ttt     = zfillDigits(Objects.requireNonNull(ticketId), 3); // 3 digits

        return yy + ggg + bb + ppppppp + ttt;
    }

    public String hash(int alg, String serial) {
        byte[] data = serial.getBytes(StandardCharsets.UTF_8);
        byte[] digest;

        if (alg == 1) { // BLAKE2b-512 -> 128 hex
            digest = new Blake2b.Blake2b512().digest(data);
        } else {        // 2 = SHA-256 (default)
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                digest = md.digest(data);
            } catch (Exception e) {
                throw new RuntimeException("SHA-256 digest unavailable", e);
            }
        }
        return toHexLower(digest);
    }

    public String computeTicketHash(int alg,
                                    String customerId,
                                    String gameId,
                                    String batchId,
                                    String packId,
                                    String ticketId) {
        String serial = buildSerial(customerId, gameId, batchId, packId, ticketId);
        return hash(alg, serial);
    }


    private static String nzfillDigits(String s, int width) {
        String v = Objects.requireNonNull(s).trim();
        if (!v.matches("\\d+")) {
            throw new IllegalArgumentException("Expected digits only, got: '" + v + "'");
        }

        int n = Integer.parseInt(v);
        return String.format("%0" + width + "d", n);
    }

    private static String zfillDigits(String s, int width) {
        String v = Objects.requireNonNull(s).trim();
        if (!v.matches("\\d*")) {
            throw new IllegalArgumentException("Expected digits only, got: '" + v + "'");
        }
        if (v.length() > width) {
            throw new IllegalArgumentException("Value '" + v + "' longer than width " + width);
        }
        if (v.length() == width) return v;
        StringBuilder sb = new StringBuilder(width);
        for (int i = v.length(); i < width; i++) sb.append('0');
        sb.append(v);
        return sb.toString();
    }

    private static String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }

    private static String toHexLower(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
