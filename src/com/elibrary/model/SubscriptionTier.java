package com.elibrary.model;

/**
 * Defines the three subscription tiers and the perks associated with each:
 * a monthly borrow/access limit and whether premium-only content is unlocked.
 *
 * The natural ordering (FREE < BASIC < PREMIUM) is used to check whether a
 * user's tier is high enough to access a given piece of content.
 */
public enum SubscriptionTier {

    FREE(0, 5, false, "Access to free content only, up to 5 accesses/month"),
    BASIC(199, 30, false, "Access to free + basic content, up to 30 accesses/month"),
    PREMIUM(499, Integer.MAX_VALUE, true, "Unlimited access to all content including premium");

    private final double monthlyPriceInRupees;
    private final int monthlyAccessLimit;
    private final boolean premiumContentUnlocked;
    private final String description;

    SubscriptionTier(double monthlyPriceInRupees, int monthlyAccessLimit,
                      boolean premiumContentUnlocked, String description) {
        this.monthlyPriceInRupees = monthlyPriceInRupees;
        this.monthlyAccessLimit = monthlyAccessLimit;
        this.premiumContentUnlocked = premiumContentUnlocked;
        this.description = description;
    }

    public double getMonthlyPriceInRupees() {
        return monthlyPriceInRupees;
    }

    public int getMonthlyAccessLimit() {
        return monthlyAccessLimit;
    }

    public boolean isPremiumContentUnlocked() {
        return premiumContentUnlocked;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns true if this tier satisfies the given minimum tier
     * requirement (ordinal comparison: FREE < BASIC < PREMIUM).
     */
    public boolean meetsMinimum(SubscriptionTier required) {
        return this.ordinal() >= required.ordinal();
    }
}
