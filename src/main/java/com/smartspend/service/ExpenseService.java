package com.smartspend.service;

import com.smartspend.dao.ExpenseDao;
import com.smartspend.model.CategorySummary;
import com.smartspend.model.Expense;
import com.smartspend.util.SessionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service class handling Expense business logic.
 * Demonstrates:
 * 1. Business rules validation
 * 2. Method Overloading (various retrieval methods)
 * 3. DAO delegation
 */
public class ExpenseService {

    private final ExpenseDao expenseDao;

    public ExpenseService() {
        this.expenseDao = new ExpenseDao();
    }

    public ExpenseService(ExpenseDao expenseDao) {
        this.expenseDao = expenseDao;
    }

    /**
     * Adds a new expense with validation.
     */
    public boolean addExpense(double amount, String category, String description, LocalDate date) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            throw new IllegalStateException("No active user session.");
        }
        validateExpenseData(amount, category, date);

        Expense expense = new Expense(userId, amount, category, description, date);
        return expenseDao.save(expense);
    }

    /**
     * Updates an existing expense.
     */
    public boolean updateExpense(int expenseId, double amount, String category, String description, LocalDate date) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            throw new IllegalStateException("No active user session.");
        }
        validateExpenseData(amount, category, date);

        Expense expense = new Expense(expenseId, userId, amount, category, description, date);
        return expenseDao.update(expense);
    }

    /**
     * Deletes an expense by its ID.
     */
    public boolean deleteExpense(int expenseId) {
        return expenseDao.delete(expenseId);
    }

    /**
     * Retrieves all expenses for the current logged-in user.
     */
    public List<Expense> getAllExpenses() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return expenseDao.findAllByUserId(userId);
    }

    /**
     * Overloaded method: Search and filter expenses.
     */
    public List<Expense> filterExpenses(String keyword, String category, LocalDate startDate, LocalDate endDate) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return expenseDao.searchAndFilter(userId, keyword, category, startDate, endDate);
    }

    /**
     * Calculates total monthly expenses.
     */
    public double getTotalExpenseForMonth(String yearMonth) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return expenseDao.getTotalExpensesForMonth(userId, yearMonth);
    }

    /**
     * Calculates total all-time expenses.
     */
    public double getTotalExpenseAllTime() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return expenseDao.getTotalExpensesAllTime(userId);
    }

    /**
     * Retrieves recent expenses.
     */
    public List<Expense> getRecentExpenses(int limit) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return expenseDao.getRecentExpenses(userId, limit);
    }

    /**
     * Retrieves category summaries for charting.
     */
    public List<CategorySummary> getCategorySummaries(String yearMonth) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return expenseDao.getCategorySummaries(userId, yearMonth);
    }

    /**
     * Retrieves monthly totals for bar charts.
     */
    public Map<String, Double> getMonthlyTotals(int monthsCount) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return expenseDao.getMonthlyTotalsForPastMonths(userId, monthsCount);
    }

    private void validateExpenseData(double amount, String category, LocalDate date) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select an expense category.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Please select a valid date.");
        }
    }
}
