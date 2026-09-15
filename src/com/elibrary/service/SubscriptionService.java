package com.elibrary.service;

import com.elibrary.exception.DuplicateUserException;
import com.elibrary.exception.InvalidInputException;
import com.elibrary.exception.UserNotFoundException;
import com.elibrary.model.Subscription;
import com.elibrary.model.SubscriptionTier;
import com.elibrary.model.User;
import com.elibrary.util.AppLogger;
import com.elibrary.util.FileStorageService;
import com.elibrary.util.PasswordUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Module 2: User & Subscription Management.
 *
 * Responsible for registering users, authenticating them, and handling
 * subscription tier upgrades/downgrades and expiry/renewal logic.
 */
public class SubscriptionService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?"
                    + "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}$");
    private static final int SUBSCRIPTION_PERIOD_DAYS = 30;

    private final List<User> users;
    private final FileStorageService storageService;

    public SubscriptionService(FileStorageService storageService) {
        this.storageService = storageService;
        this.users = storageService.loadUsers();
    }

    public User register(String name, String email, String password, SubscriptionTier initialTier)
            throws InvalidInputException, DuplicateUserException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Name cannot be empty.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidInputException("Invalid email format: " + email);
        }
        if (password == null || password.length() < 4) {
            throw new InvalidInputException("Password must be at least 4 characters long.");
        }
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                throw new DuplicateUserException("A user with email " + email + " already exists.");
            }
        }

        String userId = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDate today = LocalDate.now();
        Subscription subscription = new Subscription(
                initialTier, today, today.plusDays(SUBSCRIPTION_PERIOD_DAYS));
        User user = new User(userId, name, email, PasswordUtil.hash(password), subscription);
        users.add(user);
        persist();
        AppLogger.info("Registered new user: " + userId + " (" + email + ") tier=" + initialTier);
        return user;
    }

    /**
     * Used only during first-run bootstrap to create the default
     * administrator account (see Main.bootstrapAdminIfNeeded).
     */
    public User registerAdmin(String name, String email, String password)
            throws InvalidInputException, DuplicateUserException {
        User admin = register(name, email, password, SubscriptionTier.PREMIUM);
        admin.setAdmin(true);
        persist();
        return admin;
    }

    public User authenticate(String email, String password) throws UserNotFoundException, InvalidInputException {
        User user = findByEmail(email);
        if (!PasswordUtil.matches(password, user.getPasswordHash())) {
            throw new InvalidInputException("Incorrect password.");
        }
        return user;
    }

    public User findByEmail(String email) throws UserNotFoundException {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        throw new UserNotFoundException("No user found with email: " + email);
    }

    public User findById(String userId) throws UserNotFoundException {
        for (User u : users) {
            if (u.getUserId().equalsIgnoreCase(userId)) {
                return u;
            }
        }
        throw new UserNotFoundException("No user found with ID: " + userId);
    }

    /**
     * Changes a user's subscription tier and resets their billing cycle,
     * used both for upgrades (Free -> Basic -> Premium) and downgrades.
     */
    public void changeTier(User user, SubscriptionTier newTier) {
        Subscription subscription = user.getSubscription();
        subscription.setTier(newTier);
        LocalDate today = LocalDate.now();
        subscription.setStartDate(today);
        subscription.setExpiryDate(today.plusDays(SUBSCRIPTION_PERIOD_DAYS));
        persist();
        AppLogger.info("User " + user.getUserId() + " changed tier to " + newTier);
    }

    /**
     * Renews the current subscription for another billing cycle
     * without changing tier, and resets the monthly usage counter.
     */
    public void renew(User user) {
        Subscription subscription = user.getSubscription();
        LocalDate today = LocalDate.now();
        subscription.setStartDate(today);
        subscription.setExpiryDate(today.plusDays(SUBSCRIPTION_PERIOD_DAYS));
        subscription.resetMonthlyUsage();
        persist();
        AppLogger.info("User " + user.getUserId() + " renewed subscription (" + subscription.getTier() + ")");
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public void persist() {
        storageService.saveUsers(users);
    }
}
