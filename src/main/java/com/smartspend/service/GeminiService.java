package com.smartspend.service;

import com.smartspend.model.Budget;
import com.smartspend.model.CategorySummary;
import com.smartspend.model.Expense;
import com.smartspend.model.MonthlySummary;
import com.smartspend.util.DateUtil;
import com.smartspend.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for AI Integration using Google Gemini API.
 * Demonstrates:
 * 1. Java 21 java.net.http.HttpClient (Modern asynchronous HTTP requests)
 * 2. Secure API Key handling via Environment Variables (never hardcoded)
 * 3. Real financial context injection from database into AI prompt
 * 4. Exception Handling and Offline Fallback Analysis for college viva
 */
public class GeminiService {

    // Default Gemini model endpoint
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    private HttpClient httpClient;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final BudgetService budgetService;

    private String runtimeApiKey;

    public GeminiService() {
        this.expenseService = new ExpenseService();
        this.incomeService = new IncomeService();
        this.budgetService = new BudgetService();
    }

    private synchronized HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }
        return httpClient;
    }

    /**
     * Resolves the Gemini API Key from:
     * 1. Runtime override (if user typed it in dialog)
     * 2. System Environment Variable: GEMINI_API_KEY
     * 3. Java System Property: -Dgemini.api.key
     */
    public String getApiKey() {
        if (runtimeApiKey != null && !runtimeApiKey.trim().isEmpty()) {
            return runtimeApiKey.trim();
        }
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        String propKey = System.getProperty("gemini.api.key");
        if (propKey != null && !propKey.trim().isEmpty()) {
            return propKey.trim();
        }
        return null;
    }

    public void setRuntimeApiKey(String apiKey) {
        this.runtimeApiKey = apiKey;
    }

    public boolean isApiKeyConfigured() {
        String key = getApiKey();
        return key != null && !key.trim().isEmpty();
    }

    /**
     * Sends user query together with REAL database financial snapshot to Gemini API.
     * Runs asynchronously using CompletableFuture to keep JavaFX UI completely responsive.
     *
     * @param userQuery The student's question
     * @return CompletableFuture with AI answer string
     */
    public CompletableFuture<String> askAiAssistantAsync(String userQuery) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Step 1: Compile REAL financial data from MySQL database
                String financialContext = buildRealDatabaseFinancialContext();

                String apiKey = getApiKey();
                if (apiKey == null || apiKey.isEmpty()) {
                    // Provide intelligent offline financial analysis using real database records
                    return "[Offline Financial Analysis - Set GEMINI_API_KEY for live AI]\n\n" +
                           generateOfflineSmartAnalysis(userQuery, financialContext);
                }

                // Step 2: Build complete prompt for Gemini
                String fullPrompt = "You are SmartSpend AI, an encouraging and practical financial assistant for a college student.\n" +
                        "Below is the user's REAL, ACTUAL financial data directly from their database:\n\n" +
                        financialContext + "\n\n" +
                        "STUDENT'S QUESTION: " + userQuery + "\n\n" +
                        "INSTRUCTIONS:\n" +
                        "1. Answer strictly using their real numbers from above.\n" +
                        "2. Keep the answer friendly, concise (2-4 short paragraphs or bullet points).\n" +
                        "3. Highlight specific categories and amounts.\n" +
                        "4. Give 2 actionable, realistic money-saving tips tailored to a college student.";

                String requestJson = JsonUtil.buildGeminiRequest(fullPrompt);

                // Step 3: Send HTTP POST request to Gemini API
                String responseText = null;
                try {
                    URI uri = URI.create(GEMINI_API_URL + apiKey);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(uri)
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofSeconds(20))
                            .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                            .build();

                    HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        return JsonUtil.extractGeminiText(response.body());
                    } else if (response.statusCode() == 400 || response.statusCode() == 403) {
                        return "Gemini API Error (HTTP " + response.statusCode() + "): Invalid or unauthorized API key.\n" +
                               "Please check your GEMINI_API_KEY environment variable.\n\n" +
                               "Offline Analysis based on your data:\n" + generateOfflineSmartAnalysis(userQuery, financialContext);
                    } else if (response.statusCode() == 429) {
                        return "Gemini API rate limit reached. Please wait a moment.\n\n" +
                               "Offline Analysis based on your data:\n" + generateOfflineSmartAnalysis(userQuery, financialContext);
                    }
                } catch (Throwable t) {
                    System.err.println("[GeminiService] HttpClient notice: " + t.getMessage() + ". Trying HttpURLConnection fallback...");
                    responseText = sendViaHttpURLConnection(GEMINI_API_URL + apiKey, requestJson, userQuery, financialContext);
                    if (responseText != null) {
                        return responseText;
                    }
                }

                return generateOfflineSmartAnalysis(userQuery, financialContext);

            } catch (Exception e) {
                System.err.println("[GeminiService] Error contacting Gemini: " + e.getMessage());
                return "Network or Connection Notice: " + e.getMessage() + "\n\n" +
                       "Falling back to local financial analysis:\n" +
                       generateOfflineSmartAnalysis(userQuery, buildRealDatabaseFinancialContext());
            }
        });
    }

    private String sendViaHttpURLConnection(String targetUrl, String jsonBody, String userQuery, String financialContext) {
        try {
            java.net.URL url = java.net.URI.create(targetUrl).toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            java.io.InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (stream != null) {
                try (java.util.Scanner s = new java.util.Scanner(stream, java.nio.charset.StandardCharsets.UTF_8).useDelimiter("\\A")) {
                    String responseBody = s.hasNext() ? s.next() : "";
                    if (code == 200) {
                        return JsonUtil.extractGeminiText(responseBody);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[GeminiService] Fallback network notice: " + e.getMessage());
        }
        return generateOfflineSmartAnalysis(userQuery, financialContext);
    }

    /**
     * Compiles actual user financial numbers from database into structured text.
     */
    public String buildRealDatabaseFinancialContext() {
        String currentMonth = DateUtil.getCurrentYearMonth();
        double totalIncome = incomeService.getTotalIncomeForMonth(currentMonth);
        double totalExpense = expenseService.getTotalExpenseForMonth(currentMonth);
        double balance = totalIncome - totalExpense;
        List<CategorySummary> categories = expenseService.getCategorySummaries(currentMonth);
        List<Budget> budgets = budgetService.getBudgetsForMonth(currentMonth);
        List<Expense> recentExpenses = expenseService.getRecentExpenses(5);

        StringBuilder sb = new StringBuilder();
        sb.append("=== FINANCIAL SNAPSHOT FOR ").append(DateUtil.formatMonthHeader(currentMonth)).append(" ===\n");
        sb.append(String.format("- Total Income: $%.2f%n", totalIncome));
        sb.append(String.format("- Total Expenses: $%.2f%n", totalExpense));
        sb.append(String.format("- Net Savings / Remaining Balance: $%.2f%n", balance));

        sb.append("\n=== SPENDING BY CATEGORY ===\n");
        if (categories.isEmpty()) {
            sb.append("No expenses recorded for this month yet.\n");
        } else {
            for (CategorySummary cs : categories) {
                sb.append(String.format("- %s: $%.2f (%.1f%% of total spending)%n",
                        cs.getCategory(), cs.getTotalAmount(), cs.getPercentage()));
            }
        }

        sb.append("\n=== BUDGET STATUS ===\n");
        if (budgets.isEmpty()) {
            sb.append("No budgets set for this month.\n");
        } else {
            for (Budget b : budgets) {
                sb.append(String.format("- %s: Limit $%.2f | Spent $%.2f | %s%n",
                        b.getCategory(), b.getAmount(), b.getSpentAmount(),
                        b.isExceeded() ? "EXCEEDED by $" + String.format("%.2f", -b.getRemainingAmount()) :
                                "Remaining $" + String.format("%.2f", b.getRemainingAmount())));
            }
        }

        sb.append("\n=== RECENT TRANSACTIONS ===\n");
        if (recentExpenses.isEmpty()) {
            sb.append("No recent expenses.\n");
        } else {
            for (Expense exp : recentExpenses) {
                sb.append(String.format("- %s: $%.2f (%s) on %s%n",
                        exp.getCategory(), exp.getAmount(), exp.getDescription(), exp.getDate()));
            }
        }

        return sb.toString();
    }

    /**
     * Smart Rule-based analysis using actual DB figures when offline or during demo evaluation.
     */
    private String generateOfflineSmartAnalysis(String query, String context) {
        String lower = query.toLowerCase();
        String currentMonth = DateUtil.getCurrentYearMonth();
        double totalExpense = expenseService.getTotalExpenseForMonth(currentMonth);
        double totalIncome = incomeService.getTotalIncomeForMonth(currentMonth);
        double balance = totalIncome - totalExpense;
        List<CategorySummary> categories = expenseService.getCategorySummaries(currentMonth);

        CategorySummary topCategory = categories.isEmpty() ? null : categories.get(0);

        if (lower.contains("most") || lower.contains("highest") || lower.contains("where am i spending")) {
            if (topCategory != null) {
                return String.format(
                        "Based on your actual MySQL database records for this month:\n\n" +
                        "- Your highest spending category is **%s**, where you spent **$%.2f** (%.1f%% of your total expenses).\n" +
                        "- Total spending this month is **$%.2f**.\n\n" +
                        "Tip: Consider cooking at home or looking for student discounts in %s to bring this down!",
                        topCategory.getCategory(), topCategory.getTotalAmount(), topCategory.getPercentage(),
                        totalExpense, topCategory.getCategory());
            } else {
                return "You haven't recorded any expenses for this month yet! Start logging your spending to see insights.";
            }
        } else if (lower.contains("reduce") || lower.contains("saving tips") || lower.contains("save")) {
            return String.format(
                    "Here are personalized tips based on your real spending numbers:\n\n" +
                    "1. **Audit your %s spending**: You've spent $%.2f here already. Setting a weekly cap will save you at least $%.2f by month-end.\n" +
                    "2. **Track the small daily expenses**: Snacks and coffee add up quickly for college students.\n" +
                    "3. **Current balance**: You have $%.2f remaining from your total income of $%.2f. Aim to keep a 20%% emergency buffer ($%.2f)!",
                    (topCategory != null ? topCategory.getCategory() : "lifestyle"),
                    (topCategory != null ? topCategory.getTotalAmount() : 0.0),
                    (topCategory != null ? topCategory.getTotalAmount() * 0.25 : 50.0),
                    balance, totalIncome, totalIncome * 0.20);
        } else if (lower.contains("how much") || lower.contains("spend this month") || lower.contains("summary")) {
            return String.format(
                    "Here is your real financial summary for %s:\n\n" +
                    "- **Total Income**: $%.2f\n" +
                    "- **Total Expenses**: $%.2f\n" +
                    "- **Net Savings**: $%.2f\n" +
                    "- **Savings Rate**: %.1f%%\n\n" +
                    (balance >= 0 ? "You are operating with a positive cash flow! Keep it up." :
                            "Warning: You are spending more than your income this month. Review your budget immediately!"),
                    DateUtil.formatMonthHeader(currentMonth), totalIncome, totalExpense, balance,
                    (totalIncome > 0 ? (balance / totalIncome) * 100.0 : 0.0));
        }

        // Generic intelligent response
        return String.format(
                "Here is an overview of your actual finances from the database:\n\n" +
                "- This month you earned **$%.2f** and spent **$%.2f**.\n" +
                "- Remaining savings: **$%.2f**.\n" +
                "- Top expense: **%s** ($%.2f).\n\n" +
                "To connect full live conversational AI, set your GEMINI_API_KEY environment variable.\n" +
                "You can ask me: 'Where am I spending most?', 'How can I reduce expenses?', or 'Give me saving tips'!",
                totalIncome, totalExpense, balance,
                (topCategory != null ? topCategory.getCategory() : "None"),
                (topCategory != null ? topCategory.getTotalAmount() : 0.0));
    }
}
