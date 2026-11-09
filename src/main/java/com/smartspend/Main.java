package com.smartspend;

import com.smartspend.controller.AuthController;
import com.smartspend.database.DatabaseConnection;
import com.smartspend.database.DatabaseInitializer;
import com.smartspend.view.LoginView;
import com.smartspend.view.MainView;
import com.smartspend.view.RegisterView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main Application Entry Point for SmartSpend - AI Expense Tracker.
 * Demonstrates:
 * 1. JavaFX Application Lifecycle (init, start, stop)
 * 2. Scene switching (Login -> Register -> Main Application)
 * 3. Database bootstrap and graceful resource cleanup
 */
public class Main extends Application {

    private Stage primaryStage;
    private AuthController authController;

    @Override
    public void init() {
        // Initialize database tables and seed sample demo data
        System.out.println("[Main] Bootstrapping SmartSpend Database...");
        DatabaseInitializer.initializeDatabase();
        authController = new AuthController();
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("SmartSpend – AI Expense Tracker");
        this.primaryStage.setMinWidth(1080);
        this.primaryStage.setMinHeight(680);

        // Setup login callback to navigate to dashboard
        authController.setOnLoginSuccess(user -> showMainView());

        // Show initial login screen
        showLoginView();

        primaryStage.show();
    }

    public void showLoginView() {
        LoginView loginView = new LoginView(authController, this::showRegisterView);
        Scene scene = new Scene(loginView, 1100, 720);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    public void showRegisterView() {
        RegisterView registerView = new RegisterView(authController, this::showLoginView);
        Scene scene = new Scene(registerView, 1100, 720);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    public void showMainView() {
        MainView mainView = new MainView(this::showLoginView);
        Scene scene = new Scene(mainView, 1180, 760);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    private void applyStylesheet(Scene scene) {
        URL cssUrl = getClass().getResource("/styles/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("[Main] Notice: style.css not found on classpath.");
        }
    }

    @Override
    public void stop() {
        System.out.println("[Main] Shutting down SmartSpend application...");
        DatabaseConnection.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
