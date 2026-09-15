package com.elibrary.model;

import java.time.LocalDate;

/**
 * Tracks a user's current subscription tier, when it started/expires,
 * and how many accesses they have used in the current billing month.
 */
public class Subscription {

    private SubscriptionTier tier;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private int accessesUsedThisMonth;

    public Subscription(SubscriptionTier tier, LocalDate startDate, LocalDate expiryDate) {
        this.tier = tier;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.accessesUsedThisMonth = 0;
    }

    public SubscriptionTier getTier() {
        return tier;
    }

    public void setTier(SubscriptionTier tier) {
        this.tier = tier;
        // Changing tier resets the monthly usage counter as a fresh start.
        this.accessesUsedThisMonth = 0;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getAccessesUsedThisMonth() {
        return accessesUsedThisMonth;
    }

    public void incrementUsage() {
        this.accessesUsedThisMonth++;
    }

    public void resetMonthlyUsage() {
        this.accessesUsedThisMonth = 0;
    }

    public boolean isExpired(LocalDate today) {
        return today.isAfter(expiryDate);
    }

    public boolean hasRemainingQuota() {
        return accessesUsedThisMonth < tier.getMonthlyAccessLimit();
    }

    public int getRemainingQuota() {
        if (tier.getMonthlyAccessLimit() == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, tier.getMonthlyAccessLimit() - accessesUsedThisMonth);
    }
}
