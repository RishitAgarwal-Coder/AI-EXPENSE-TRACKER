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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * JavaFX View for User Registration.
 * Demonstrates:
 * 1. JavaFX Form layout
 * 2. Event Handling and input validation invocation
 */
public class RegisterView extends StackPane {

    private final AuthController authController;
    private final Runnable navigateToLogin;

    public RegisterView(AuthController authController, Runnable navigateToLogin) {
        this.authController = authController;
        this.navigateToLogin = navigateToLogin;
        initUI();
    }

    private void initUI() {
        setStyle("-fx-background-color: #0f172a;");

        VBox card = new VBox(14);
        card.setMaxWidth(440);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(32, 40, 32, 40));
        card.setStyle(
                "-fx-background-color: #ffffff;" +
                "-fx-background-radius: 16px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 18, 0, 0, 8);"
        );

        // Header
        Label appTitle = new Label("Create Account");
        appTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");

        Label subtitle = new Label("Join SmartSpend and start tracking your college budget!");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        VBox headerBox = new VBox(4, appTitle, subtitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 8, 0));

        // Form Fields
        Label nameLabel = new Label("Full Name");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. John Doe");

        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField emailField = new TextField();
        emailField.setPromptText("e.g. john@college.edu");

        Label passLabel = new Label("Password (min 6 characters)");
        passLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter a secure password");

        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Re-type your password");

        VBox formBox = new VBox(6, nameLabel, nameField, emailLabel, emailField, passLabel, passField, confirmLabel, confirmPassField);
        formBox.setAlignment(Pos.CENTER_LEFT);

        // Submit Button
        Button registerBtn = new Button("Register Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(42);
        registerBtn.setStyle("-fx-background-color: #4338ca; -fx-font-size: 14px; -fx-text-fill: white; -fx-background-radius: 8px; -fx-cursor: hand; -fx-font-weight: bold;");

        registerBtn.setOnAction(e -> {
            boolean success = authController.handleRegister(
                    nameField.getText(),
                    emailField.getText(),
                    passField.getText(),
                    confirmPassField.getText()
            );
            if (success && navigateToLogin != null) {
                navigateToLogin.run();
            }
        });

        // Back to Login link
        Label hasAccountLabel = new Label("Already have an account?");
        hasAccountLabel.setStyle("-fx-text-fill: #64748b;");

        Hyperlink loginLink = new Hyperlink("Sign In");
        loginLink.setStyle("-fx-text-fill: #4338ca; -fx-font-weight: bold; -fx-border-color: transparent;");
        loginLink.setOnAction(e -> {
            if (navigateToLogin != null) {
                navigateToLogin.run();
            }
        });

        HBox footerBox = new HBox(4, hasAccountLabel, loginLink);
        footerBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(headerBox, formBox, registerBtn, footerBox);
        getChildren().add(card);
        setAlignment(Pos.CENTER);
    }
}
