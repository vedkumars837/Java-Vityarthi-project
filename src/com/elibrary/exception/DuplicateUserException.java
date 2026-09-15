package com.elibrary.exception;

/**
 * Thrown when attempting to register a user with an email that is
 * already registered in the system.
 */
public class DuplicateUserException extends ELibraryException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
