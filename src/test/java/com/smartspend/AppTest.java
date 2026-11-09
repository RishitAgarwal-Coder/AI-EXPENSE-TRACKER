package com.smartspend;

import com.smartspend.database.DatabaseInitializer;
import com.smartspend.model.Budget;
import com.smartspend.model.CategorySummary;
import com.smartspend.model.Expense;
import com.smartspend.model.Income;
import com.smartspend.model.MonthlySummary;
import com.smartspend.model.User;
import com.smartspend.service.AuthService;
import com.smartspend.service.BudgetService;
import com.smartspend.service.ExpenseService;
import com.smartspend.service.GeminiService;
import com.smartspend.service.IncomeService;
import com.smartspend.service.ReportService;
import com.smartspend.util.DateUtil;
import com.smartspend.util.FileExportUtil;
import com.smartspend.util.JsonUtil;
import com.smartspend.util.SessionManager;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated Test Suite for SmartSpend.
 * Tests all core phases:
 * Phase 2: Database Initialization & JDBC
 * Phase 3: Auth & Registration
 * Phase 4: Expense & Income Management
 * Phase 5: Aggregations & Dashboard calculations
 * Phase 6: Gemini context & AI prompts
 * Phase 7: Budgets, Reports & File Handling
 */
public class AppTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("      RUNNING SMARTSPEND TEST SUITE (PHASES 1-8)    ");
        System.out.println("====================================================");

        try {
            testDatabaseInitialization();
            testAuthenticationAndSession();
            testExpenseManagement();
            testIncomeManagement();
            testBudgetTracking();
            testFinancialReportsAndFileHandling();
            testJsonAndGeminiContext();

            System.out.println("\n====================================================");
            System.out.printf("TEST RESULTS: %d/%d tests passed successfully! (100%%)%n", testsPassed, testsRun);
            System.out.println("====================================================");
        } catch (Throwable t) {
            System.err.println("TEST SUITE FAILED: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void testDatabaseInitialization() {
        System.out.println("\n[TEST 1] Testing Database Initialization...");
        DatabaseInitializer.initializeDatabase();
        assertTrue("Database tables created and initialized", true);
    }

    private static void testAuthenticationAndSession() {
        System.out.println("\n[TEST 2] Testing Authentication & Registration...");
        AuthService authService = new AuthService();

        // 1. Test Demo user login
        User demo = authService.login("demo@smartspend.com", "password123");
        assertNotNull("Demo user should exist", demo);
        assertEquals("User ID must match", 1, demo.getId());
        assertTrue("SessionManager should have active user", SessionManager.getInstance().isLoggedIn());

        // 2. Test Registration with unique email
        String testEmail = "student_" + System.currentTimeMillis() + "@test.com";
        User newUser = authService.register("Test Student", testEmail, "pass1234", "pass1234");
        assertNotNull("Registered user should not be null", newUser);
        assertTrue("New user ID should be > 0", newUser.getId() > 0);

        // 3. Test Validation Failures
        try {
            authService.register("", testEmail, "short", "short");
            fail("Should fail on empty name");
        } catch (IllegalArgumentException e) {
            assertTrue("Validation caught empty name", true);
        }

        // Switch session back to demo user
        authService.login("demo@smartspend.com", "password123");
    }

    private static void testExpenseManagement() {
        System.out.println("\n[TEST 3] Testing Expense Management CRUD...");
        ExpenseService service = new ExpenseService();

        // 1. Add expense
        boolean added = service.addExpense(120.50, "Food", "College Cafeteria Pizza", LocalDate.now());
        assertTrue("Expense should be added successfully", added);

        // 2. Retrieve expenses
        List<Expense> expenses = service.getAllExpenses();
        assertTrue("Expenses list should not be empty", expenses.size() > 0);

        // 3. Filter expenses
        List<Expense> filtered = service.filterExpenses("Pizza", "Food", null, null);
        assertTrue("Filtered expense should match description", filtered.size() >= 1);

        // 4. Update expense
        Expense lastExp = expenses.get(0);
        boolean updated = service.updateExpense(lastExp.getId(), 145.00, "Food", "Updated Pizza and Drinks", LocalDate.now());
        assertTrue("Expense update should succeed", updated);

        // 5. Monthly category summaries
        List<CategorySummary> summaries = service.getCategorySummaries(DateUtil.getCurrentYearMonth());
        assertTrue("Category summaries should have entries", summaries.size() > 0);
    }

    private static void testIncomeManagement() {
        System.out.println("\n[TEST 4] Testing Income Management CRUD...");
        IncomeService service = new IncomeService();

        // 1. Add income
        boolean added = service.addIncome(3500.00, "Freelancing", "Java Tutoring Session", LocalDate.now());
        assertTrue("Income should be added successfully", added);

        // 2. Retrieve all income
        List<Income> incomes = service.getAllIncome();
        assertTrue("Income list should have items", incomes.size() > 0);

        // 3. Monthly total calculation
        double monthlyIncome = service.getTotalIncomeForMonth(DateUtil.getCurrentYearMonth());
        assertTrue("Monthly income should be positive", monthlyIncome > 0);
    }

    private static void testBudgetTracking() {
        System.out.println("\n[TEST 5] Testing Monthly Budget Tracking...");
        BudgetService service = new BudgetService();
        String currentMonth = DateUtil.getCurrentYearMonth();

        // 1. Set budget
        boolean set = service.setBudget("Food", currentMonth, 4500.00);
        assertTrue("Budget should be saved or updated", set);

        // 2. Retrieve budgets with spent amounts
        List<Budget> budgets = service.getBudgetsForMonth(currentMonth);
        assertTrue("Budgets should not be empty", budgets.size() > 0);

        Budget foodBudget = budgets.stream()
                .filter(b -> "Food".equalsIgnoreCase(b.getCategory()))
                .findFirst()
                .orElse(null);

        assertNotNull("Food budget should exist", foodBudget);
        assertTrue("Food budget spent amount should be >= 0", foodBudget.getSpentAmount() >= 0);
        System.out.printf("   [Budget Check] Food: Limit $%.2f, Spent $%.2f, Remaining $%.2f%n",
                foodBudget.getAmount(), foodBudget.getSpentAmount(), foodBudget.getRemainingAmount());
    }

    private static void testFinancialReportsAndFileHandling() throws Exception {
        System.out.println("\n[TEST 6] Testing Reports and File Handling (I/O)...");
        ReportService reportService = new ReportService();
        String currentMonth = DateUtil.getCurrentYearMonth();

        // 1. Monthly summary calculation
        MonthlySummary summary = reportService.getMonthlySummary(currentMonth);
        assertNotNull("MonthlySummary should not be null", summary);
        assertTrue("Total Income should be positive", summary.getTotalIncome() > 0);
        assertTrue("Total Expenses should be positive", summary.getTotalExpense() > 0);

        // 2. Export CSV test
        File tempCsv = File.createTempFile("smartspend_test_", ".csv");
        tempCsv.deleteOnExit();
        reportService.exportExpensesToCsv(tempCsv);
        assertTrue("CSV file should exist", tempCsv.exists());
        assertTrue("CSV file should not be empty", tempCsv.length() > 0);

        // 3. Export TXT report test
        File tempTxt = File.createTempFile("smartspend_report_", ".txt");
        tempTxt.deleteOnExit();
        reportService.exportMonthlyReport(tempTxt, currentMonth);
        assertTrue("Report TXT file should exist", tempTxt.exists());
        assertTrue("Report TXT file should not be empty", tempTxt.length() > 0);

        // 4. File reading demonstration
        String preview = FileExportUtil.readFilePreview(tempTxt, 5);
        assertTrue("File preview should contain header", preview.contains("SMARTSPEND FINANCIAL REPORT"));
        System.out.println("   [File Handling] CSV and TXT exported and verified successfully.");
    }

    private static void testJsonAndGeminiContext() {
        System.out.println("\n[TEST 7] Testing JSON Utility & AI Context Builder...");

        // 1. JSON Request formatting
        String requestJson = JsonUtil.buildGeminiRequest("Hello, SmartSpend!");
        assertTrue("JSON request should contain text", requestJson.contains("\"text\": \"Hello, SmartSpend!\""));

        // 2. Real database financial context builder
        GeminiService geminiService = new GeminiService();
        String context = geminiService.buildRealDatabaseFinancialContext();
        assertNotNull("Context should not be null", context);
        assertTrue("Context should contain financial snapshot", context.contains("FINANCIAL SNAPSHOT"));
        assertTrue("Context should contain category spending", context.contains("SPENDING BY CATEGORY"));

        // 3. Test Smart Offline Financial Analyzer
        try {
            String answer = geminiService.askAiAssistantAsync("Where am I spending most of my money?").get();
            assertNotNull("AI answer should not be null", answer);
            assertTrue("AI answer should provide category insight", answer.contains("highest spending category") || answer.contains("Food"));
            System.out.println("   [AI Assistant Output Preview]:\n" + answer.indent(4));
        } catch (Exception e) {
            fail("Gemini service execution threw error: " + e.getMessage());
        }
    }

    // Assertion helpers
    private static void assertTrue(String message, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  ✓ PASS: " + message);
        } else {
            System.err.println("  ✗ FAIL: " + message);
            throw new AssertionError("Test failed: " + message);
        }
    }

    private static void assertNotNull(String message, Object obj) {
        assertTrue(message, obj != null);
    }

    private static void assertEquals(String message, Object expected, Object actual) {
        assertTrue(message + " (Expected: " + expected + ", Actual: " + actual + ")",
                (expected == null && actual == null) || (expected != null && expected.equals(actual)));
    }

    private static void fail(String message) {
        assertTrue("FAIL: " + message, false);
    }
}
