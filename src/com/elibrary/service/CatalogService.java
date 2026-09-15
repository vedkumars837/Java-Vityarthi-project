package com.elibrary.service;

import com.elibrary.exception.ContentNotFoundException;
import com.elibrary.exception.InvalidInputException;
import com.elibrary.model.Book;
import com.elibrary.model.Content;
import com.elibrary.model.Course;
import com.elibrary.model.SubscriptionTier;
import com.elibrary.util.AppLogger;
import com.elibrary.util.FileStorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Module 1: Content Catalog Management.
 *
 * Responsible for adding, searching, listing and removing content
 * (books and courses) in the library catalog. Persists changes via
 * FileStorageService.
 */
public class CatalogService {

    private final List<Content> catalog;
    private final FileStorageService storageService;

    public CatalogService(FileStorageService storageService) {
        this.storageService = storageService;
        this.catalog = storageService.loadContent();
    }

    public Book addBook(String title, String author, String category,
                         SubscriptionTier minimumTier, int pageCount, String isbn)
            throws InvalidInputException {
        validateCommonFields(title, author, category);
        if (pageCount <= 0) {
            throw new InvalidInputException("Page count must be greater than zero.");
        }
        String id = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Book book = new Book(id, title, author, category, minimumTier, pageCount, isbn);
        catalog.add(book);
        persist();
        AppLogger.info("Added book: " + id + " - " + title);
        return book;
    }

    public Course addCourse(String title, String author, String category,
                             SubscriptionTier minimumTier, double durationHours, int numberOfLectures)
            throws InvalidInputException {
        validateCommonFields(title, author, category);
        if (durationHours <= 0 || numberOfLectures <= 0) {
            throw new InvalidInputException("Duration and lecture count must be greater than zero.");
        }
        String id = "CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Course course = new Course(id, title, author, category, minimumTier, durationHours, numberOfLectures);
        catalog.add(course);
        persist();
        AppLogger.info("Added course: " + id + " - " + title);
        return course;
    }

    private void validateCommonFields(String title, String author, String category)
            throws InvalidInputException {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidInputException("Title cannot be empty.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new InvalidInputException("Author cannot be empty.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidInputException("Category cannot be empty.");
        }
    }

    public Content findById(String contentId) throws ContentNotFoundException {
        for (Content c : catalog) {
            if (c.getContentId().equalsIgnoreCase(contentId)) {
                return c;
            }
        }
        throw new ContentNotFoundException("No content found with ID: " + contentId);
    }

    public List<Content> searchByTitle(String keyword) {
        List<Content> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (Content c : catalog) {
            if (c.getTitle().toLowerCase().contains(lowerKeyword)) {
                results.add(c);
            }
        }
        return results;
    }

    public List<Content> searchByCategory(String category) {
        List<Content> results = new ArrayList<>();
        for (Content c : catalog) {
            if (c.getCategory().equalsIgnoreCase(category)) {
                results.add(c);
            }
        }
        return results;
    }

    public List<Content> getAllContent() {
        return new ArrayList<>(catalog);
    }

    public boolean removeContent(String contentId) {
        boolean removed = catalog.removeIf(c -> c.getContentId().equalsIgnoreCase(contentId));
        if (removed) {
            persist();
            AppLogger.info("Removed content: " + contentId);
        }
        return removed;
    }

    private void persist() {
        storageService.saveContent(catalog);
    }

    /**
     * Public persist hook used after an external mutation to a Content
     * object's state (e.g. AccessService incrementing its access count),
     * so the catalog file on disk stays in sync.
     */
    public void persistCatalog() {
        persist();
    }
}
