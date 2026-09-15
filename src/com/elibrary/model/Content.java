package com.elibrary.model;

/**
 * Abstract base class representing any piece of content in the library.
 * Book and Course extend this class, demonstrating inheritance and
 * polymorphism across the catalog.
 */
public abstract class Content {

    private final String contentId;
    private String title;
    private String author;
    private String category;
    private SubscriptionTier minimumTier;
    private int accessCount;

    protected Content(String contentId, String title, String author,
                       String category, SubscriptionTier minimumTier) {
        this.contentId = contentId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.minimumTier = minimumTier;
        this.accessCount = 0;
    }

    public String getContentId() {
        return contentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public SubscriptionTier getMinimumTier() {
        return minimumTier;
    }

    public void setMinimumTier(SubscriptionTier minimumTier) {
        this.minimumTier = minimumTier;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public void incrementAccessCount() {
        this.accessCount++;
    }

    /**
     * Every content subtype must describe what kind of content it is
     * (e.g. "Book", "Course"). Used for reporting and JSON persistence.
     */
    public abstract String getContentType();

    /**
     * Every content subtype must provide a short summary line used
     * when listing content in the CLI menus.
     */
    public abstract String getSummary();

    @Override
    public String toString() {
        return String.format("[%s] %s by %s (%s) - min tier: %s, accessed %d time(s)",
                getContentType(), title, author, category, minimumTier, accessCount);
    }
}
