package com.smartspend.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Model class representing Income.
 * Demonstrates:
 * 1. Inheritance (Extends Transaction abstract class)
 * 2. Polymorphism & Method Overriding (@Override getTransactionType)
 * 3. Constructor Chaining (super(...) calls)
 * 4. Constants and Collections
 */
public class Income extends Transaction {

    // Standard income sources
    public static final List<String> SOURCES = Arrays.asList(
            "Salary",
            "Allowance",
            "Freelancing",
            "Part-Time Job",
            "Investment",
            "Gift",
            "Scholarship",
            "Other"
    );

    // Default Constructor
    public Income() {
        super();
    }

    // Overloaded Constructor for new income
    public Income(int userId, double amount, String source, String description, LocalDate date) {
        super(userId, amount, source, description, date);
    }

    // Overloaded Constructor for existing income
    public Income(int id, int userId, double amount, String source, String description, LocalDate date) {
        super(id, userId, amount, source, description, date);
    }

    /**
     * Source is stored in category field of parent Transaction
     */
    public String getSource() {
        return getCategory();
    }

    public void setSource(String source) {
        setCategory(source);
    }

    /**
     * Polymorphic implementation of getTransactionType()
     */
    @Override
    public String getTransactionType() {
        return "INCOME";
    }

    @Override
    public String getFormattedSummary() {
        return String.format("[+] %s + $%.2f (%s) on %s", category, amount, description, date);
    }
}
