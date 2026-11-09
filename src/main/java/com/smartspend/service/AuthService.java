package com.smartspend.service;

import com.smartspend.dao.UserDao;
import com.smartspend.model.User;
import com.smartspend.util.SessionManager;

import java.util.regex.Pattern;

/**
 * Service class handling user authentication, registration, and validation.
 * Demonstrates:
 * 1. Business Logic Layer separation
 * 2. Regular Expressions and Input Validation
 * 3. Session Management
 */
public class AuthService {

    private final UserDao userDao;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public AuthService() {
        this.userDao = new UserDao();
    }

    // Overloaded constructor for dependency injection (testing)
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Validates and registers a new user.
     * @param name User's full name
     * @param email User's email address
     * @param password Password (min 6 characters)
     * @param confirmPassword Must match password
     * @return Registered User object if successful
     * @throws IllegalArgumentException If validation fails
     */
    public User register(String name, String email, String password, String confirmPassword) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format (e.g. user@example.com).");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (userDao.isEmailTaken(email)) {
            throw new IllegalArgumentException("This email address is already registered.");
        }

        User user = new User(name.trim(), email.trim().toLowerCase(), password);
        boolean saved = userDao.save(user);
        if (saved) {
            System.out.println("[AuthService] Registered new user: " + user.getEmail());
            return user;
        } else {
            throw new RuntimeException("Failed to save user account.");
        }
    }

    /**
     * Authenticates user credentials and starts a session.
     * @param email User email
     * @param password User password
     * @return Logged in User
     * @throws IllegalArgumentException If credentials are invalid
     */
    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter your email.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Please enter your password.");
        }

        User user = userDao.authenticate(email, password);
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password. Please try again.");
        }

        SessionManager.getInstance().login(user);
        return user;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        SessionManager.getInstance().logout();
    }
}
