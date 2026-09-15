package com.elibrary.service;

import com.elibrary.model.AccessRecord;
import com.elibrary.model.Content;
import com.elibrary.model.User;

import java.util.*;

/**
 * Generates cross-cutting usage reports by pulling data from the
 * catalog, subscription, and access services. Kept separate from the
 * three core modules since reporting reads from all of them but owns
 * none of the underlying data.
 */
public class ReportService {

    private final CatalogService catalogService;
    private final SubscriptionService subscriptionService;
    private final AccessService accessService;

    public ReportService(CatalogService catalogService,
                          SubscriptionService subscriptionService,
                          AccessService accessService) {
        this.catalogService = catalogService;
        this.subscriptionService = subscriptionService;
        this.accessService = accessService;
    }

    /**
     * Returns content sorted by access count, descending - the
     * "most popular content" report.
     */
    public List<Content> getMostAccessedContent(int limit) {
        List<Content> all = catalogService.getAllContent();
        all.sort((a, b) -> Integer.compare(b.getAccessCount(), a.getAccessCount()));
        return all.subList(0, Math.min(limit, all.size()));
    }

    /**
     * Counts how many users currently sit in each subscription tier.
     */
    public Map<String, Long> getUserCountByTier() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (User u : subscriptionService.getAllUsers()) {
            String tier = u.getSubscription().getTier().name();
            counts.merge(tier, 1L, Long::sum);
        }
        return counts;
    }

    /**
     * Total number of access events recorded across the whole platform.
     */
    public int getTotalAccessCount() {
        return accessService.getAccessLog().size();
    }

    /**
     * Ranks users by how many times they have accessed content -
     * "most active users" report.
     */
    public List<User> getMostActiveUsers(int limit) {
        List<User> all = subscriptionService.getAllUsers();
        all.sort((a, b) -> Integer.compare(
                b.getAccessedContentIds().size(), a.getAccessedContentIds().size()));
        return all.subList(0, Math.min(limit, all.size()));
    }

    /**
     * Breaks down total accesses by content category (e.g. "Fiction",
     * "Programming") to show which subject areas are most popular.
     */
    public Map<String, Integer> getAccessCountByCategory() {
        Map<String, Integer> counts = new TreeMap<>();
        for (Content c : catalogService.getAllContent()) {
            counts.merge(c.getCategory(), c.getAccessCount(), Integer::sum);
        }
        return counts;
    }

    public List<AccessRecord> getRecentAccessEvents(int limit) {
        List<AccessRecord> log = accessService.getAccessLog();
        List<AccessRecord> reversed = new ArrayList<>(log);
        Collections.reverse(reversed);
        return reversed.subList(0, Math.min(limit, reversed.size()));
    }
}
