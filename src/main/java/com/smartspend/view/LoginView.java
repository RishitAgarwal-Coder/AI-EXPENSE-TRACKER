package com.smartspend.view;

import com.smartspend.controller.AuthController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * JavaFX View for User Login.
 * Demonstrates:
 * 1. JavaFX layout management (StackPane, VBox, HBox)
 * 2. Event Handling (Button onAction, KeyPressed)
 * 3. Modern Dark Blue & Purple Theme styling
 */
public class LoginView extends StackPane {

    private final AuthController authController;
    private final Runnable navigateToRegister;

    public LoginView(AuthController authController, Runnable navigateToRegister) {
        this.authController = authController;
        this.navigateToRegister = navigateToRegister;
        initUI();
    }

    private void initUI() {
        setStyle("-fx-background-color: #0f172a;");

        // Login Card Container
        VBox card = new VBox(16);
        card.setMaxWidth(420);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 40, 36, 40));
        card.setStyle(
                "-fx-background-color: #ffffff;" +
                "-fx-background-radius: 16px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 18, 0, 0, 8);"
        );

        // Header / Logo
        Label logoIcon = new Label("💰");
        logoIcon.setStyle("-fx-font-size: 38px;");

        Label appTitle = new Label("SmartSpend");
        appTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");

        Label subtitle = new Label("AI-Integrated Student Expense Tracker");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        VBox headerBox = new VBox(4, logoIcon, appTitle, subtitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        // Form Fields
        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField emailField = new TextField();
        emailField.setPromptText("e.g. demo@smartspend.com");
        emailField.setText("demo@smartspend.com"); // Pre-filled for quick demo

        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setText("password123"); // Pre-filled for quick demo

        VBox formBox = new VBox(8, emailLabel, emailField, passLabel, passField);
        formBox.setAlignment(Pos.CENTER_LEFT);

        // Login Button
        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(42);
        loginBtn.setStyle("-fx-background-color: #4338ca; -fx-font-size: 14px; -fx-text-fill: white; -fx-background-radius: 8px; -fx-cursor: hand;");

        loginBtn.setOnAction(e -> {
            authController.handleLogin(emailField.getText(), passField.getText());
        });

        // Enter key trigger
        passField.setOnAction(e -> loginBtn.fire());
        emailField.setOnAction(e -> passField.requestFocus());

        // Switch to Registration
        Label noAccountLabel = new Label("Don't have an account?");
        noAccountLabel.setStyle("-fx-text-fill: #64748b;");

        Hyperlink registerLink = new Hyperlink("Sign Up");
        registerLink.setStyle("-fx-text-fill: #4338ca; -fx-font-weight: bold; -fx-border-color: transparent;");
        registerLink.setOnAction(e -> {
            if (navigateToRegister != null) {
                navigateToRegister.run();
            }
        });

        HBox footerBox = new HBox(4, noAccountLabel, registerLink);
        footerBox.setAlignment(Pos.CENTER);

        // Demo hint pill
        Label demoHint = new Label("💡 Demo Login: demo@smartspend.com / password123");
        demoHint.setStyle(
                "-fx-background-color: #ede9fe;" +
                "-fx-text-fill: #5b21b6;" +
                "-fx-font-size: 11px;" +
                "-fx-padding: 6px 12px;" +
                "-fx-background-radius: 12px;"
        );

        card.getChildren().addAll(headerBox, formBox, loginBtn, footerBox, demoHint);
        getChildren().add(card);
        setAlignment(Pos.CENTER);
    }
}
