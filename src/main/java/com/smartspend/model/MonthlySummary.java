package com.smartspend.model;

/**
 * Model class for aggregated monthly financial overview.
 * Displays Total Income, Total Expenses, Balance, and Savings Rate.
 */
public class MonthlySummary {

    private String month; // e.g. "2026-11"
    private double totalIncome;
    private double totalExpense;

    public MonthlySummary(String month, double totalIncome, double totalExpense) {
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
    }

    public String getMonth() {
        return month;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetSavings() {
        return totalIncome - totalExpense;
    }

    public double getSavingsRate() {
        if (totalIncome <= 0) return 0.0;
        return (getNetSavings() / totalIncome) * 100.0;
    }
}
