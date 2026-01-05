package com.scms.validation_claims.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashingServiceTests {

    private final HashingService hashing = new HashingService();

    @Test
    @DisplayName("Serial is built with correct zero-fill widths")
    void serialZeroFill() {
        String serial = hashing.buildSerial("11", "101", "1", "123", "7");
        // YY=11, GGG=101, BB=01, PPPPPPP=0000123, TTT=007
        assertEquals("11101010000123007", serial);
    }

    @Test
    @DisplayName("Pack/Ticket zfill preserves leading zeros and pads to width")
    void zfillStringFields() {
        String s1 = hashing.buildSerial("11", "101", "1", "0000123", "007");
        assertEquals("11101010000123007", s1);

        String s2 = hashing.buildSerial("11", "101", "1", "", "7");
        assertEquals("11101010000000007", s2);
    }

    @Test
    @DisplayName("BLAKE2b vs SHA-256 produce different digests for same serial")
    void differentAlgorithms() {
        String serial = hashing.buildSerial("11", "101", "01", "0000123", "007");

        String blake = hashing.hash(1, serial);
        String sha   = hashing.hash(2, serial);

        assertNotNull(blake);
        assertNotNull(sha);
        assertEquals(128, blake.length(), "BLAKE2b-512 hex length must be 128");
        assertEquals(64, sha.length(),   "SHA-256 hex length must be 64");
        assertNotEquals(blake, sha, "Different algorithms should yield different digests");
    }

    @Test
    @DisplayName("Hash is deterministic: same input -> same hex output")
    void deterministicHash() {
        String serial = hashing.buildSerial("22", "201", "01", "0000000", "123");
        String blake1 = hashing.hash(1, serial);
        String blake2 = hashing.hash(1, serial);
        String sha1   = hashing.hash(2, serial);
        String sha2   = hashing.hash(2, serial);

        assertEquals(blake1, blake2);
        assertEquals(sha1, sha2);
    }
}
