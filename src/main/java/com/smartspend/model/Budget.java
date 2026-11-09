package com.smartspend.model;

/**
 * Model class representing a Monthly Budget.
 * Demonstrates:
 * 1. Encapsulation
 * 2. Business logic calculation inside domain model (remaining budget, percentage spent)
 * 3. Method Overloading & Constructors
 */
public class Budget {

    private int id;
    private int userId;
    private String category;
    private String month; // Format: YYYY-MM
    private double amount; // Budget limit

    // Calculated fields (populated by service/dao for UI display)
    private double spentAmount;

    public Budget() {
    }

    public Budget(int userId, String category, String month, double amount) {
        this.userId = userId;
        this.category = category;
        this.month = month;
        this.amount = amount;
    }

    public Budget(int id, int userId, String category, String month, double amount) {
        this(userId, category, month, amount);
        this.id = id;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    /**
     * Calculates the remaining budget.
     * Can be negative if budget has been exceeded.
     */
    public double getRemainingAmount() {
        return amount - spentAmount;
    }

    /**
     * Calculates percentage of budget spent (0.0 to 100.0+).
     */
    public double getPercentageSpent() {
        if (amount <= 0) return 0.0;
        return (spentAmount / amount) * 100.0;
    }

    /**
     * Checks if the user has exceeded their budget limit.
     */
    public boolean isExceeded() {
        return spentAmount > amount;
    }

    /**
     * Warning threshold check (e.g. reached 80% of budget)
     */
    public boolean isNearLimit() {
        return getPercentageSpent() >= 80.0 && !isExceeded();
    }

    @Override
    public String toString() {
        return "Budget{" +
                "category='" + category + '\'' +
                ", month='" + month + '\'' +
                ", amount=" + amount +
                ", spentAmount=" + spentAmount +
                ", remaining=" + getRemainingAmount() +
                '}';
    }
}
