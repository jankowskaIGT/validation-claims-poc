
package com.scms.validationclaims.service;

import org.bouncycastle.jcajce.provider.digest.Blake2b;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * HashingService buduje numer seryjny i hashuje go.
 *
 * Serial format (konkatenacja, bez separatorów):
 *   YY(2) + GGG(3) + BB(2) + PPPPPPP(7) + TTT(3)
 * Example:
 *   customer_id=11, game_id=101, batch_id=01, pack_id=0000123, ticket_id=007
 *   serial = "11" + "101" + "01" + "0000123" + "007" = "11101010000123007"
 */
@Service
public class HashingService {

    /**
     *  - 101 -> BLAKE2b-512
     *  - 202 -> SHA-256
     *  - other -> exception
     */
    public String computeTicketHash(String customerId,
                                    String gameId,
                                    String batchId,
                                    String packId,
                                    String ticketId) {
        String serial = buildSerial(customerId, gameId, batchId, packId, ticketId);
        int resolvedAlg = chooseAlgByGameId(gameId);
        return hash(resolvedAlg, serial);
    }

    @Deprecated
    public String computeTicketHash(int alg,
                                    String customerId,
                                    String gameId,
                                    String batchId,
                                    String packId,
                                    String ticketId) {
        return computeTicketHash(customerId, gameId, batchId, packId, ticketId);
    }

    public String buildSerial(String customerId,
                              String gameId,
                              String batchId,
                              String packId,
                              String ticketId) {

        final String yy      = nzfillDigits(requireNonNullWithMsg(customerId, "No customerId"), 2);      // 2 digits
        final String ggg     = nzfillDigits(requireNonNullWithMsg(gameId,     "No gameId"), 3); // 3 digits
        final String bb      = nzfillDigits(requireNonNullWithMsg(batchId,    "No batchId"), 2);         // 2 digits
        final String ppppppp = zfillDigits(nullToEmpty(packId), 7);                                        // 7 digits
        final String ttt     = zfillDigits(requireNonNullWithMsg(ticketId,   "No ticketId"), 3);         // 3 digits

        return yy + ggg + bb + ppppppp + ttt;
    }


    public String hash(int alg, String serial) {
        byte[] data = serial.getBytes(StandardCharsets.UTF_8);
        byte[] digest;

        switch (alg) {
            case 1: // BLAKE2b-512 -> 128 hex
                digest = new Blake2b.Blake2b512().digest(data);
                break;
            case 2: // SHA-256 -> 64 hex
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    digest = md.digest(data);
                } catch (Exception e) {
                    throw new RuntimeException("SHA-256 digest unavailable", e);
                }
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown algorithm: " + alg + " (permitted: 1=BLAKE2b-512, 2=SHA-256)"
                );
        }
        return toHexLower(digest);
    }

    /**
     * 101 -> 1 (BLAKE2b-512)
     * 202 -> 2 (SHA-256)
     * Other -> exception
     */
    private static int chooseAlgByGameId(String gameId) {
        String g = Objects.requireNonNull(gameId, "No gameId").trim();
        if (!g.matches("\\d+")) {
            throw new IllegalArgumentException("gameId has to be numeric, received: '" + g + "'");
        }
        switch (g) {
            case "101":
                return 1; // BLAKE2b-512
            case "202":
                return 2; // SHA-256
            default:
                throw new IllegalArgumentException(
                        "Not supported gameId: " + g + " (permitted: 101=BLAKE2b-512, 202=SHA-256)"
                );
        }
    }


    private static String requireNonNullWithMsg(String s, String message) {
        if (s == null) throw new IllegalArgumentException(message);
        return s;
    }

    private static String nzfillDigits(String s, int width) {
        String v = Objects.requireNonNull(s, "Value can not be null").trim();
        if (!v.matches("\\d+")) {
            throw new IllegalArgumentException("Only numbers expected (" + width + " numbers), received: '" + v + "'");
        }
        int n = Integer.parseInt(v);
        String out = String.format("%0" + width + "d", n);
        if (out.length() > width) {
            throw new IllegalArgumentException("Value '" + v + "' does not fit in width " + width);
        }
        return out;
    }

    private static String zfillDigits(String s, int width) {
        String v = Objects.requireNonNull(s, "Value can not be null").trim();
        if (!v.matches("\\d*")) {
            throw new IllegalArgumentException("Only numbers expected, received: '" + v + "'");
        }
        if (v.length() > width) {
            throw new IllegalArgumentException("Value '" + v + "' longer than the permissible width " + width);
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
