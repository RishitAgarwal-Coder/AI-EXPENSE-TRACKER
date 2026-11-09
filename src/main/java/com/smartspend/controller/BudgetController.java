package com.smartspend.controller;

import com.smartspend.model.Budget;
import com.smartspend.service.BudgetService;
import com.smartspend.util.AlertHelper;
import com.smartspend.util.DateUtil;

import java.util.List;

/**
 * Controller for Budget Management.
 * Demonstrates:
 * 1. Budget tracking and alert calculations
 * 2. Input validation
 */
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController() {
        this.budgetService = new BudgetService();
    }

    public List<Budget> getBudgetsForMonth(String month) {
        return budgetService.getBudgetsForMonth(month);
    }

    public boolean setBudget(String category, String month, String amountStr) {
        try {
            double amount = parseAmount(amountStr);
            boolean ok = budgetService.setBudget(category, month, amount);
            if (ok) {
                AlertHelper.showInfo("Budget Saved", "Success", "Budget for " + category + " set to $" + String.format("%.2f", amount));
                return true;
            }
        } catch (IllegalArgumentException e) {
            AlertHelper.showWarning("Validation Error", "Invalid Input", e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Database Error", "Failed to Save Budget", e.getMessage());
        }
        return false;
    }

    public boolean deleteBudget(Budget budget) {
        if (budget == null) return false;
        boolean confirm = AlertHelper.showConfirmation(
                "Delete Budget",
                "Remove Budget?",
                "Are you sure you want to delete the budget for " + budget.getCategory() + "?"
        );
        if (confirm) {
            try {
                boolean ok = budgetService.deleteBudget(budget.getId());
                if (ok) {
                    AlertHelper.showInfo("Budget Deleted", "Success", "Budget removed.");
                    return true;
                }
            } catch (Exception e) {
                AlertHelper.showError("Error", "Delete Failed", e.getMessage());
            }
        }
        return false;
    }

    public double getTotalBudget(String month) {
        return budgetService.getTotalBudgetLimit(month);
    }

    public int getExceededCount(String month) {
        return budgetService.getExceededBudgetsCount(month);
    }

    private double parseAmount(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be empty.");
        }
        try {
            double val = Double.parseDouble(str.trim());
            if (val <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid numeric budget amount.");
        }
    }
}
