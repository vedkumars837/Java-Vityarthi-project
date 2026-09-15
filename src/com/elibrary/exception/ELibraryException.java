package com.elibrary.exception;

/**
 * Base checked exception for all domain-specific errors in the application.
 * Keeping a common base lets calling code catch broadly or specifically.
 */
public class ELibraryException extends Exception {
    public ELibraryException(String message) {
        super(message);
    }
}
