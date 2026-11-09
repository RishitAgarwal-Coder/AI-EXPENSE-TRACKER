package com.smartspend.service;

import com.smartspend.dao.BudgetDao;
import com.smartspend.model.Budget;
import com.smartspend.util.DateUtil;
import com.smartspend.util.SessionManager;

import java.util.List;

/**
 * Service class handling Budget management and alert logic.
 * Demonstrates:
 * 1. Business Logic Layer
 * 2. Budget vs Expense calculations
 * 3. Threshold warnings
 */
public class BudgetService {

    private final BudgetDao budgetDao;

    public BudgetService() {
        this.budgetDao = new BudgetDao();
    }

    public BudgetService(BudgetDao budgetDao) {
        this.budgetDao = budgetDao;
    }

    /**
     * Sets or updates budget for a category in the specified month.
     */
    public boolean setBudget(String category, String month, double amount) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            throw new IllegalStateException("No active user session.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (month == null || month.trim().isEmpty()) {
            month = DateUtil.getCurrentYearMonth();
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Budget amount must be greater than zero.");
        }

        Budget budget = new Budget(userId, category, month, amount);
        return budgetDao.saveOrUpdate(budget);
    }

    /**
     * Retrieves all budgets for a given month with spent calculation.
     */
    public List<Budget> getBudgetsForMonth(String month) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (month == null || month.trim().isEmpty()) {
            month = DateUtil.getCurrentYearMonth();
        }
        return budgetDao.getBudgetsWithSpentForMonth(userId, month);
    }

    /**
     * Deletes a budget by ID.
     */
    public boolean deleteBudget(int budgetId) {
        return budgetDao.delete(budgetId);
    }

    /**
     * Calculates total budget limit for a month across all categories.
     */
    public double getTotalBudgetLimit(String month) {
        List<Budget> budgets = getBudgetsForMonth(month);
        double total = 0.0;
        for (Budget b : budgets) {
            total += b.getAmount();
        }
        return total;
    }

    /**
     * Counts how many categories have exceeded their budget.
     */
    public int getExceededBudgetsCount(String month) {
        List<Budget> budgets = getBudgetsForMonth(month);
        int count = 0;
        for (Budget b : budgets) {
            if (b.isExceeded()) {
                count++;
            }
        }
        return count;
    }
}
