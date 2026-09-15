package com.elibrary.exception;

/**
 * Thrown when a requested content ID does not exist in the catalog.
 */
public class ContentNotFoundException extends ELibraryException {
    public ContentNotFoundException(String message) {
        super(message);
    }
}
