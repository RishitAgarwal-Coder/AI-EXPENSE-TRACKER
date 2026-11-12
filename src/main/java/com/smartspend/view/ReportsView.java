package com.smartspend.view;

import com.smartspend.controller.ReportsController;
import com.smartspend.model.CategorySummary;
import com.smartspend.model.MonthlySummary;
import com.smartspend.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JavaFX View for Financial Reports and Analytics.
 * Demonstrates:
 * 1. JavaFX BarChart (Income vs Expense comparison)
 * 2. Category-wise analytics TableView
 * 3. File export triggers (CSV and TXT formats)
 */
public class ReportsView extends ScrollPane {

    private final ReportsController controller;

    private ComboBox<String> monthPicker;
    private Label incomeLabel;
    private Label expenseLabel;
    private Label savingsLabel;
    private Label savingsRateLabel;

    private BarChart<String, Number> comparisonChart;
    private TableView<CategorySummary> categoryTable;

    public ReportsView(ReportsController controller) {
        this.controller = controller;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        initUI();
        loadReportData();
    }

    private void initUI() {
        VBox root = new VBox(22);
        root.setPadding(new Insets(24));

        // 1. Header with Month Selector and Export Buttons
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Financial Reports & Analytics");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");
        Label subtitle = new Label("Review your spending trends, cash flow comparisons, and export records");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Month Picker
        List<String> months = generateMonthOptions();
        monthPicker = new ComboBox<>(FXCollections.observableArrayList(months));
        monthPicker.setValue(DateUtil.getCurrentYearMonth());
        monthPicker.setOnAction(e -> loadReportData());

        Button exportTxtBtn = new Button("📄 Export Report (TXT)");
        exportTxtBtn.getStyleClass().add("btn-secondary");
        exportTxtBtn.setOnAction(e -> handleExportTxt());

        Button exportCsvBtn = new Button("📊 Export Expenses (CSV)");
        exportCsvBtn.getStyleClass().add("btn-secondary");
        exportCsvBtn.setOnAction(e -> handleExportCsv());

        header.getChildren().addAll(titleBox, spacer, new Label("Month:"), monthPicker, exportTxtBtn, exportCsvBtn);

        // 2. Metric Stat Cards
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);

        incomeLabel = new Label("$0.00");
        expenseLabel = new Label("$0.00");
        savingsLabel = new Label("$0.00");
        savingsRateLabel = new Label("0.0%");

        VBox incCard = createStatCard("Total Income", incomeLabel, "#16a34a");
        VBox expCard = createStatCard("Total Expenses", expenseLabel, "#dc2626");
        VBox savCard = createStatCard("Net Savings", savingsLabel, "#4338ca");
        VBox rateCard = createStatCard("Savings Rate", savingsRateLabel, "#7c3aed");

        statsGrid.add(incCard, 0, 0);
        statsGrid.add(expCard, 1, 0);
        statsGrid.add(savCard, 2, 0);
        statsGrid.add(rateCard, 3, 0);

        statsGrid.getColumnConstraints().addAll(
                new javafx.scene.layout.ColumnConstraints(100, 240, Double.MAX_VALUE, Priority.ALWAYS, javafx.geometry.HPos.CENTER, true),
                new javafx.scene.layout.ColumnConstraints(100, 240, Double.MAX_VALUE, Priority.ALWAYS, javafx.geometry.HPos.CENTER, true),
                new javafx.scene.layout.ColumnConstraints(100, 240, Double.MAX_VALUE, Priority.ALWAYS, javafx.geometry.HPos.CENTER, true),
                new javafx.scene.layout.ColumnConstraints(100, 240, Double.MAX_VALUE, Priority.ALWAYS, javafx.geometry.HPos.CENTER, true)
        );

        // 3. Comparison Chart: Income vs Expenses over past 6 months
        VBox chartCard = new VBox(12);
        chartCard.getStyleClass().add("content-card");

        Label chartTitle = new Label("Monthly Cash Flow Trend (Past 6 Months)");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount ($)");

        comparisonChart = new BarChart<>(xAxis, yAxis);
        comparisonChart.setPrefHeight(280);
        comparisonChart.setAnimated(false);

        chartCard.getChildren().addAll(chartTitle, comparisonChart);

        // 4. Category-Wise Breakdown Table
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("content-card");

        Label tableTitle = new Label("Category-Wise Spending Breakdown");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");

        categoryTable = createCategoryTable();
        categoryTable.setPrefHeight(260);

        tableCard.getChildren().addAll(tableTitle, categoryTable);

        root.getChildren().addAll(header, statsGrid, chartCard, tableCard);
        setContent(root);
    }

    private VBox createStatCard(String title, Label valLabel, String colorHex) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        valLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");

        card.getChildren().addAll(titleLabel, valLabel);
        return card;
    }

    private TableView<CategorySummary> createCategoryTable() {
        TableView<CategorySummary> table = new TableView<>();
        table.setPlaceholder(emptyLabel("No spending recorded for this month."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CategorySummary, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<CategorySummary, String> amtCol = new TableColumn<>("Total Spent ($)");
        amtCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getTotalAmount())));
        amtCol.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");

        TableColumn<CategorySummary, String> pctCol = new TableColumn<>("Share (%)");
        pctCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.1f%%", data.getValue().getPercentage())));

        TableColumn<CategorySummary, String> countCol = new TableColumn<>("Transactions Count");
        countCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTransactionCount())));

        table.getColumns().addAll(catCol, amtCol, pctCol, countCol);
        return table;
    }

    private void handleExportTxt() {
        String month = monthPicker.getValue();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Financial Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
        fileChooser.setInitialFileName("smartspend_report_" + month + ".txt");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            controller.exportMonthlyReportToText(file, month);
        }
    }

    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export All Expenses to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("smartspend_all_expenses.csv");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            controller.exportAllExpensesToCsv(file);
        }
    }

    private List<String> generateMonthOptions() {
        List<String> list = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            LocalDate m = now.minusMonths(i);
            list.add(String.format("%04d-%02d", m.getYear(), m.getMonthValue()));
        }
        return list;
    }

    public void loadReportData() {
        String month = monthPicker.getValue();
        if (month == null) month = DateUtil.getCurrentYearMonth();

        MonthlySummary summary = controller.getMonthlySummary(month);
        incomeLabel.setText(String.format("$%.2f", summary.getTotalIncome()));
        expenseLabel.setText(String.format("$%.2f", summary.getTotalExpense()));
        savingsLabel.setText(String.format("$%.2f", summary.getNetSavings()));
        savingsRateLabel.setText(String.format("%.1f%%", summary.getSavingsRate()));

        // Update category table
        List<CategorySummary> catList = controller.getCategoryReport(month);
        categoryTable.setItems(FXCollections.observableArrayList(catList));

        // Update BarChart
        comparisonChart.getData().clear();

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expense");

        Map<String, Double> incMap = controller.getMonthlyIncomes(6);
        Map<String, Double> expMap = controller.getMonthlyExpenses(6);

        for (String m : incMap.keySet()) {
            incomeSeries.getData().add(new XYChart.Data<>(m, incMap.get(m)));
            expenseSeries.getData().add(new XYChart.Data<>(m, expMap.getOrDefault(m, 0.0)));
        }

        comparisonChart.getData().addAll(incomeSeries, expenseSeries);
    }

    private static Label emptyLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("table-empty");
        return l;
    }
}
