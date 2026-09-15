package com.elibrary.service;

import com.elibrary.exception.AccessDeniedException;
import com.elibrary.exception.QuotaExceededException;
import com.elibrary.exception.SubscriptionExpiredException;
import com.elibrary.model.AccessRecord;
import com.elibrary.model.Content;
import com.elibrary.model.Subscription;
import com.elibrary.model.User;
import com.elibrary.util.AppLogger;
import com.elibrary.util.FileStorageService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 3: Access Control & Usage Tracking.
 *
 * Enforces subscription-tier access rules whenever a user tries to
 * access a piece of content, records every successful access, and
 * persists the access log for later reporting.
 */
public class AccessService {

    private final List<AccessRecord> accessLog;
    private final FileStorageService storageService;

    public AccessService(FileStorageService storageService) {
        this.storageService = storageService;
        this.accessLog = storageService.loadAccessLog();
    }

    /**
     * Attempts to grant a user access to a piece of content, enforcing
     * (in order): subscription expiry, tier eligibility, and monthly quota.
     * On success, records the access against both the user, the content
     * item, and the persistent access log.
     */
    public void accessContent(User user, Content content)
            throws SubscriptionExpiredException, AccessDeniedException, QuotaExceededException {

        Subscription subscription = user.getSubscription();

        if (subscription.isExpired(LocalDate.now())) {
            AppLogger.error("Access denied (expired subscription): user=" + user.getUserId()
                    + " content=" + content.getContentId());
            throw new SubscriptionExpiredException(
                    "Your subscription expired on " + subscription.getExpiryDate()
                            + ". Please renew to continue accessing content.");
        }

        if (!subscription.getTier().meetsMinimum(content.getMinimumTier())) {
            AppLogger.error("Access denied (tier too low): user=" + user.getUserId()
                    + " content=" + content.getContentId());
            throw new AccessDeniedException(
                    "This content requires " + content.getMinimumTier()
                            + " tier or higher. Your current tier is " + subscription.getTier() + ".");
        }

        if (!subscription.hasRemainingQuota()) {
            AppLogger.error("Access denied (quota exceeded): user=" + user.getUserId()
                    + " content=" + content.getContentId());
            throw new QuotaExceededException(
                    "You have used all " + subscription.getTier().getMonthlyAccessLimit()
                            + " accesses allowed this month on the " + subscription.getTier() + " tier.");
        }

        subscription.incrementUsage();
        content.incrementAccessCount();
        user.recordAccess(content.getContentId());
        accessLog.add(new AccessRecord(user.getUserId(), content.getContentId(), LocalDateTime.now()));

        AppLogger.info("Access granted: user=" + user.getUserId()
                + " content=" + content.getContentId() + " remainingQuota=" + subscription.getRemainingQuota());

        persist();
    }

    public List<AccessRecord> getAccessLog() {
        return new ArrayList<>(accessLog);
    }

    public List<AccessRecord> getAccessHistoryForUser(String userId) {
        List<AccessRecord> results = new ArrayList<>();
        for (AccessRecord record : accessLog) {
            if (record.getUserId().equalsIgnoreCase(userId)) {
                results.add(record);
            }
        }
        return results;
    }

    private void persist() {
        storageService.saveAccessLog(accessLog);
    }
}
