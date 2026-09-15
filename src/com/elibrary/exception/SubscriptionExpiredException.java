package com.elibrary.exception;

/**
 * Thrown when a user's subscription has passed its expiry date and
 * they attempt to access content or perform a subscription action.
 */
public class SubscriptionExpiredException extends ELibraryException {
    public SubscriptionExpiredException(String message) {
        super(message);
    }
}
