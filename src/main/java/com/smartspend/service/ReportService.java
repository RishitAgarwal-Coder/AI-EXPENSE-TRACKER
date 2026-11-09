package com.smartspend.service;

import com.smartspend.model.CategorySummary;
import com.smartspend.model.Expense;
import com.smartspend.model.MonthlySummary;
import com.smartspend.util.DateUtil;
import com.smartspend.util.FileExportUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Service class handling Financial Reporting and File Export.
 * Demonstrates:
 * 1. Business Logic Aggregation
 * 2. File Export integration (CSV and TXT formats)
 */
public class ReportService {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public ReportService() {
        this.expenseService = new ExpenseService();
        this.incomeService = new IncomeService();
    }

    public ReportService(ExpenseService expenseService, IncomeService incomeService) {
        this.expenseService = expenseService;
        this.incomeService = incomeService;
    }

    /**
     * Generates a monthly summary for the given month.
     */
    public MonthlySummary getMonthlySummary(String month) {
        if (month == null || month.isEmpty()) {
            month = DateUtil.getCurrentYearMonth();
        }
        double income = incomeService.getTotalIncomeForMonth(month);
        double expense = expenseService.getTotalExpenseForMonth(month);
        return new MonthlySummary(month, income, expense);
    }

    /**
     * Retrieves category summaries for reporting.
     */
    public List<CategorySummary> getCategoryReport(String month) {
        return expenseService.getCategorySummaries(month);
    }

    /**
     * Exports all expenses to CSV format.
     */
    public void exportExpensesToCsv(File file) throws IOException {
        List<Expense> expenses = expenseService.getAllExpenses();
        FileExportUtil.exportExpensesToCsv(file, expenses);
    }

    /**
     * Exports detailed monthly financial statement to formatted TXT report.
     */
    public void exportMonthlyReport(File file, String month) throws IOException {
        MonthlySummary summary = getMonthlySummary(month);
        List<CategorySummary> categories = getCategoryReport(month);
        FileExportUtil.exportMonthlyReportToText(
                file,
                DateUtil.formatMonthHeader(month),
                summary.getTotalIncome(),
                summary.getTotalExpense(),
                summary.getNetSavings(),
                categories
        );
    }
}
