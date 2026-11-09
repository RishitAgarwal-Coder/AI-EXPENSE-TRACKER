package com.smartspend.util;

/**
 * Custom Exception class representing database-related errors.
 * Demonstrates Java Exception Handling, Custom Exceptions, and Constructor Chaining.
 */
public class DatabaseException extends RuntimeException {

    // Default constructor
    public DatabaseException() {
        super("A database error occurred in SmartSpend.");
    }

    // Parameterized constructor with message
    public DatabaseException(String message) {
        super(message);
    }

    // Parameterized constructor with message and root cause
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
