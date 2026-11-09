package com.smartspend.controller;

import com.smartspend.model.Expense;
import com.smartspend.service.ExpenseService;
import com.smartspend.util.AlertHelper;
import com.smartspend.util.FileExportUtil;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Expense Management.
 * Demonstrates:
 * 1. CRUD handling
 * 2. Input validation and user alerts
 * 3. File export delegation
 */
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController() {
        this.expenseService = new ExpenseService();
    }

    public List<Expense> loadAllExpenses() {
        return expenseService.getAllExpenses();
    }

    public List<Expense> filterExpenses(String keyword, String category, LocalDate start, LocalDate end) {
        return expenseService.filterExpenses(keyword, category, start, end);
    }

    public boolean addExpense(String amountStr, String category, String description, LocalDate date) {
        try {
            double amount = parseAmount(amountStr);
            boolean ok = expenseService.addExpense(amount, category, description, date);
            if (ok) {
                AlertHelper.showInfo("Expense Added", "Success", "Expense of $" + String.format("%.2f", amount) + " added successfully.");
                return true;
            }
        } catch (IllegalArgumentException e) {
            AlertHelper.showWarning("Validation Error", "Invalid Input", e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Database Error", "Failed to Add Expense", e.getMessage());
        }
        return false;
    }

    public boolean updateExpense(int expenseId, String amountStr, String category, String description, LocalDate date) {
        try {
            double amount = parseAmount(amountStr);
            boolean ok = expenseService.updateExpense(expenseId, amount, category, description, date);
            if (ok) {
                AlertHelper.showInfo("Expense Updated", "Success", "Expense updated successfully.");
                return true;
            }
        } catch (IllegalArgumentException e) {
            AlertHelper.showWarning("Validation Error", "Invalid Input", e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Database Error", "Failed to Update Expense", e.getMessage());
        }
        return false;
    }

    public boolean deleteExpense(Expense expense) {
        if (expense == null) return false;
        boolean confirm = AlertHelper.showConfirmation(
                "Delete Expense",
                "Are you sure?",
                "Do you want to delete this expense: " + expense.getCategory() + " ($" + expense.getAmount() + ")?"
        );
        if (confirm) {
            try {
                boolean ok = expenseService.deleteExpense(expense.getId());
                if (ok) {
                    AlertHelper.showInfo("Expense Deleted", "Success", "Expense was successfully removed.");
                    return true;
                }
            } catch (Exception e) {
                AlertHelper.showError("Error", "Delete Failed", e.getMessage());
            }
        }
        return false;
    }

    public void exportToCsv(File file, List<Expense> expenses) {
        try {
            FileExportUtil.exportExpensesToCsv(file, expenses);
            AlertHelper.showInfo("Export Complete", "CSV Exported", "Expenses saved to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            AlertHelper.showError("Export Failed", "Error Writing File", e.getMessage());
        }
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
            throw new IllegalArgumentException("Please enter a valid numeric amount (e.g. 25.50).");
        }
    }
}
