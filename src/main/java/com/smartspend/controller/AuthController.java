package com.smartspend.controller;

import com.smartspend.model.User;
import com.smartspend.service.AuthService;
import com.smartspend.util.AlertHelper;

import java.util.function.Consumer;

/**
 * Controller handling user authentication and registration workflows.
 * Demonstrates:
 * 1. Controller role in MVC pattern
 * 2. Exception Handling and UI feedback
 * 3. Functional Callbacks (java.util.function.Consumer)
 */
public class AuthController {

    private final AuthService authService;
    private Consumer<User> onLoginSuccess;

    public AuthController() {
        this.authService = new AuthService();
    }

    public void setOnLoginSuccess(Consumer<User> callback) {
        this.onLoginSuccess = callback;
    }

    /**
     * Handles user login action.
     */
    public boolean handleLogin(String email, String password) {
        try {
            User user = authService.login(email, password);
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(user);
            }
            return true;
        } catch (IllegalArgumentException e) {
            AlertHelper.showWarning("Login Failed", "Invalid Credentials", e.getMessage());
            return false;
        } catch (Exception e) {
            AlertHelper.showError("Database Error", "Login Error", "Could not connect to database: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles user registration action.
     */
    public boolean handleRegister(String name, String email, String password, String confirmPassword) {
        try {
            User user = authService.register(name, email, password, confirmPassword);
            AlertHelper.showInfo("Registration Successful", "Welcome to SmartSpend!",
                    "Account created successfully for " + user.getName() + " (" + user.getEmail() + "). You can now log in.");
            return true;
        } catch (IllegalArgumentException e) {
            AlertHelper.showWarning("Registration Validation", "Invalid Input", e.getMessage());
            return false;
        } catch (Exception e) {
            AlertHelper.showError("Registration Error", "Error Saving User", e.getMessage());
            return false;
        }
    }

    /**
     * Handles user logout.
     */
    public void handleLogout() {
        authService.logout();
    }
}
