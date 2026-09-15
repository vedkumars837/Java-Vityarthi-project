package com.elibrary.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Utility for hashing passwords with SHA-256 so plaintext passwords are
 * never stored on disk. This is a basic security measure appropriate
 * for a coursework project (not a production-grade credential store).
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // utility class - no instances
    }

    public static String hash(String plainTextPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainTextPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every standard JVM,
            // so this branch should never execute in practice.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean matches(String plainTextPassword, String storedHash) {
        return hash(plainTextPassword).equals(storedHash);
    }
}
