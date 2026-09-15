package com.elibrary.exception;

/**
 * Thrown when a requested user ID/email does not exist in the system.
 */
public class UserNotFoundException extends ELibraryException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
