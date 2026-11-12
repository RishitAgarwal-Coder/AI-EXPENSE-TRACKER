package com.smartspend.service;

import com.smartspend.model.Budget;
import com.smartspend.model.CategorySummary;
import com.smartspend.model.Expense;
import com.smartspend.model.Income;
import com.smartspend.util.DateUtil;
import com.smartspend.util.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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

    /** Result of the last answer, so the UI can show which engine replied. */
    public enum Mode { LIVE, OFFLINE, ERROR }

    // Gemini REST endpoint. Models are tried in order; retired / overloaded ones are skipped.
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String[] GEMINI_MODELS = {"gemini-2.5-flash", "gemini-2.0-flash", "gemini-flash-latest"};

    // The key typed into the in-app dialog is remembered here between launches
    private static final Path CONFIG_FILE = Paths.get(System.getProperty("user.home"), ".smartspend", "ai.properties");

    // Number of previous chat messages sent along so follow-up questions keep their context
    private static final int MAX_HISTORY = 10;

    private HttpClient httpClient;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final BudgetService budgetService;

    private String runtimeApiKey;
    private String workingModel;
    private final List<String[]> history = new ArrayList<>();

    private volatile Mode lastMode = Mode.OFFLINE;
    private volatile String lastModeDetail = "";

    public GeminiService() {
        this.expenseService = new ExpenseService();
        this.incomeService = new IncomeService();
        this.budgetService = new BudgetService();
        this.runtimeApiKey = loadSavedKey();
    }

    private synchronized HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }
        return httpClient;
    }

    /**
     * Resolves the Gemini API Key from:
     * 1. Runtime override (typed in the in-app dialog, remembered in ~/.smartspend/ai.properties)
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

    /** Sets the key for this session and remembers it for the next launch (blank clears it). */
    public void setRuntimeApiKey(String apiKey) {
        this.runtimeApiKey = (apiKey == null || apiKey.isBlank()) ? null : apiKey.trim();
        this.workingModel = null;
        saveKey(this.runtimeApiKey);
    }

    public boolean isApiKeyConfigured() {
        String key = getApiKey();
        return key != null && !key.trim().isEmpty();
    }

    public Mode getLastMode() {
        return lastMode;
    }

    /** Short human-readable note about the last answer, e.g. the model name or why it went offline. */
    public String getLastModeDetail() {
        return lastModeDetail;
    }

    public synchronized void clearHistory() {
        history.clear();
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
            // Step 1: Compile REAL financial data from the database
            String financialContext = buildRealDatabaseFinancialContext();

            String apiKey = getApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                setMode(Mode.OFFLINE, "Offline smart analysis · add a Gemini API key for live AI");
                return generateOfflineSmartAnalysis(userQuery, financialContext);
            }

            // Step 2: Build the prompt (system instruction + recent conversation + new question)
            String systemInstruction = "You are SmartSpend AI, an encouraging and practical financial assistant for a college student.\n" +
                    "Below is the user's REAL, ACTUAL financial data directly from their database:\n\n" +
                    financialContext + "\n\n" +
                    "INSTRUCTIONS:\n" +
                    "1. Answer strictly using their real numbers from above. Never invent transactions.\n" +
                    "2. Use the same currency symbol as the data ($).\n" +
                    "3. Keep the answer friendly and concise (2-4 short paragraphs or bullet points).\n" +
                    "4. Highlight specific categories and amounts.\n" +
                    "5. When useful, give 2 actionable, realistic money-saving tips tailored to a college student.\n" +
                    "6. If the question is not about money, answer briefly and steer back to their finances.";

            List<String[]> turns;
            synchronized (this) {
                turns = new ArrayList<>(history);
            }
            turns.add(new String[]{"user", userQuery});
            String requestJson = JsonUtil.buildGeminiChatRequest(systemInstruction, turns);

            // Step 3: Send HTTP POST request to Gemini, trying the next model if one is retired
            String problem = null;
            List<String> models = new ArrayList<>();
            if (workingModel != null) models.add(workingModel);
            for (String m : GEMINI_MODELS) if (!models.contains(m)) models.add(m);

            for (String model : models) {
                int[] status = {0};
                String body;
                try {
                    body = post(GEMINI_BASE_URL + model + ":generateContent", apiKey, requestJson, status);
                } catch (Exception e) {
                    System.err.println("[GeminiService] Network error: " + e.getMessage());
                    problem = "No internet connection to Google Gemini";
                    break;
                }

                if (status[0] == 200) {
                    String answer = JsonUtil.extractGeminiText(body);
                    workingModel = model;
                    remember(userQuery, answer);
                    setMode(Mode.LIVE, "Live Gemini AI · " + model);
                    return answer;
                }

                String apiMessage = JsonUtil.extractGeminiText(body).replaceFirst("^AI Error: ", "");
                System.err.println("[GeminiService] " + model + " -> HTTP " + status[0] + ": " + apiMessage);
                if (status[0] == 404 || status[0] == 500 || status[0] == 503) {
                    problem = "Gemini model unavailable (HTTP " + status[0] + ")";
                    continue; // try the next model
                }
                String lowerMsg = apiMessage.toLowerCase(Locale.ROOT);
                if (lowerMsg.contains("leaked")) {
                    problem = "Google blocked this API key (reported as leaked). Create a new key at aistudio.google.com and click \"Set API Key\"";
                } else if (status[0] == 400 && lowerMsg.contains("api key")) {
                    problem = "Your Gemini API key is not valid. Click \"Set API Key\" to update it";
                } else if (status[0] == 400 || status[0] == 403) {
                    problem = "Gemini rejected the request: " + apiMessage;
                } else if (status[0] == 429) {
                    problem = "Gemini rate limit reached. Wait a minute and try again";
                } else {
                    problem = "Gemini error (HTTP " + status[0] + "): " + apiMessage;
                }
                break;
            }

            setMode(Mode.ERROR, problem == null ? "Gemini unavailable" : problem);
            return generateOfflineSmartAnalysis(userQuery, financialContext);
        });
    }

    /** POSTs JSON to Gemini with HttpClient, falling back to HttpURLConnection. Returns the body; status[0] = HTTP code. */
    private String post(String url, String apiKey, String json, int[] status) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            status[0] = response.statusCode();
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        } catch (IOException | RuntimeException e) {
            System.err.println("[GeminiService] HttpClient notice: " + e.getMessage() + ". Trying HttpURLConnection fallback...");
            return sendViaHttpURLConnection(url, apiKey, json, status);
        }
    }

    private String sendViaHttpURLConnection(String targetUrl, String apiKey, String jsonBody, int[] status) throws IOException {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) URI.create(targetUrl).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-goog-api-key", apiKey);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(45000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        status[0] = conn.getResponseCode();
        InputStream stream = (status[0] >= 200 && status[0] < 300) ? conn.getInputStream() : conn.getErrorStream();
        if (stream == null) return "";
        try (InputStream in = stream) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private synchronized void remember(String question, String answer) {
        history.add(new String[]{"user", question});
        history.add(new String[]{"model", answer});
        while (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    private void setMode(Mode mode, String detail) {
        this.lastMode = mode;
        this.lastModeDetail = detail;
    }

    private static String loadSavedKey() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                Properties p = new Properties();
                try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
                    p.load(in);
                }
                return p.getProperty("gemini.api.key");
            }
        } catch (IOException e) {
            System.err.println("[GeminiService] Could not read saved API key: " + e.getMessage());
        }
        return null;
    }

    private static void saveKey(String key) {
        try {
            if (key == null) {
                Files.deleteIfExists(CONFIG_FILE);
                return;
            }
            Files.createDirectories(CONFIG_FILE.getParent());
            Properties p = new Properties();
            p.setProperty("gemini.api.key", key);
            try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
                p.store(out, "SmartSpend AI settings");
            }
        } catch (IOException e) {
            System.err.println("[GeminiService] Could not save API key: " + e.getMessage());
        }
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
                sb.append(String.format("- %s: $%.2f (%.1f%% of total spending, %d transactions)%n",
                        cs.getCategory(), cs.getTotalAmount(), cs.getPercentage(), cs.getTransactionCount()));
            }
        }

        sb.append("\n=== INCOME BY SOURCE ===\n");
        Map<String, Double> sources = incomeBySource(currentMonth);
        if (sources.isEmpty()) {
            sb.append("No income recorded for this month yet.\n");
        } else {
            sources.forEach((src, amt) -> sb.append(String.format("- %s: $%.2f%n", src, amt)));
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

    private Map<String, Double> incomeBySource(String yearMonth) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Income inc : incomeService.getAllIncome()) {
            if (inc.getDate() != null && inc.getDate().format(DateUtil.MONTH_FORMATTER).equals(yearMonth)) {
                map.merge(inc.getSource(), inc.getAmount(), Double::sum);
            }
        }
        return map;
    }

    /**
     * Smart Rule-based analysis using actual DB figures when offline or during demo evaluation.
     */
    private String generateOfflineSmartAnalysis(String query, String context) {
        String lower = query.toLowerCase(Locale.ROOT).trim();
        String currentMonth = DateUtil.getCurrentYearMonth();
        double totalExpense = expenseService.getTotalExpenseForMonth(currentMonth);
        double totalIncome = incomeService.getTotalIncomeForMonth(currentMonth);
        double balance = totalIncome - totalExpense;
        List<CategorySummary> categories = expenseService.getCategorySummaries(currentMonth);

        CategorySummary topCategory = categories.isEmpty() ? null : categories.get(0);

        // Greeting
        if (lower.matches("^(hi|hello|hey|hii+|namaste|good (morning|afternoon|evening))\\b.*") && lower.length() < 30) {
            return String.format(
                    "Hi! 👋 This month you have earned **$%.2f** and spent **$%.2f**.\n\n" +
                    "Ask me where your money is going, how your budgets look, or for saving tips.",
                    totalIncome, totalExpense);
        }

        // A specific expense category, e.g. "How much did I spend on food?"
        for (String cat : Expense.CATEGORIES) {
            if (!cat.equalsIgnoreCase("Other") && lower.contains(cat.toLowerCase(Locale.ROOT))) {
                CategorySummary match = categories.stream()
                        .filter(c -> c.getCategory().equalsIgnoreCase(cat)).findFirst().orElse(null);
                Budget budget = budgetService.getBudgetsForMonth(currentMonth).stream()
                        .filter(b -> b.getCategory().equalsIgnoreCase(cat)).findFirst().orElse(null);
                StringBuilder sb = new StringBuilder();
                if (match == null) {
                    sb.append(String.format("You haven't spent anything on **%s** this month. Nice! 🎉", cat));
                } else {
                    sb.append(String.format(
                            "This month you spent **$%.2f** on **%s** across %d transaction%s (%.1f%% of your total spending).",
                            match.getTotalAmount(), cat, match.getTransactionCount(),
                            match.getTransactionCount() == 1 ? "" : "s", match.getPercentage()));
                }
                if (budget != null) {
                    sb.append(String.format("\n\n- Budget: **$%.2f**\n- %s",
                            budget.getAmount(),
                            budget.isExceeded()
                                    ? String.format("Over budget by **$%.2f**. Try to pause %s spending for a while.", -budget.getRemainingAmount(), cat)
                                    : String.format("Remaining: **$%.2f** (%.0f%% used)", budget.getRemainingAmount(), budget.getPercentageSpent())));
                } else if (match != null) {
                    sb.append(String.format("\n\nTip: set a monthly budget for %s in the Budgets tab to keep it in check.", cat));
                }
                return sb.toString();
            }
        }

        if (lower.contains("budget") || lower.contains("limit") || lower.contains("exceed") || lower.contains("over spend")
                || lower.contains("overspend")) {
            List<Budget> budgets = budgetService.getBudgetsForMonth(currentMonth);
            if (budgets.isEmpty()) {
                return "You haven't set any budgets for this month yet. Open the **Monthly Budgets** tab to set a limit per category.";
            }
            StringBuilder sb = new StringBuilder("Here is your budget status for " + DateUtil.formatMonthHeader(currentMonth) + ":\n\n");
            int exceeded = 0;
            for (Budget b : budgets) {
                String state = b.isExceeded() ? "⚠ EXCEEDED" : b.isNearLimit() ? "⚠ Near limit" : "✓ Safe";
                if (b.isExceeded()) exceeded++;
                sb.append(String.format("- **%s**: $%.2f of $%.2f (%.0f%%) %s%n",
                        b.getCategory(), b.getSpentAmount(), b.getAmount(), b.getPercentageSpent(), state));
            }
            sb.append(exceeded == 0 ? "\nAll budgets are under control. Keep it up!"
                    : String.format("%n%d budget%s exceeded. Cut back there first.", exceeded, exceeded == 1 ? " is" : "s are"));
            return sb.toString();
        }

        if (lower.contains("income") || lower.contains("earn") || lower.contains("salary") || lower.contains("stipend")) {
            Map<String, Double> sources = incomeBySource(currentMonth);
            if (sources.isEmpty()) {
                return "No income has been recorded for this month yet. Add it in the **Income** tab.";
            }
            StringBuilder sb = new StringBuilder(String.format("This month you earned **$%.2f**:%n%n", totalIncome));
            sources.forEach((src, amt) -> sb.append(String.format("- %s: $%.2f%n", src, amt)));
            sb.append(String.format("%nAfter expenses you have **$%.2f** left.", balance));
            return sb.toString();
        }

        if (lower.contains("recent") || lower.contains("last") || lower.contains("latest") || lower.contains("transaction")) {
            List<Expense> recent = expenseService.getRecentExpenses(5);
            if (recent.isEmpty()) {
                return "You have no expenses recorded yet.";
            }
            StringBuilder sb = new StringBuilder("Your latest expenses:\n\n");
            for (Expense e : recent) {
                sb.append(String.format("- %s · **%s** $%.2f (%s)%n",
                        DateUtil.formatDisplay(e.getDate()), e.getCategory(), e.getAmount(), e.getDescription()));
            }
            return sb.toString();
        }

        if (lower.contains("most") || lower.contains("highest") || lower.contains("where am i spending")
                || lower.contains("where did my money") || lower.contains("biggest")) {
            if (topCategory != null) {
                return String.format(
                        "Based on your actual database records for this month:\n\n" +
                        "- Your highest spending category is **%s**, where you spent **$%.2f** (%.1f%% of your total expenses).\n" +
                        "- Total spending this month is **$%.2f**.\n\n" +
                        "Tip: Consider cooking at home or looking for student discounts in %s to bring this down!",
                        topCategory.getCategory(), topCategory.getTotalAmount(), topCategory.getPercentage(),
                        totalExpense, topCategory.getCategory());
            } else {
                return "You haven't recorded any expenses for this month yet! Start logging your spending to see insights.";
            }
        } else if (lower.contains("reduce") || lower.contains("tip") || lower.contains("save") || lower.contains("saving")
                || lower.contains("cut")) {
            return String.format(
                    "Here are personalized tips based on your real spending numbers:\n\n" +
                    "1. **Audit your %s spending**: You've spent $%.2f here already. Setting a weekly cap will save you at least $%.2f by month-end.\n" +
                    "2. **Track the small daily expenses**: Snacks and coffee add up quickly for college students.\n" +
                    "3. **Current balance**: You have $%.2f remaining from your total income of $%.2f. Aim to keep a 20%% emergency buffer ($%.2f)!",
                    (topCategory != null ? topCategory.getCategory() : "lifestyle"),
                    (topCategory != null ? topCategory.getTotalAmount() : 0.0),
                    (topCategory != null ? topCategory.getTotalAmount() * 0.25 : 50.0),
                    balance, totalIncome, totalIncome * 0.20);
        } else if (lower.contains("how much") || lower.contains("spend this month") || lower.contains("summary")
                || lower.contains("balance") || lower.contains("overview")) {
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
                "You can ask me: 'Where am I spending most?', 'How much did I spend on food?', " +
                "'How are my budgets?' or 'Give me saving tips'!",
                totalIncome, totalExpense, balance,
                (topCategory != null ? topCategory.getCategory() : "None"),
                (topCategory != null ? topCategory.getTotalAmount() : 0.0));
    }
}
