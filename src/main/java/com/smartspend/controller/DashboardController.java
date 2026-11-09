package com.smartspend.controller;

import com.smartspend.model.CategorySummary;
import com.smartspend.model.Expense;
import com.smartspend.model.Income;
import com.smartspend.model.Transaction;
import com.smartspend.service.ExpenseService;
import com.smartspend.service.IncomeService;
import com.smartspend.util.DateUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for the Main Dashboard.
 * Demonstrates:
 * 1. Polymorphic Collections: Merging Expense and Income into List<Transaction>
 * 2. Financial aggregations and KPI calculations
 * 3. Sorting using Java 8+ Comparator
 */
public class DashboardController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public DashboardController() {
        this.expenseService = new ExpenseService();
        this.incomeService = new IncomeService();
    }

    public double getMonthlyIncome() {
        return incomeService.getTotalIncomeForMonth(DateUtil.getCurrentYearMonth());
    }

    public double getMonthlyExpense() {
        return expenseService.getTotalExpenseForMonth(DateUtil.getCurrentYearMonth());
    }

    public double getRemainingBalance() {
        return getMonthlyIncome() - getMonthlyExpense();
    }

    public double getMonthlySavings() {
        double balance = getRemainingBalance();
        return Math.max(0, balance);
    }

    public List<CategorySummary> getCategorySummaries() {
        return expenseService.getCategorySummaries(DateUtil.getCurrentYearMonth());
    }

    /**
     * Polymorphism demonstration:
     * Combines both Expense and Income into a unified List<Transaction>,
     * sorts them by date descending, and returns top recent records.
     */
    public List<Transaction> getRecentTransactions(int limit) {
        List<Expense> expenses = expenseService.getRecentExpenses(limit);
        List<Income> incomes = incomeService.getRecentIncome(limit);

        // Polymorphic collection
        List<Transaction> transactions = new ArrayList<>();
        transactions.addAll(expenses);
        transactions.addAll(incomes);

        // Sort by date descending
        transactions.sort(Comparator.comparing(Transaction::getDate).reversed());

        if (transactions.size() > limit) {
            return transactions.subList(0, limit);
        }
        return transactions;
    }
}
