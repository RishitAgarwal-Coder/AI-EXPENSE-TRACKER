package com.smartspend.util;

import com.smartspend.model.CategorySummary;
import com.smartspend.model.Expense;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * File Handling Utility Class for SmartSpend.
 * Demonstrates:
 * 1. Java File I/O (File, FileWriter, PrintWriter, BufferedReader, FileReader)
 * 2. Exception Handling (try-with-resources, IOException)
 * 3. CSV generation and Text Report generation
 */
public class FileExportUtil {

    /**
     * Exports a list of expenses to a CSV file.
     * Demonstrates FileWriter and PrintWriter.
     *
     * @param targetFile Destination file selected by the user
     * @param expenses   List of expenses to write
     * @throws IOException If write fails
     */
    public static void exportExpensesToCsv(File targetFile, List<Expense> expenses) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            // Write CSV Header
            writer.println("ID,Date,Category,Amount,Description");

            // Write Records
            for (Expense exp : expenses) {
                writer.printf("%d,%s,\"%s\",%.2f,\"%s\"%n",
                        exp.getId(),
                        exp.getDate(),
                        escapeCsv(exp.getCategory()),
                        exp.getAmount(),
                        escapeCsv(exp.getDescription())
                );
            }
        }
        System.out.println("[FileExportUtil] Successfully exported " + expenses.size() + " expenses to CSV: " + targetFile.getAbsolutePath());
    }

    /**
     * Generates and writes a comprehensive financial summary report to a text file.
     * Demonstrates structured text formatting and File I/O.
     */
    public static void exportMonthlyReportToText(File targetFile, String month, double income, double expense,
                                                double savings, List<CategorySummary> categories) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("==========================================================");
            writer.println("           SMARTSPEND FINANCIAL REPORT - " + month);
            writer.println("==========================================================");
            writer.printf("Generated On: %s%n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")));
            writer.println("----------------------------------------------------------");
            writer.println("                   FINANCIAL SUMMARY                      ");
            writer.println("----------------------------------------------------------");
            writer.printf("  Total Monthly Income   : $%,10.2f%n", income);
            writer.printf("  Total Monthly Expenses : $%,10.2f%n", expense);
            writer.printf("  Net Savings            : $%,10.2f%n", savings);
            double rate = (income > 0) ? (savings / income) * 100.0 : 0.0;
            writer.printf("  Savings Rate           : %10.1f%%%n", rate);
            writer.println("----------------------------------------------------------");
            writer.println("               CATEGORY-WISE SPENDING                     ");
            writer.println("----------------------------------------------------------");
            writer.printf("%-18s %-12s %-10s %-8s%n", "Category", "Amount ($)", "Share (%)", "Count");
            writer.println("----------------------------------------------------------");

            if (categories == null || categories.isEmpty()) {
                writer.println("  No expenses recorded for this month.");
            } else {
                for (CategorySummary cat : categories) {
                    writer.printf("%-18s $%,10.2f %9.1f%% %8d%n",
                            cat.getCategory(),
                            cat.getTotalAmount(),
                            cat.getPercentage(),
                            cat.getTransactionCount()
                    );
                }
            }

            writer.println("==========================================================");
            writer.println("       SmartSpend - AI Expense Tracker (College Project)  ");
            writer.println("==========================================================");
        }
    }

    /**
     * Reads a file using BufferedReader to verify content or display preview.
     * Demonstrates Java FileReader and BufferedReader.
     */
    public static String readFilePreview(File file, int maxLines) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < maxLines) {
                sb.append(line).append(System.lineSeparator());
                count++;
            }
        }
        return sb.toString();
    }

    private static String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
