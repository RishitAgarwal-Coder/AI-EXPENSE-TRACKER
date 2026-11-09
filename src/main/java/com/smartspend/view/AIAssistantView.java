package com.smartspend.view;

import com.smartspend.controller.AIAssistantController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * JavaFX View for the AI Expense Assistant.
 * Demonstrates:
 * 1. Interactive Chat UI with custom bubble rendering
 * 2. Asynchronous API consumption with Platform.runLater
 * 3. Loading indicators during HTTP network calls
 * 4. Quick prompt suggestions
 */
public class AIAssistantView extends VBox {

    private final AIAssistantController controller;

    private VBox chatBox;
    private ScrollPane chatScrollPane;
    private TextField inputField;
    private Button sendBtn;
    private HBox loadingBox;
    private Label apiKeyStatusLabel;

    public AIAssistantView(AIAssistantController controller) {
        this.controller = controller;
        setSpacing(14);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");
        initUI();
    }

    private void initUI() {
        // 1. Header Bar
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setStyle(
                "-fx-background-color: #ffffff;" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 12px;"
        );

        Label aiIcon = new Label("🤖");
        aiIcon.setStyle("-fx-font-size: 26px;");

        VBox titleBox = new VBox(2);
        Label title = new Label("SmartSpend AI Financial Advisor");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");

        apiKeyStatusLabel = new Label(controller.isApiKeySet() ? "\u25CF Live Gemini API Active" : "\u25CF Offline Smart Mode (Set GEMINI_API_KEY for Live AI)");
        apiKeyStatusLabel.setStyle(controller.isApiKeySet() ? "-fx-font-size: 12px; -fx-text-fill: #16a34a; -fx-font-weight: bold;" : "-fx-font-size: 12px; -fx-text-fill: #d97706;");
        titleBox.getChildren().addAll(title, apiKeyStatusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button setKeyBtn = new Button("🔑 Set API Key");
        setKeyBtn.getStyleClass().add("btn-secondary");
        setKeyBtn.setOnAction(e -> handleSetApiKey());

        header.getChildren().addAll(aiIcon, titleBox, spacer, setKeyBtn);

        // 2. Chat Conversation History (ScrollPane)
        chatBox = new VBox(14);
        chatBox.setPadding(new Insets(16));
        chatBox.setAlignment(Pos.TOP_LEFT);

        chatScrollPane = new ScrollPane(chatBox);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setPrefHeight(450);
        chatScrollPane.setStyle("-fx-background-color: #ffffff; -fx-background: #ffffff; -fx-background-radius: 12px; -fx-border-color: #e2e8f0; -fx-border-radius: 12px;");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        // Initial AI Greeting message
        addAiMessage("👋 Hello! I am your SmartSpend AI Expense Assistant.\n\n" +
                     "I have direct access to your actual expense, income, and budget data in MySQL. " +
                     "Feel free to ask me anything about your spending habits, highest categories, or how to save money as a college student!");

        // Loading indicator bar (hidden by default)
        loadingBox = new HBox(10);
        loadingBox.setAlignment(Pos.CENTER_LEFT);
        loadingBox.setPadding(new Insets(6, 12, 6, 12));
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(20, 20);
        Label loadingLabel = new Label("AI is analyzing your actual transactions...");
        loadingLabel.setStyle("-fx-text-fill: #6366f1; -fx-font-weight: bold; -fx-font-size: 12px;");
        loadingBox.getChildren().addAll(progress, loadingLabel);
        loadingBox.setVisible(false);
        loadingBox.setManaged(false);

        // 3. Quick Prompt Chips
        HBox chipsBox = new HBox(8);
        chipsBox.setAlignment(Pos.CENTER_LEFT);

        String[] quickPrompts = {
                "Where am I spending most of my money?",
                "How can I reduce my expenses?",
                "How much did I spend this month?",
                "Give me some saving tips.",
                "Which category has the highest expenses?"
        };

        for (String q : quickPrompts) {
            Button chip = new Button(q);
            chip.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #4338ca; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-background-radius: 14px; -fx-cursor: hand;");
            chip.setOnAction(e -> {
                inputField.setText(q);
                handleSend();
            });
            chipsBox.getChildren().add(chip);
        }

        // 4. Input Box
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);

        inputField = new TextField();
        inputField.setPromptText("Type your question here (e.g. Where did my money go this month?)...");
        inputField.setPrefHeight(42);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setOnAction(e -> handleSend());

        sendBtn = new Button("Send 🚀");
        sendBtn.getStyleClass().add("btn-primary");
        sendBtn.setPrefHeight(42);
        sendBtn.setOnAction(e -> handleSend());

        inputBox.getChildren().addAll(inputField, sendBtn);

        getChildren().addAll(header, chatScrollPane, loadingBox, chipsBox, inputBox);
    }

    private void handleSend() {
        String question = inputField.getText();
        if (question == null || question.trim().isEmpty()) {
            return;
        }

        inputField.clear();
        addUserMessage(question);

        // Show loading
        loadingBox.setVisible(true);
        loadingBox.setManaged(true);
        sendBtn.setDisable(true);
        inputField.setDisable(true);

        // Scroll to bottom
        chatScrollPane.setVvalue(1.0);

        // Call AI Asynchronously (Java 21 HttpClient CompletableFuture)
        controller.askAi(question).thenAccept(aiAnswer -> {
            Platform.runLater(() -> {
                loadingBox.setVisible(false);
                loadingBox.setManaged(false);
                sendBtn.setDisable(false);
                inputField.setDisable(false);
                inputField.requestFocus();

                addAiMessage(aiAnswer);
                chatScrollPane.setVvalue(1.0);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                loadingBox.setVisible(false);
                loadingBox.setManaged(false);
                sendBtn.setDisable(false);
                inputField.setDisable(false);

                addAiMessage("Error contacting AI Assistant: " + ex.getMessage());
                chatScrollPane.setVvalue(1.0);
            });
            return null;
        });
    }

    private void addUserMessage(String message) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_RIGHT);

        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().add("bubble-user");

        container.getChildren().add(label);
        chatBox.getChildren().add(container);
    }

    private void addAiMessage(String message) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.TOP_LEFT);

        Label avatar = new Label("🤖");
        avatar.setStyle("-fx-font-size: 20px; -fx-padding: 4px 0 0 0;");

        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().add("bubble-ai");

        container.getChildren().addAll(avatar, label);
        chatBox.getChildren().add(container);
    }

    private void handleSetApiKey() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Gemini API Key Configuration");
        dialog.setHeaderText("Set Gemini API Key for SmartSpend");
        dialog.setContentText("Enter your Google Gemini API Key:\n(Or leave blank to keep current system setting)");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(key -> {
            if (!key.trim().isEmpty()) {
                controller.setRuntimeKey(key.trim());
                apiKeyStatusLabel.setText("\u25CF Live Gemini API Active");
                apiKeyStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
                addAiMessage("✅ Gemini API Key updated for this session! You are now connected to Google Gemini.");
            }
        });
    }
}
