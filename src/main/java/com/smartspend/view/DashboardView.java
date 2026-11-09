package com.smartspend.view;

import com.smartspend.controller.DashboardController;
import com.smartspend.model.CategorySummary;
import com.smartspend.model.Transaction;
import com.smartspend.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * JavaFX View for the Dashboard.
 * Demonstrates:
 * 1. JavaFX Controls (TableView, PieChart, GridPane, Cards)
 * 2. Polymorphic TableView rendering for List<Transaction>
 * 3. Modern Dark Blue & Purple aesthetic
 */
public class DashboardView extends ScrollPane {

    private final DashboardController controller;
    private final Consumer<String> navigationConsumer;

    private Label incomeValueLabel;
    private Label expenseValueLabel;
    private Label balanceValueLabel;
    private Label savingsValueLabel;
    private TableView<Transaction> recentTable;
    private PieChart categoryPieChart;
    private Label emptyPieLabel;

    public DashboardView(DashboardController controller, Consumer<String> navigationConsumer) {
        this.controller = controller;
        this.navigationConsumer = navigationConsumer;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        initUI();
        refresh();
    }

    private void initUI() {
        VBox root = new VBox(22);
        root.setPadding(new Insets(24));

        // 1. Dashboard Banner Header
        HBox banner = new HBox();
        banner.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Financial Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");
        Label subtitle = new Label("Overview for " + DateUtil.formatMonthHeader(DateUtil.getCurrentYearMonth()));
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button askAiBtn = new Button("🤖 Ask AI Assistant");
        askAiBtn.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 8px; -fx-cursor: hand;");
        askAiBtn.setOnAction(e -> {
            if (navigationConsumer != null) navigationConsumer.accept("AI Assistant");
        });

        banner.getChildren().addAll(titleBox, spacer, askAiBtn);

        // 2. Metric Stat Cards (GridPane)
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);

        incomeValueLabel = new Label("$0.00");
        expenseValueLabel = new Label("$0.00");
        balanceValueLabel = new Label("$0.00");
        savingsValueLabel = new Label("$0.00");

        VBox incomeCard = createStatCard("Total Income", incomeValueLabel, "This Month", "#16a34a");
        VBox expenseCard = createStatCard("Total Expenses", expenseValueLabel, "This Month", "#dc2626");
        VBox balanceCard = createStatCard("Net Balance", balanceValueLabel, "Cash Flow", "#4338ca");
        VBox savingsCard = createStatCard("Monthly Savings", savingsValueLabel, "Target", "#7c3aed");

        statsGrid.add(incomeCard, 0, 0);
        statsGrid.add(expenseCard, 1, 0);
        statsGrid.add(balanceCard, 2, 0);
        statsGrid.add(savingsCard, 3, 0);

        statsGrid.getColumnConstraints().addAll(
                new javafx.scene.layout.ColumnConstraints(100, 240, Double.MAX_VALUE, Priority.ALWAYS, javafx.geometry.HPos.CENTER, true),
                new javafx.scene.layout.ColumnConstraints(100, 240, Double.MAX_VALUE, Priority.ALWAYS, javafx.geometry.HPos.CENTER, true),
                new javafx.scene.layout.ColumnConstraints(100, 240, Double.MAX_VALUE, Priority.ALWAYS, javafx.geometry.HPos.CENTER, true),
                new javafx.scene.layout.ColumnConstraints(100, 240, Double.MAX_VALUE, Priority.ALWAYS, javafx.geometry.HPos.CENTER, true)
        );

        // 3. Main Content Split: Recent Transactions + Spending Breakdown Chart
        HBox contentSplit = new HBox(18);

        // Recent Transactions Box (Left)
        VBox recentBox = new VBox(12);
        recentBox.getStyleClass().add("content-card");
        HBox.setHgrow(recentBox, Priority.ALWAYS);

        HBox recentHeader = new HBox();
        recentHeader.setAlignment(Pos.CENTER_LEFT);
        Label recentTitle = new Label("Recent Transactions");
        recentTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");
        Region rSpacer = new Region();
        HBox.setHgrow(rSpacer, Priority.ALWAYS);

        Button viewAllExpBtn = new Button("View All");
        viewAllExpBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4338ca; -fx-font-weight: bold; -fx-cursor: hand;");
        viewAllExpBtn.setOnAction(e -> {
            if (navigationConsumer != null) navigationConsumer.accept("Expenses");
        });
        recentHeader.getChildren().addAll(recentTitle, rSpacer, viewAllExpBtn);

        recentTable = createRecentTransactionsTable();
        recentTable.setPrefHeight(320);
        recentBox.getChildren().addAll(recentHeader, recentTable);

        // Spending Chart Box (Right)
        VBox chartBox = new VBox(12);
        chartBox.getStyleClass().add("content-card");
        chartBox.setPrefWidth(380);
        chartBox.setMinWidth(320);

        Label chartTitle = new Label("Spending by Category");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");

        categoryPieChart = new PieChart();
        categoryPieChart.setLegendVisible(true);
        categoryPieChart.setLabelsVisible(true);
        categoryPieChart.setPrefHeight(280);

        emptyPieLabel = new Label("No expense records found for this month.");
        emptyPieLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
        emptyPieLabel.setAlignment(Pos.CENTER);

        chartBox.getChildren().addAll(chartTitle, categoryPieChart, emptyPieLabel);

        contentSplit.getChildren().addAll(recentBox, chartBox);

        root.getChildren().addAll(banner, statsGrid, contentSplit);
        setContent(root);
    }

    private VBox createStatCard(String title, Label valLabel, String subtext, String colorHex) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        valLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");

        Label sub = new Label(subtext);
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        card.getChildren().addAll(titleLabel, valLabel, sub);
        return card;
    }

    private TableView<Transaction> createRecentTransactionsTable() {
        TableView<Transaction> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Column 1: Type
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(90);
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTransactionType()));
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("INCOME".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Column 2: Category / Source
        TableColumn<Transaction, String> catCol = new TableColumn<>("Category / Source");
        catCol.setPrefWidth(120);
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

        // Column 3: Description
        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(180);
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        // Column 4: Amount
        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setPrefWidth(100);
        amountCol.setCellValueFactory(data -> {
            Transaction t = data.getValue();
            String prefix = "INCOME".equalsIgnoreCase(t.getTransactionType()) ? "+$" : "-$";
            return new SimpleStringProperty(String.format("%s%.2f", prefix, t.getAmount()));
        });
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("+")) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Column 5: Date
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(100);
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(DateUtil.formatDisplay(data.getValue().getDate())));

        table.getColumns().addAll(typeCol, catCol, descCol, amountCol, dateCol);
        return table;
    }

    /**
     * Refreshes all data on the dashboard.
     */
    public void refresh() {
        double income = controller.getMonthlyIncome();
        double expense = controller.getMonthlyExpense();
        double balance = controller.getRemainingBalance();
        double savings = controller.getMonthlySavings();

        incomeValueLabel.setText(String.format("$%.2f", income));
        expenseValueLabel.setText(String.format("$%.2f", expense));
        balanceValueLabel.setText(String.format("$%.2f", balance));
        savingsValueLabel.setText(String.format("$%.2f", savings));

        // Update Recent Transactions Table
        List<Transaction> recents = controller.getRecentTransactions(8);
        recentTable.setItems(FXCollections.observableArrayList(recents));

        // Update Category Pie Chart
        List<CategorySummary> catSummaries = controller.getCategorySummaries();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (CategorySummary cs : catSummaries) {
            pieData.add(new PieChart.Data(cs.getCategory() + " ($" + (int)cs.getTotalAmount() + ")", cs.getTotalAmount()));
        }
        categoryPieChart.setData(pieData);

        boolean hasData = !pieData.isEmpty();
        categoryPieChart.setVisible(hasData);
        categoryPieChart.setManaged(hasData);
        emptyPieLabel.setVisible(!hasData);
        emptyPieLabel.setManaged(!hasData);
    }
}
