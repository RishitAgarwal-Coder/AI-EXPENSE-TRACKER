package com.smartspend.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Model class representing an Expense.
 * Demonstrates:
 * 1. Inheritance (Extends Transaction abstract class)
 * 2. Polymorphism & Method Overriding (@Override getTransactionType)
 * 3. Constructor Chaining (super(...) calls)
 * 4. Constants and Collections (List of valid categories)
 */
public class Expense extends Transaction {

    // Standard expense categories as requested
    public static final List<String> CATEGORIES = Arrays.asList(
            "Food",
            "Shopping",
            "Travel",
            "Education",
            "Transport",
            "Entertainment",
            "Healthcare",
            "Other"
    );

    // Default Constructor
    public Expense() {
        super();
    }

    // Overloaded Constructor for new expense
    public Expense(int userId, double amount, String category, String description, LocalDate date) {
        super(userId, amount, category, description, date);
    }

    // Overloaded Constructor for existing expense
    public Expense(int id, int userId, double amount, String category, String description, LocalDate date) {
        super(id, userId, amount, category, description, date);
    }

    /**
     * Polymorphic implementation of getTransactionType()
     */
    @Override
    public String getTransactionType() {
        return "EXPENSE";
    }

    @Override
    public String getFormattedSummary() {
        return String.format("[-] %s - $%.2f (%s) on %s", category, amount, description, date);
    }
}
