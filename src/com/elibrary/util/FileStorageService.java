package com.elibrary.util;

import com.elibrary.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles reading and writing the application's data to JSON files on
 * disk (data/users.json, data/content.json, data/access_log.json).
 *
 * This is the single place in the codebase that knows how domain
 * objects map to/from JSON, keeping the rest of the service layer free
 * of persistence concerns.
 */
public class FileStorageService {

    private static final String DATA_DIR = "data";
    private static final Path USERS_FILE = Paths.get(DATA_DIR, "users.json");
    private static final Path CONTENT_FILE = Paths.get(DATA_DIR, "content.json");
    private static final Path ACCESS_LOG_FILE = Paths.get(DATA_DIR, "access_log.json");

    public FileStorageService() {
        ensureDataDirectoryExists();
    }

    private void ensureDataDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            AppLogger.error("Could not create data directory: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Users
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        String content = readFileOrEmpty(USERS_FILE);
        if (content.isEmpty()) {
            return users;
        }
        Object parsed = JsonUtil.parse(content);
        List<Object> array = (List<Object>) parsed;
        for (Object item : array) {
            Map<String, Object> obj = (Map<String, Object>) item;
            String userId = (String) obj.get("userId");
            String name = (String) obj.get("name");
            String email = (String) obj.get("email");
            String passwordHash = (String) obj.get("passwordHash");

            Map<String, Object> subObj = (Map<String, Object>) obj.get("subscription");
            SubscriptionTier tier = SubscriptionTier.valueOf((String) subObj.get("tier"));
            LocalDate startDate = LocalDate.parse((String) subObj.get("startDate"));
            LocalDate expiryDate = LocalDate.parse((String) subObj.get("expiryDate"));
            Subscription subscription = new Subscription(tier, startDate, expiryDate);
            double usedThisMonth = (Double) subObj.get("accessesUsedThisMonth");
            for (int i = 0; i < usedThisMonth; i++) {
                subscription.incrementUsage();
            }

            User user = new User(userId, name, email, passwordHash, subscription);
            Object adminFlag = obj.get("admin");
            if (adminFlag != null) {
                user.setAdmin((Boolean) adminFlag);
            }
            List<Object> accessed = (List<Object>) obj.get("accessedContentIds");
            if (accessed != null) {
                for (Object cid : accessed) {
                    user.recordAccess((String) cid);
                }
            }
            users.add(user);
        }
        return users;
    }

    public void saveUsers(List<User> users) {
        List<Object> array = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("userId", user.getUserId());
            obj.put("name", user.getName());
            obj.put("email", user.getEmail());
            obj.put("passwordHash", user.getPasswordHash());
            obj.put("admin", user.isAdmin());

            Map<String, Object> subObj = new LinkedHashMap<>();
            Subscription sub = user.getSubscription();
            subObj.put("tier", sub.getTier().name());
            subObj.put("startDate", sub.getStartDate().toString());
            subObj.put("expiryDate", sub.getExpiryDate().toString());
            subObj.put("accessesUsedThisMonth", (double) sub.getAccessesUsedThisMonth());
            obj.put("subscription", subObj);

            obj.put("accessedContentIds", new ArrayList<Object>(user.getAccessedContentIds()));
            array.add(obj);
        }
        writeFile(USERS_FILE, JsonUtil.write(array));
    }

    // ---------------------------------------------------------------
    // Content
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<Content> loadContent() {
        List<Content> contentList = new ArrayList<>();
        String raw = readFileOrEmpty(CONTENT_FILE);
        if (raw.isEmpty()) {
            return contentList;
        }
        Object parsed = JsonUtil.parse(raw);
        List<Object> array = (List<Object>) parsed;
        for (Object item : array) {
            Map<String, Object> obj = (Map<String, Object>) item;
            String type = (String) obj.get("contentType");
            String contentId = (String) obj.get("contentId");
            String title = (String) obj.get("title");
            String author = (String) obj.get("author");
            String category = (String) obj.get("category");
            SubscriptionTier minTier = SubscriptionTier.valueOf((String) obj.get("minimumTier"));
            double accessCountD = (Double) obj.get("accessCount");

            Content content;
            if ("Book".equals(type)) {
                int pageCount = (int) (double) (Double) obj.get("pageCount");
                String isbn = (String) obj.get("isbn");
                content = new Book(contentId, title, author, category, minTier, pageCount, isbn);
            } else {
                double durationHours = (Double) obj.get("durationHours");
                int lectures = (int) (double) (Double) obj.get("numberOfLectures");
                content = new Course(contentId, title, author, category, minTier, durationHours, lectures);
            }
            for (int i = 0; i < accessCountD; i++) {
                content.incrementAccessCount();
            }
            contentList.add(content);
        }
        return contentList;
    }

    public void saveContent(List<Content> contentList) {
        List<Object> array = new ArrayList<>();
        for (Content content : contentList) {
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("contentType", content.getContentType());
            obj.put("contentId", content.getContentId());
            obj.put("title", content.getTitle());
            obj.put("author", content.getAuthor());
            obj.put("category", content.getCategory());
            obj.put("minimumTier", content.getMinimumTier().name());
            obj.put("accessCount", (double) content.getAccessCount());

            if (content instanceof Book) {
                Book book = (Book) content;
                obj.put("pageCount", (double) book.getPageCount());
                obj.put("isbn", book.getIsbn());
            } else if (content instanceof Course) {
                Course course = (Course) content;
                obj.put("durationHours", course.getDurationHours());
                obj.put("numberOfLectures", (double) course.getNumberOfLectures());
            }
            array.add(obj);
        }
        writeFile(CONTENT_FILE, JsonUtil.write(array));
    }

    // ---------------------------------------------------------------
    // Access log
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<AccessRecord> loadAccessLog() {
        List<AccessRecord> records = new ArrayList<>();
        String raw = readFileOrEmpty(ACCESS_LOG_FILE);
        if (raw.isEmpty()) {
            return records;
        }
        Object parsed = JsonUtil.parse(raw);
        List<Object> array = (List<Object>) parsed;
        for (Object item : array) {
            Map<String, Object> obj = (Map<String, Object>) item;
            String userId = (String) obj.get("userId");
            String contentId = (String) obj.get("contentId");
            LocalDateTime timestamp = LocalDateTime.parse((String) obj.get("timestamp"));
            records.add(new AccessRecord(userId, contentId, timestamp));
        }
        return records;
    }

    public void saveAccessLog(List<AccessRecord> records) {
        List<Object> array = new ArrayList<>();
        for (AccessRecord record : records) {
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("userId", record.getUserId());
            obj.put("contentId", record.getContentId());
            obj.put("timestamp", record.getTimestamp().toString());
            array.add(obj);
        }
        writeFile(ACCESS_LOG_FILE, JsonUtil.write(array));
    }

    // ---------------------------------------------------------------
    // Low-level file helpers
    // ---------------------------------------------------------------

    private String readFileOrEmpty(Path path) {
        try {
            if (!Files.exists(path)) {
                return "";
            }
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
            return content.isEmpty() ? "" : content;
        } catch (IOException e) {
            AppLogger.error("Failed to read " + path + ": " + e.getMessage());
            return "";
        }
    }

    private void writeFile(Path path, String content) {
        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            AppLogger.error("Failed to write " + path + ": " + e.getMessage());
        }
    }
}
