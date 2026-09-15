package com.elibrary.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registered user of the platform, holding their credentials,
 * subscription details, and a list of content IDs they have accessed.
 */
public class User {

    private final String userId;
    private String name;
    private String email;
    private String passwordHash;
    private Subscription subscription;
    private final List<String> accessedContentIds;
    private boolean admin;

    public User(String userId, String name, String email, String passwordHash,
                Subscription subscription) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.subscription = subscription;
        this.accessedContentIds = new ArrayList<>();
        this.admin = false;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public List<String> getAccessedContentIds() {
        return accessedContentIds;
    }

    public void recordAccess(String contentId) {
        accessedContentIds.add(contentId);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s tier - %d items accessed",
                name, email, subscription.getTier(), accessedContentIds.size());
    }
}
