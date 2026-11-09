package com.smartspend.util;

import com.smartspend.model.User;

/**
 * SessionManager maintains the authenticated user session.
 * Demonstrates:
 * 1. Singleton Design Pattern
 * 2. Encapsulation of active session state
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        System.out.println("[SessionManager] User logged in: " + user.getEmail());
    }

    public void logout() {
        System.out.println("[SessionManager] User logged out: " + (currentUser != null ? currentUser.getEmail() : "none"));
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}
