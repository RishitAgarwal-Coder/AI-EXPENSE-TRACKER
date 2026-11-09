package com.smartspend.model;

/**
 * Model class for aggregated category spending.
 * Used for JavaFX PieCharts, BarCharts, and Reports.
 */
public class CategorySummary {

    private String category;
    private double totalAmount;
    private int transactionCount;
    private double percentage;

    public CategorySummary(String category, double totalAmount, int transactionCount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    public CategorySummary(String category, double totalAmount, int transactionCount, double percentage) {
        this(category, totalAmount, transactionCount);
        this.percentage = percentage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return String.format("%s: $%.2f (%.1f%%, %d transactions)",
                category, totalAmount, percentage, transactionCount);
    }
}
