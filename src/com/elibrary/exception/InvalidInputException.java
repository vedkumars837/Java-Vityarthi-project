package com.elibrary.exception;

/**
 * Thrown when user-provided input fails validation
 * (empty fields, malformed email, negative numbers, etc.).
 */
public class InvalidInputException extends ELibraryException {
    public InvalidInputException(String message) {
        super(message);
    }
}
