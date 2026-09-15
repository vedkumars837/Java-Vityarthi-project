package com.elibrary.exception;

/**
 * Thrown when a user has used up their monthly access quota for
 * their current subscription tier.
 */
public class QuotaExceededException extends ELibraryException {
    public QuotaExceededException(String message) {
        super(message);
    }
}
