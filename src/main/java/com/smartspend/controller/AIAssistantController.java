package com.smartspend.controller;

import com.smartspend.service.GeminiService;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for the AI Expense Assistant.
 * Demonstrates:
 * 1. Asynchronous execution with CompletableFuture
 * 2. Real-time Gemini API integration
 * 3. Separation of AI communication logic
 */
public class AIAssistantController {

    private final GeminiService geminiService;

    public AIAssistantController() {
        this.geminiService = new GeminiService();
    }

    public CompletableFuture<String> askAi(String question) {
        return geminiService.askAiAssistantAsync(question);
    }

    public boolean isApiKeySet() {
        return geminiService.isApiKeyConfigured();
    }

    public void setRuntimeKey(String key) {
        geminiService.setRuntimeApiKey(key);
    }

    public GeminiService.Mode getLastMode() {
        return geminiService.getLastMode();
    }

    public String getLastModeDetail() {
        return geminiService.getLastModeDetail();
    }

    public void clearConversation() {
        geminiService.clearHistory();
    }

    public String getFinancialContextSummary() {
        return geminiService.buildRealDatabaseFinancialContext();
    }
}
