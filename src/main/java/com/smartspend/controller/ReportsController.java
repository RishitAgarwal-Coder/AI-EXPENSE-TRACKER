package com.smartspend.controller;

import com.smartspend.model.CategorySummary;
import com.smartspend.model.MonthlySummary;
import com.smartspend.service.ExpenseService;
import com.smartspend.service.IncomeService;
import com.smartspend.service.ReportService;
import com.smartspend.util.AlertHelper;
import com.smartspend.util.DateUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Controller for Financial Reports and Analytics.
 * Demonstrates:
 * 1. Financial analysis and data aggregation
 * 2. BarChart data preparation (Income vs Expense)
 * 3. File export handling
 */
public class ReportsController {

    private final ReportService reportService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public ReportsController() {
        this.reportService = new ReportService();
        this.expenseService = new ExpenseService();
        this.incomeService = new IncomeService();
    }

    public MonthlySummary getMonthlySummary(String month) {
        return reportService.getMonthlySummary(month);
    }

    public List<CategorySummary> getCategoryReport(String month) {
        return reportService.getCategoryReport(month);
    }

    public Map<String, Double> getMonthlyIncomes(int months) {
        return incomeService.getMonthlyTotals(months);
    }

    public Map<String, Double> getMonthlyExpenses(int months) {
        return expenseService.getMonthlyTotals(months);
    }

    public void exportMonthlyReportToText(File file, String month) {
        try {
            reportService.exportMonthlyReport(file, month);
            AlertHelper.showInfo("Report Exported", "Text Report Created", "Monthly summary saved to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            AlertHelper.showError("Export Failed", "Error Writing Report", e.getMessage());
        }
    }

    public void exportAllExpensesToCsv(File file) {
        try {
            reportService.exportExpensesToCsv(file);
            AlertHelper.showInfo("Export Complete", "CSV Exported", "Expense data saved to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            AlertHelper.showError("Export Failed", "Error Writing CSV", e.getMessage());
        }
    }
}
