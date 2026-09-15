package com.elibrary.exception;

/**
 * Thrown when a user's subscription tier is not high enough to access
 * a given piece of content.
 */
public class AccessDeniedException extends ELibraryException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
