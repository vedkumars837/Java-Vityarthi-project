package com.elibrary.model;

/**
 * Represents an e-book in the catalog.
 */
public class Book extends Content {

    private int pageCount;
    private String isbn;

    public Book(String contentId, String title, String author, String category,
                SubscriptionTier minimumTier, int pageCount, String isbn) {
        super(contentId, title, author, category, minimumTier);
        this.pageCount = pageCount;
        this.isbn = isbn;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public String getContentType() {
        return "Book";
    }

    @Override
    public String getSummary() {
        return String.format("Book: \"%s\" - %d pages - ISBN %s", getTitle(), pageCount, isbn);
    }
}
