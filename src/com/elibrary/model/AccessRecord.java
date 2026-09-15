package com.elibrary.model;

import java.time.LocalDateTime;

/**
 * Represents a single access/borrow event: which user accessed which
 * content, and when. Used to build usage reports.
 */
public class AccessRecord {

    private final String userId;
    private final String contentId;
    private final LocalDateTime timestamp;

    public AccessRecord(String userId, String contentId, LocalDateTime timestamp) {
        this.userId = userId;
        this.contentId = contentId;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getContentId() {
        return contentId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] User %s accessed content %s", timestamp, userId, contentId);
    }
}
