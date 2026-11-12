package com.smartspend.view;

import com.smartspend.controller.AIAssistantController;
import com.smartspend.service.GeminiService;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
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
    private Label loadingLabel;
    private Timeline loadingAnimation;
    private Label apiKeyStatusLabel;
    private final List<Button> chips = new ArrayList<>();

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
        header.getStyleClass().add("ai-header");

        Label aiIcon = new Label("🤖");
        aiIcon.getStyleClass().add("ai-header-icon");

        VBox titleBox = new VBox(2);
        Label title = new Label("SmartSpend AI Financial Advisor");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");

        apiKeyStatusLabel = new Label();
        apiKeyStatusLabel.setWrapText(true);
        apiKeyStatusLabel.setMaxWidth(560);
        updateStatusLabel();
        titleBox.getChildren().addAll(title, apiKeyStatusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button setKeyBtn = new Button("🔑 Set API Key");
        setKeyBtn.getStyleClass().add("btn-secondary");
        setKeyBtn.setOnAction(e -> handleSetApiKey());

        header.getChildren().addAll(aiIcon, titleBox, spacer, setKeyBtn);

        // 2. Chat Conversation History (ScrollPane)
        chatBox = new VBox(14);
        chatBox.setPadding(new Insets(18));
        chatBox.setAlignment(Pos.TOP_LEFT);

        chatScrollPane = new ScrollPane(chatBox);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setPrefHeight(450);
        chatScrollPane.getStyleClass().add("chat-panel");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        // Keep the newest message in view whenever the conversation grows
        chatBox.heightProperty().addListener((obs, oldH, newH) -> chatScrollPane.setVvalue(1.0));

        // Initial AI Greeting message
        addAiMessage("👋 Hello! I am your SmartSpend AI Expense Assistant.\n\n" +
                     "I have direct access to your actual expense, income, and budget data in MySQL. " +
                     "Feel free to ask me anything about your spending habits, highest categories, or how to save money as a college student!",
                     null);

        // Loading indicator bar (hidden by default)
        loadingBox = new HBox(10);
        loadingBox.setAlignment(Pos.CENTER_LEFT);
        loadingBox.setPadding(new Insets(4, 12, 4, 12));
        loadingLabel = new Label("AI is analyzing your actual transactions");
        loadingLabel.getStyleClass().add("ai-typing");
        loadingBox.getChildren().addAll(loadingLabel);
        loadingBox.setVisible(false);
        loadingBox.setManaged(false);

        String base = loadingLabel.getText();
        loadingAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, e -> loadingLabel.setText(base + " .")),
                new KeyFrame(Duration.millis(400), e -> loadingLabel.setText(base + " . .")),
                new KeyFrame(Duration.millis(800), e -> loadingLabel.setText(base + " . . .")),
                new KeyFrame(Duration.millis(1200)));
        loadingAnimation.setCycleCount(Timeline.INDEFINITE);

        // 3. Quick Prompt Chips (wrap onto a second line instead of being cut off)
        FlowPane chipsBox = new FlowPane(8, 8);
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
            chip.getStyleClass().add("chip");
            chip.setOnAction(e -> {
                inputField.setText(q);
                handleSend();
            });
            chips.add(chip);
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
        sendBtn.disableProperty().bind(inputField.textProperty().isEmpty().or(inputField.disabledProperty()));

        inputBox.getChildren().addAll(inputField, sendBtn);

        getChildren().addAll(header, chatScrollPane, loadingBox, chipsBox, inputBox);
    }

    private void handleSend() {
        String question = inputField.getText();
        if (question == null || question.trim().isEmpty() || inputField.isDisabled()) {
            return;
        }

        inputField.clear();
        addUserMessage(question.trim());
        setBusy(true);

        // Call AI Asynchronously (Java 21 HttpClient CompletableFuture)
        controller.askAi(question.trim()).thenAccept(aiAnswer -> Platform.runLater(() -> {
            setBusy(false);
            addAiMessage(aiAnswer, controller.getLastModeDetail());
            updateStatusLabel();
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                setBusy(false);
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                addAiMessage("Sorry, something went wrong while reading your data: " + cause.getMessage(), null);
            });
            return null;
        });
    }

    private void setBusy(boolean busy) {
        loadingBox.setVisible(busy);
        loadingBox.setManaged(busy);
        inputField.setDisable(busy);
        chips.forEach(c -> c.setDisable(busy));
        if (busy) {
            loadingAnimation.play();
        } else {
            loadingAnimation.stop();
            inputField.requestFocus();
        }
    }

    private void addUserMessage(String message) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_RIGHT);

        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().add("bubble-user");
        label.maxWidthProperty().bind(chatScrollPane.widthProperty().multiply(0.7));

        container.getChildren().add(label);
        appendAnimated(container);
    }

    private void addAiMessage(String message, String caption) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.TOP_LEFT);

        Label avatar = new Label("🤖");
        avatar.getStyleClass().add("ai-avatar");

        TextFlow bubble = renderMarkdown(message);
        bubble.getStyleClass().add("bubble-ai");
        bubble.maxWidthProperty().bind(chatScrollPane.widthProperty().multiply(0.75));

        VBox column = new VBox(4, bubble);
        if (caption != null && !caption.isBlank()) {
            Label meta = new Label(caption);
            meta.getStyleClass().add("bubble-meta");
            column.getChildren().add(meta);
        }

        container.getChildren().addAll(avatar, column);
        appendAnimated(container);
    }

    /** Renders the small Markdown subset Gemini uses (**bold**, bullets, headings) into a TextFlow. */
    private TextFlow renderMarkdown(String message) {
        TextFlow flow = new TextFlow();
        flow.setLineSpacing(3);
        String[] lines = (message == null ? "" : message).replace("\r", "").split("\n", -1);
        for (int li = 0; li < lines.length; li++) {
            String line = lines[li];
            boolean heading = false;
            if (line.matches("^#{1,6}\\s+.*")) {
                line = line.replaceFirst("^#{1,6}\\s+", "");
                heading = true;
            } else if (line.matches("^\\s*[*-]\\s+.*")) {
                line = line.replaceFirst("^(\\s*)[*-]\\s+", "$1•  ");
            }
            String[] parts = line.split("\\*\\*", -1);
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].isEmpty()) continue;
                Text t = new Text(parts[i].replaceAll("(?<![*\\w])\\*(?!\\s)([^*]+?)\\*(?![*\\w])", "$1"));
                t.getStyleClass().add(heading || i % 2 == 1 ? "bubble-text-bold" : "bubble-text");
                flow.getChildren().add(t);
            }
            if (li < lines.length - 1) {
                flow.getChildren().add(new Text("\n"));
            }
        }
        return flow;
    }

    private void appendAnimated(Node node) {
        node.setOpacity(0);
        chatBox.getChildren().add(node);

        FadeTransition fade = new FadeTransition(Duration.millis(220), node);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), node);
        slide.setFromY(8);
        slide.setToY(0);
        fade.play();
        slide.play();
    }

    private void updateStatusLabel() {
        apiKeyStatusLabel.getStyleClass().removeAll("status-live", "status-offline", "status-error");
        if (!controller.isApiKeySet()) {
            apiKeyStatusLabel.setText("● Offline Smart Mode (Set GEMINI_API_KEY for Live AI)");
            apiKeyStatusLabel.getStyleClass().add("status-offline");
        } else if (controller.getLastMode() == GeminiService.Mode.ERROR) {
            apiKeyStatusLabel.setText("● " + controller.getLastModeDetail() + " (showing offline analysis)");
            apiKeyStatusLabel.getStyleClass().add("status-error");
        } else {
            apiKeyStatusLabel.setText("● Live Gemini API Active");
            apiKeyStatusLabel.getStyleClass().add("status-live");
        }
    }

    private void handleSetApiKey() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Gemini API Key Configuration");
        dialog.setHeaderText("Set Gemini API Key for SmartSpend");
        dialog.setContentText("Enter your Google Gemini API Key:\n(Or leave blank to keep current system setting)");
        dialog.getEditor().setPrefWidth(320);
        dialog.getEditor().setPromptText("AIza...");
        com.smartspend.util.AlertHelper.applyTheme(dialog.getDialogPane());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(key -> {
            if (!key.trim().isEmpty()) {
                controller.setRuntimeKey(key.trim());
                controller.clearConversation();
                updateStatusLabel();
                addAiMessage("✅ Gemini API Key saved! It will be remembered next time you open SmartSpend. " +
                        "Ask me anything to test the live connection.", null);
            }
        });
    }
}
