package com.smartspend.view;

import com.smartspend.controller.BudgetController;
import com.smartspend.model.Budget;
import com.smartspend.model.Expense;
import com.smartspend.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * JavaFX View for Monthly Budget Management.
 * Demonstrates:
 * 1. Progress indicators and status badges
 * 2. Visual budget warnings (Safe, Near Limit, Exceeded)
 * 3. TableView with custom ProgressBar cell rendering
 */
public class BudgetView extends ScrollPane {

    private final BudgetController controller;

    private ComboBox<String> categoryBox;
    private TextField amountField;
    private TableView<Budget> budgetTable;

    private Label totalBudgetLabel;
    private Label totalSpentLabel;
    private Label exceededCountLabel;

    private String activeMonth;

    public BudgetView(BudgetController controller) {
        this.controller = controller;
        this.activeMonth = DateUtil.getCurrentYearMonth();
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        initUI();
        loadBudgetData();
    }

    private void initUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        // 1. Header with KPI badges
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Monthly Budgets");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");
        Label subtitle = new Label("Active Budget Month: " + DateUtil.formatMonthHeader(activeMonth));
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalBudgetLabel = new Label("$0.00");
        totalBudgetLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4338ca;");
        VBox bgtKpi = new VBox(2, kpiCaption("Total Budget:"), totalBudgetLabel);
        bgtKpi.setStyle("-fx-background-color: #ede9fe; -fx-padding: 8px 16px; -fx-background-radius: 8px;");

        exceededCountLabel = new Label("0");
        exceededCountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        VBox alertKpi = new VBox(2, kpiCaption("Over Budget:"), exceededCountLabel);
        alertKpi.setStyle("-fx-background-color: #fee2e2; -fx-padding: 8px 16px; -fx-background-radius: 8px;");

        header.getChildren().addAll(titleBox, spacer, bgtKpi, alertKpi);

        // 2. Set Budget Form Card
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("content-card");

        Label formTitle = new Label("Set Category Budget");
        formTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(12);

        categoryBox = new ComboBox<>(FXCollections.observableArrayList(Expense.CATEGORIES));
        categoryBox.setPromptText("Select Category");
        categoryBox.setPrefWidth(220);

        amountField = new TextField();
        amountField.setPromptText("e.g. 300.00");
        amountField.setPrefWidth(180);

        Button setBtn = new Button("🎯 Set / Update Budget");
        setBtn.getStyleClass().add("btn-primary");
        setBtn.setOnAction(e -> handleSetBudget());

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> handleDeleteBudget());

        formGrid.add(new Label("Category:"), 0, 0);
        formGrid.add(categoryBox, 0, 1);
        formGrid.add(new Label("Monthly Limit ($):"), 1, 0);
        formGrid.add(amountField, 1, 1);
        formGrid.add(new Label("Actions:"), 2, 0);

        HBox btnBox = new HBox(10, setBtn, deleteBtn);
        formGrid.add(btnBox, 2, 1);

        formCard.getChildren().addAll(formTitle, formGrid);

        // 3. Budgets TableView with custom progress bar rendering
        budgetTable = createTable();
        budgetTable.setPrefHeight(380);

        root.getChildren().addAll(header, formCard, budgetTable);
        setContent(root);
    }

    private TableView<Budget> createTable() {
        TableView<Budget> table = new TableView<>();
        table.setPlaceholder(emptyLabel("No budgets set for this month yet. Pick a category above to set one."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Budget, String> catCol = new TableColumn<>("Category");
        catCol.setPrefWidth(140);
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<Budget, String> limitCol = new TableColumn<>("Budget Limit");
        limitCol.setPrefWidth(110);
        limitCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getAmount())));

        TableColumn<Budget, String> spentCol = new TableColumn<>("Spent So Far");
        spentCol.setPrefWidth(110);
        spentCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getSpentAmount())));

        TableColumn<Budget, String> remCol = new TableColumn<>("Remaining");
        remCol.setPrefWidth(110);
        remCol.setCellValueFactory(data -> {
            Budget b = data.getValue();
            return new SimpleStringProperty(String.format("$%.2f", b.getRemainingAmount()));
        });
        remCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Budget b = getTableView().getItems().get(getIndex());
                    if (b.isExceeded()) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Visual Progress Bar Column
        TableColumn<Budget, Void> progCol = new TableColumn<>("Usage %");
        progCol.setPrefWidth(160);
        progCol.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar();
            private final Label pctLabel = new Label();
            private final HBox pane = new HBox(8, bar, pctLabel);

            {
                pane.setAlignment(Pos.CENTER_LEFT);
                bar.setPrefWidth(90);
                pctLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Budget b = getTableView().getItems().get(getIndex());
                    double pct = b.getPercentageSpent() / 100.0;
                    bar.setProgress(Math.min(1.0, pct));
                    pctLabel.setText(String.format("%.0f%%", b.getPercentageSpent()));

                    if (b.isExceeded()) {
                        bar.setStyle("-fx-accent: #dc2626;");
                        pctLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else if (b.isNearLimit()) {
                        bar.setStyle("-fx-accent: #d97706;");
                        pctLabel.setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    } else {
                        bar.setStyle("-fx-accent: #10b981;");
                        pctLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    }
                    setGraphic(pane);
                }
            }
        });

        // Status Badge Column
        TableColumn<Budget, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(130);
        statusCol.setCellValueFactory(data -> {
            Budget b = data.getValue();
            if (b.isExceeded()) return new SimpleStringProperty("EXCEEDED [!]");
            if (b.isNearLimit()) return new SimpleStringProperty("NEAR LIMIT [!]");
            return new SimpleStringProperty("SAFE [OK]");
        });
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if (item.contains("EXCEEDED")) {
                        badge.getStyleClass().add("badge-danger");
                    } else if (item.contains("NEAR LIMIT")) {
                        badge.getStyleClass().add("badge-warning");
                    } else {
                        badge.getStyleClass().add("badge-ok");
                    }
                    setGraphic(badge);
                }
            }
        });

        table.getColumns().addAll(catCol, limitCol, spentCol, remCol, progCol, statusCol);
        return table;
    }

    private void handleSetBudget() {
        boolean ok = controller.setBudget(
                categoryBox.getValue(),
                activeMonth,
                amountField.getText()
        );
        if (ok) {
            amountField.clear();
            categoryBox.setValue(null);
            loadBudgetData();
        }
    }

    private void handleDeleteBudget() {
        Budget b = budgetTable.getSelectionModel().getSelectedItem();
        if (b == null) return;
        boolean ok = controller.deleteBudget(b);
        if (ok) {
            loadBudgetData();
        }
    }

    public void loadBudgetData() {
        List<Budget> list = controller.getBudgetsForMonth(activeMonth);
        budgetTable.setItems(FXCollections.observableArrayList(list));

        totalBudgetLabel.setText(String.format("$%.2f", controller.getTotalBudget(activeMonth)));
        int exceeded = controller.getExceededCount(activeMonth);
        exceededCountLabel.setText(String.valueOf(exceeded));
    }

    private static Label kpiCaption(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("kpi-caption");
        return l;
    }

    private static Label emptyLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("table-empty");
        return l;
    }
}
