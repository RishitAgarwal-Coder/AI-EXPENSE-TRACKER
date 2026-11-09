package com.smartspend.controller;

import com.smartspend.model.Income;
import com.smartspend.service.ExpenseService;
import com.smartspend.service.IncomeService;
import com.smartspend.util.AlertHelper;
import com.smartspend.util.DateUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Income Management.
 * Demonstrates:
 * 1. CRUD handling for Income
 * 2. Cross-service calculations (Income vs Expense balance)
 * 3. Input validation
 */
public class IncomeController {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public IncomeController() {
        this.incomeService = new IncomeService();
        this.expenseService = new ExpenseService();
    }

    public List<Income> loadAllIncome() {
        return incomeService.getAllIncome();
    }

    public List<Income> filterIncome(String keyword, String source, LocalDate start, LocalDate end) {
        return incomeService.filterIncome(keyword, source, start, end);
    }

    public double getMonthlyTotalIncome() {
        return incomeService.getTotalIncomeForMonth(DateUtil.getCurrentYearMonth());
    }

    public double getRemainingBalance() {
        String currentMonth = DateUtil.getCurrentYearMonth();
        return incomeService.getTotalIncomeForMonth(currentMonth) - expenseService.getTotalExpenseForMonth(currentMonth);
    }

    public boolean addIncome(String amountStr, String source, String description, LocalDate date) {
        try {
            double amount = parseAmount(amountStr);
            boolean ok = incomeService.addIncome(amount, source, description, date);
            if (ok) {
                AlertHelper.showInfo("Income Added", "Success", "Income of $" + String.format("%.2f", amount) + " added successfully.");
                return true;
            }
        } catch (IllegalArgumentException e) {
            AlertHelper.showWarning("Validation Error", "Invalid Input", e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Database Error", "Failed to Add Income", e.getMessage());
        }
        return false;
    }

    public boolean updateIncome(int incomeId, String amountStr, String source, String description, LocalDate date) {
        try {
            double amount = parseAmount(amountStr);
            boolean ok = incomeService.updateIncome(incomeId, amount, source, description, date);
            if (ok) {
                AlertHelper.showInfo("Income Updated", "Success", "Income record updated successfully.");
                return true;
            }
        } catch (IllegalArgumentException e) {
            AlertHelper.showWarning("Validation Error", "Invalid Input", e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Database Error", "Failed to Update Income", e.getMessage());
        }
        return false;
    }

    public boolean deleteIncome(Income income) {
        if (income == null) return false;
        boolean confirm = AlertHelper.showConfirmation(
                "Delete Income",
                "Are you sure?",
                "Do you want to delete this income record: " + income.getSource() + " ($" + income.getAmount() + ")?"
        );
        if (confirm) {
            try {
                boolean ok = incomeService.deleteIncome(income.getId());
                if (ok) {
                    AlertHelper.showInfo("Income Deleted", "Success", "Income record removed.");
                    return true;
                }
            } catch (Exception e) {
                AlertHelper.showError("Error", "Delete Failed", e.getMessage());
            }
        }
        return false;
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
            throw new IllegalArgumentException("Please enter a valid numeric amount.");
        }
    }
}
