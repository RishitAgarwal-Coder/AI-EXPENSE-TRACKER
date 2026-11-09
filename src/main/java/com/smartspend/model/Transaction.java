package com.smartspend.model;

import java.time.LocalDate;

/**
 * Abstract Base Class for financial transactions.
 * Demonstrates:
 * 1. Abstraction (Abstract class with abstract method getTransactionType())
 * 2. Inheritance (Base class for Expense and Income)
 * 3. Encapsulation (Protected/private fields with public getters/setters)
 * 4. Polymorphism (Abstract method implemented differently in child classes)
 * 5. Constructors and Constructor Overloading
 */
public abstract class Transaction {

    protected int id;
    protected int userId;
    protected double amount;
    protected String category; // or source for income
    protected String description;
    protected LocalDate date;

    // Default constructor
    public Transaction() {
        this.date = LocalDate.now();
    }

    // Overloaded Constructor without ID (for new records)
    public Transaction(int userId, double amount, String category, String description, LocalDate date) {
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = (date != null) ? date : LocalDate.now();
    }

    // Overloaded Constructor with ID (for existing database records)
    public Transaction(int id, int userId, double amount, String category, String description, LocalDate date) {
        this(userId, amount, category, description, date); // Constructor Chaining
        this.id = id;
    }

    /**
     * Polymorphic Abstract Method:
     * Subclasses (Expense, Income) must provide their own implementation.
     * @return Transaction type string ("EXPENSE" or "INCOME")
     */
    public abstract String getTransactionType();

    /**
     * Formats a summary of this transaction.
     * Demonstrates Polymorphism and Method Overriding in subclasses.
     */
    public String getFormattedSummary() {
        return String.format("[%s] %s: $%.2f (%s) on %s",
                getTransactionType(), category, amount, description, date);
    }

    // Encapsulated Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative!");
        }
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return getFormattedSummary();
    }
}
