package com.smartspend.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Optional;

/**
 * Utility helper class for consistent JavaFX alert dialogs.
 */
public class AlertHelper {

    public static void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void styleDialog(Alert alert) {
        applyTheme(alert.getDialogPane());
    }

    /** Gives any dialog the same look as the rest of the app (fonts, buttons, fields). */
    public static void applyTheme(DialogPane dialogPane) {
        dialogPane.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 13px;");
        java.net.URL css = AlertHelper.class.getResource("/styles/style.css");
        if (css != null && !dialogPane.getStylesheets().contains(css.toExternalForm())) {
            dialogPane.getStylesheets().add(css.toExternalForm());
        }
        dialogPane.getStyleClass().add("app-dialog");
    }
}
