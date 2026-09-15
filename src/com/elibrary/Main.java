package com.elibrary;

import com.elibrary.exception.*;
import com.elibrary.model.*;
import com.elibrary.service.*;
import com.elibrary.util.AppLogger;
import com.elibrary.util.FileStorageService;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Command-line entry point for the E-Library Digital Content Subscription
 * System. Wires together the three core service modules (catalog,
 * subscription, access) and drives a simple text menu for both regular
 * users and an administrator account.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static FileStorageService storageService;
    private static CatalogService catalogService;
    private static SubscriptionService subscriptionService;
    private static AccessService accessService;
    private static ReportService reportService;

    public static void main(String[] args) {
        storageService = new FileStorageService();
        catalogService = new CatalogService(storageService);
        subscriptionService = new SubscriptionService(storageService);
        accessService = new AccessService(storageService);
        reportService = new ReportService(catalogService, subscriptionService, accessService);

        bootstrapAdminIfNeeded();
        seedSampleContentIfEmpty();

        AppLogger.info("Application started.");
        printBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleRegister();
                    break;
                case "2":
                    handleLogin();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        AppLogger.info("Application exiting.");
        System.out.println("Goodbye!");
    }

    // ---------------------------------------------------------------
    // Bootstrap
    // ---------------------------------------------------------------

    private static void bootstrapAdminIfNeeded() {
        if (subscriptionService.getAllUsers().stream().noneMatch(User::isAdmin)) {
            try {
                subscriptionService.registerAdmin("Administrator", "admin@elibrary.com", "admin123");
                AppLogger.info("Bootstrapped default admin account (admin@elibrary.com / admin123).");
            } catch (InvalidInputException | DuplicateUserException e) {
                AppLogger.error("Failed to bootstrap admin account: " + e.getMessage());
            }
        }
    }

    private static void seedSampleContentIfEmpty() {
        if (!catalogService.getAllContent().isEmpty()) {
            return;
        }
        try {
            catalogService.addBook("Clean Code", "Robert C. Martin", "Programming",
                    SubscriptionTier.FREE, 464, "978-0132350884");
            catalogService.addBook("Deep Work", "Cal Newport", "Self-Help",
                    SubscriptionTier.BASIC, 304, "978-1455586691");
            catalogService.addBook("Advanced Algorithms", "T. Cormen", "Computer Science",
                    SubscriptionTier.PREMIUM, 1312, "978-0262033848");
            catalogService.addCourse("Java Programming Basics", "Priya Sharma", "Programming",
                    SubscriptionTier.FREE, 6.5, 24);
            catalogService.addCourse("Data Structures in Java", "Priya Sharma", "Computer Science",
                    SubscriptionTier.BASIC, 12.0, 40);
            catalogService.addCourse("Machine Learning Masterclass", "Dr. Ana Ruiz", "Data Science",
                    SubscriptionTier.PREMIUM, 20.0, 65);

            // --- Electrical & Electronics Engineering (EEE) ---
            catalogService.addBook("Electrical Engineering Fundamentals", "V. Del Toro", "EEE",
                    SubscriptionTier.FREE, 528, "978-0132494701");
            catalogService.addCourse("Basic Electronics and Circuit Theory", "Rakesh Iyer", "EEE",
                    SubscriptionTier.BASIC, 10.0, 32);

            // --- English ---
            catalogService.addBook("English Grammar in Use", "Raymond Murphy", "English",
                    SubscriptionTier.FREE, 380, "978-1108457651");
            catalogService.addCourse("Business English Communication", "Emma Clarke", "English",
                    SubscriptionTier.FREE, 5.0, 18);

            // --- Digital Logic & Computer Architecture (DLCA) ---
            catalogService.addBook("Digital Logic and Computer Design", "M. Morris Mano", "DLCA",
                    SubscriptionTier.BASIC, 560, "978-0132774208");
            catalogService.addCourse("Computer Architecture Fundamentals", "Dr. Sanjay Rao", "DLCA",
                    SubscriptionTier.PREMIUM, 14.0, 45);

            // --- Calculus ---
            catalogService.addBook("Calculus: Early Transcendentals", "James Stewart", "Calculus",
                    SubscriptionTier.BASIC, 1368, "978-1285741550");
            catalogService.addCourse("Calculus I: Limits and Derivatives", "Dr. Meera Nair", "Calculus",
                    SubscriptionTier.FREE, 8.0, 28);

            // --- Python ---
            catalogService.addBook("Python Crash Course", "Eric Matthes", "Python",
                    SubscriptionTier.FREE, 544, "978-1593279288");
            catalogService.addCourse("Python for Everybody", "Dr. Charles Severance", "Python",
                    SubscriptionTier.BASIC, 15.0, 50);

            // --- C++ ---
            catalogService.addBook("The C++ Programming Language", "Bjarne Stroustrup", "C++",
                    SubscriptionTier.PREMIUM, 1376, "978-0321563842");
            catalogService.addCourse("C++ Programming Masterclass", "Arjun Mehta", "C++",
                    SubscriptionTier.BASIC, 18.0, 60);

            AppLogger.info("Seeded sample catalog content on first run.");
        } catch (InvalidInputException e) {
            AppLogger.error("Failed to seed sample content: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Top-level menu
    // ---------------------------------------------------------------

    private static void printBanner() {
        System.out.println("==================================================");
        System.out.println("   E-LIBRARY DIGITAL CONTENT SUBSCRIPTION SYSTEM   ");
        System.out.println("==================================================");
    }

    private static void printMainMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void handleRegister() {
        try {
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password (min 4 chars): ");
            String password = scanner.nextLine().trim();
            System.out.println("Choose starting tier: 1) FREE  2) BASIC  3) PREMIUM");
            System.out.print("Choice: ");
            SubscriptionTier tier = promptTierChoice();

            User user = subscriptionService.register(name, email, password, tier);
            System.out.println("Registration successful! Your user ID is " + user.getUserId());
        } catch (InvalidInputException | DuplicateUserException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Repeatedly prompts until the user enters exactly "1", "2", or "3".
     * Rejects any other input and asks again rather than silently
     * substituting a default tier.
     */
    private static SubscriptionTier promptTierChoice() {
        while (true) {
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": return SubscriptionTier.FREE;
                case "2": return SubscriptionTier.BASIC;
                case "3": return SubscriptionTier.PREMIUM;
                default:
                    System.out.print("Invalid choice \"" + choice + "\". Please enter 1, 2, or 3: ");
            }
        }
    }

    private static void handleLogin() {
        try {
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            User user = subscriptionService.authenticate(email, password);
            System.out.println("Welcome, " + user.getName() + "!");
            if (user.isAdmin()) {
                runAdminMenu(user);
            } else {
                runUserMenu(user);
            }
        } catch (UserNotFoundException | InvalidInputException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Regular user menu
    // ---------------------------------------------------------------

    private static void runUserMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- USER MENU (" + user.getName() + ") ---");
            System.out.println("1. Browse all content");
            System.out.println("2. Search content by title");
            System.out.println("3. Search content by category");
            System.out.println("4. Access/borrow content");
            System.out.println("5. View my subscription");
            System.out.println("6. Upgrade/downgrade subscription");
            System.out.println("7. Renew subscription");
            System.out.println("8. View my access history");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": browseAllContent(); break;
                case "2": searchByTitle(); break;
                case "3": searchByCategory(); break;
                case "4": accessContent(user); break;
                case "5": viewSubscription(user); break;
                case "6": changeSubscriptionTier(user); break;
                case "7": renewSubscription(user); break;
                case "8": viewAccessHistory(user); break;
                case "0": loggedIn = false; break;
                default: System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void browseAllContent() {
        List<Content> all = catalogService.getAllContent();
        if (all.isEmpty()) {
            System.out.println("No content available yet.");
            return;
        }
        System.out.println("\n--- CATALOG (" + all.size() + " items) ---");
        for (Content c : all) {
            System.out.println(c.getContentId() + " | " + c.getSummary()
                    + " | min tier: " + c.getMinimumTier());
        }
    }

    private static void searchByTitle() {
        System.out.print("Enter title keyword: ");
        String keyword = scanner.nextLine().trim();
        List<Content> results = catalogService.searchByTitle(keyword);
        printSearchResults(results);
    }

    private static void searchByCategory() {
        System.out.print("Enter category: ");
        String category = scanner.nextLine().trim();
        List<Content> results = catalogService.searchByCategory(category);
        printSearchResults(results);
    }

    private static void printSearchResults(List<Content> results) {
        if (results.isEmpty()) {
            System.out.println("No matching content found.");
            return;
        }
        for (Content c : results) {
            System.out.println(c.getContentId() + " | " + c.getSummary());
        }
    }

    private static void accessContent(User user) {
        System.out.print("Enter content ID to access: ");
        String contentId = scanner.nextLine().trim();
        try {
            Content content = catalogService.findById(contentId);
            accessService.accessContent(user, content);
            subscriptionService.persist();
            catalogService.persistCatalog();
            System.out.println("Access granted! Enjoy: " + content.getTitle());
            System.out.println("Remaining quota this month: "
                    + describeQuota(user.getSubscription().getRemainingQuota()));
        } catch (ContentNotFoundException | SubscriptionExpiredException
                 | AccessDeniedException | QuotaExceededException e) {
            System.out.println("Cannot access content: " + e.getMessage());
        }
    }

    private static String describeQuota(int quota) {
        return quota == Integer.MAX_VALUE ? "unlimited" : String.valueOf(quota);
    }

    private static void viewSubscription(User user) {
        Subscription s = user.getSubscription();
        System.out.println("\n--- YOUR SUBSCRIPTION ---");
        System.out.println("Tier: " + s.getTier() + " - " + s.getTier().getDescription());
        System.out.println("Started: " + s.getStartDate());
        System.out.println("Expires: " + s.getExpiryDate());
        System.out.println("Accesses used this month: " + s.getAccessesUsedThisMonth());
        System.out.println("Remaining quota: " + describeQuota(s.getRemainingQuota()));
    }

    private static void changeSubscriptionTier(User user) {
        System.out.println("Choose new tier: 1) FREE  2) BASIC  3) PREMIUM");
        System.out.print("Choice: ");
        SubscriptionTier newTier = promptTierChoice();
        subscriptionService.changeTier(user, newTier);
        System.out.println("Subscription updated to " + newTier + ". Billing cycle has been reset.");
    }

    private static void renewSubscription(User user) {
        subscriptionService.renew(user);
        System.out.println("Subscription renewed. New expiry date: "
                + user.getSubscription().getExpiryDate());
    }

    private static void viewAccessHistory(User user) {
        List<AccessRecord> history = accessService.getAccessHistoryForUser(user.getUserId());
        if (history.isEmpty()) {
            System.out.println("You haven't accessed any content yet.");
            return;
        }
        System.out.println("\n--- YOUR ACCESS HISTORY ---");
        for (AccessRecord record : history) {
            System.out.println(record);
        }
    }

    // ---------------------------------------------------------------
    // Admin menu
    // ---------------------------------------------------------------

    private static void runAdminMenu(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Add new book");
            System.out.println("2. Add new course");
            System.out.println("3. Remove content");
            System.out.println("4. View all content");
            System.out.println("5. View all users");
            System.out.println("6. View reports & analytics");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": addBook(); break;
                case "2": addCourse(); break;
                case "3": removeContent(); break;
                case "4": browseAllContent(); break;
                case "5": viewAllUsers(); break;
                case "6": viewReports(); break;
                case "0": loggedIn = false; break;
                default: System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void addBook() {
        try {
            System.out.print("Title: ");
            String title = scanner.nextLine().trim();
            System.out.print("Author: ");
            String author = scanner.nextLine().trim();
            System.out.print("Category: ");
            String category = scanner.nextLine().trim();
            System.out.println("Minimum tier required: 1) FREE  2) BASIC  3) PREMIUM");
            System.out.print("Choice: ");
            SubscriptionTier tier = promptTierChoice();
            System.out.print("Page count: ");
            int pages = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("ISBN: ");
            String isbn = scanner.nextLine().trim();

            Book book = catalogService.addBook(title, author, category, tier, pages, isbn);
            System.out.println("Book added with ID: " + book.getContentId());
        } catch (InvalidInputException e) {
            System.out.println("Failed to add book: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Page count must be a whole number.");
        }
    }

    private static void addCourse() {
        try {
            System.out.print("Title: ");
            String title = scanner.nextLine().trim();
            System.out.print("Author/Instructor: ");
            String author = scanner.nextLine().trim();
            System.out.print("Category: ");
            String category = scanner.nextLine().trim();
            System.out.println("Minimum tier required: 1) FREE  2) BASIC  3) PREMIUM");
            System.out.print("Choice: ");
            SubscriptionTier tier = promptTierChoice();
            System.out.print("Duration in hours: ");
            double hours = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Number of lectures: ");
            int lectures = Integer.parseInt(scanner.nextLine().trim());

            Course course = catalogService.addCourse(title, author, category, tier, hours, lectures);
            System.out.println("Course added with ID: " + course.getContentId());
        } catch (InvalidInputException e) {
            System.out.println("Failed to add course: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Duration/lecture count must be numeric.");
        }
    }

    private static void removeContent() {
        System.out.print("Enter content ID to remove: ");
        String id = scanner.nextLine().trim();
        boolean removed = catalogService.removeContent(id);
        System.out.println(removed ? "Content removed." : "No content found with that ID.");
    }

    private static void viewAllUsers() {
        List<User> users = subscriptionService.getAllUsers();
        System.out.println("\n--- ALL USERS (" + users.size() + ") ---");
        for (User u : users) {
            System.out.println(u.getUserId() + " | " + u
                    + (u.isAdmin() ? " [ADMIN]" : ""));
        }
    }

    private static void viewReports() {
        System.out.println("\n--- REPORTS & ANALYTICS ---");

        System.out.println("\nMost accessed content:");
        List<Content> top = reportService.getMostAccessedContent(5);
        if (top.isEmpty()) {
            System.out.println("  (no data yet)");
        } else {
            for (Content c : top) {
                System.out.println("  " + c.getAccessCount() + "x - " + c.getTitle());
            }
        }

        System.out.println("\nUsers by subscription tier:");
        Map<String, Long> byTier = reportService.getUserCountByTier();
        byTier.forEach((tier, count) -> System.out.println("  " + tier + ": " + count));

        System.out.println("\nMost active users:");
        List<User> activeUsers = reportService.getMostActiveUsers(5);
        for (User u : activeUsers) {
            System.out.println("  " + u.getAccessedContentIds().size() + " accesses - " + u.getName());
        }

        System.out.println("\nAccess count by category:");
        Map<String, Integer> byCategory = reportService.getAccessCountByCategory();
        byCategory.forEach((cat, count) -> System.out.println("  " + cat + ": " + count));

        System.out.println("\nTotal access events recorded: " + reportService.getTotalAccessCount());
    }
}
