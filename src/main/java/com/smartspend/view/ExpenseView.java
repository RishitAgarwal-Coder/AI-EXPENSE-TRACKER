package com.smartspend.view;

import com.smartspend.controller.ExpenseController;
import com.smartspend.model.Expense;
import com.smartspend.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

/**
 * JavaFX View for Expense Management.
 * Demonstrates:
 * 1. Complete CRUD GUI (Create, Read, Update, Delete)
 * 2. Search & Filter Bar (Keywords, Category ComboBox, DatePickers)
 * 3. TableView selection binding
 * 4. FileChooser integration for CSV export
 */
public class ExpenseView extends ScrollPane {

    private final ExpenseController controller;

    private TableView<Expense> expenseTable;
    private TextField amountField;
    private ComboBox<String> categoryBox;
    private TextField descField;
    private DatePicker datePicker;

    private Button addBtn;
    private Button updateBtn;
    private Button clearBtn;

    // Filter controls
    private TextField searchField;
    private ComboBox<String> filterCategoryBox;
    private DatePicker filterStartDate;
    private DatePicker filterEndDate;

    private Expense selectedExpense = null;

    public ExpenseView(ExpenseController controller) {
        this.controller = controller;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        initUI();
        loadTableData();
    }

    private void initUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        // 1. Header Banner
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Expense Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");
        Label subtitle = new Label("Track, search, and categorize your daily spending");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("Export to CSV");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> handleExportCsv());

        header.getChildren().addAll(titleBox, spacer, exportBtn);

        // 2. Add / Edit Expense Form Card
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("content-card");

        Label formTitle = new Label("Add / Edit Expense");
        formTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(12);

        // Fields
        amountField = new TextField();
        amountField.setPromptText("e.g. 15.50");

        categoryBox = new ComboBox<>(FXCollections.observableArrayList(Expense.CATEGORIES));
        categoryBox.setPromptText("Select Category");
        categoryBox.setPrefWidth(200);

        descField = new TextField();
        descField.setPromptText("e.g. Campus cafeteria lunch");

        datePicker = new DatePicker(LocalDate.now());

        formGrid.add(new Label("Amount ($):"), 0, 0);
        formGrid.add(amountField, 0, 1);
        formGrid.add(new Label("Category:"), 1, 0);
        formGrid.add(categoryBox, 1, 1);
        formGrid.add(new Label("Date:"), 2, 0);
        formGrid.add(datePicker, 2, 1);
        formGrid.add(new Label("Description:"), 3, 0);
        formGrid.add(descField, 3, 1);

        GridPane.setHgrow(descField, Priority.ALWAYS);

        // Form Action Buttons
        HBox formActions = new HBox(10);
        formActions.setAlignment(Pos.CENTER_LEFT);

        addBtn = new Button("➕ Add Expense");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> handleAddExpense());

        updateBtn = new Button("💾 Update Selected");
        updateBtn.getStyleClass().add("btn-secondary");
        updateBtn.setDisable(true);
        updateBtn.setOnAction(e -> handleUpdateExpense());

        clearBtn = new Button("Clear Form");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> clearForm());

        formActions.getChildren().addAll(addBtn, updateBtn, clearBtn);
        formCard.getChildren().addAll(formTitle, formGrid, formActions);

        // 3. Search and Filter Bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(12, 16, 12, 16));
        filterBar.setStyle(
                "-fx-background-color: #ffffff;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 10px;"
        );

        searchField = new TextField();
        searchField.setPromptText("Search description...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((obs, oldV, newV) -> handleFilter());

        List<String> filterCategories = new ArrayList<>();
        filterCategories.add("All");
        filterCategories.addAll(Expense.CATEGORIES);
        filterCategoryBox = new ComboBox<>(FXCollections.observableArrayList(filterCategories));
        filterCategoryBox.setValue("All");
        filterCategoryBox.setOnAction(e -> handleFilter());

        filterStartDate = new DatePicker();
        filterStartDate.setPromptText("From Date");
        filterStartDate.setOnAction(e -> handleFilter());

        filterEndDate = new DatePicker();
        filterEndDate.setPromptText("To Date");
        filterEndDate.setOnAction(e -> handleFilter());

        Button resetFilterBtn = new Button("Reset");
        resetFilterBtn.getStyleClass().add("btn-secondary");
        resetFilterBtn.setOnAction(e -> {
            searchField.clear();
            filterCategoryBox.setValue("All");
            filterStartDate.setValue(null);
            filterEndDate.setValue(null);
            loadTableData();
        });

        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> handleDeleteExpense());

        filterBar.getChildren().addAll(
                new Label("Filter:"), searchField, filterCategoryBox,
                new Label("From:"), filterStartDate,
                new Label("To:"), filterEndDate,
                resetFilterBtn, fSpacer, deleteBtn
        );

        // 4. Expenses TableView
        expenseTable = createTable();
        expenseTable.setPrefHeight(380);

        // Row selection listener to populate form for updating
        expenseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedExpense = newSel;
                amountField.setText(String.valueOf(newSel.getAmount()));
                categoryBox.setValue(newSel.getCategory());
                descField.setText(newSel.getDescription());
                datePicker.setValue(newSel.getDate());
                updateBtn.setDisable(false);
                addBtn.setDisable(true);
            }
        });

        root.getChildren().addAll(header, formCard, filterBar, expenseTable);
        setContent(root);
    }

    private TableView<Expense> createTable() {
        TableView<Expense> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Expense, String> idCol = new TableColumn<>("#");
        idCol.setPrefWidth(50);
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<Expense, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(110);
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(DateUtil.formatDisplay(data.getValue().getDate())));

        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setPrefWidth(130);
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<Expense, String> amountCol = new TableColumn<>("Amount ($)");
        amountCol.setPrefWidth(110);
        amountCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getAmount())));
        amountCol.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");

        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(260);
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        table.getColumns().addAll(idCol, dateCol, catCol, amountCol, descCol);
        return table;
    }

    private void handleAddExpense() {
        boolean ok = controller.addExpense(
                amountField.getText(),
                categoryBox.getValue(),
                descField.getText(),
                datePicker.getValue()
        );
        if (ok) {
            clearForm();
            loadTableData();
        }
    }

    private void handleUpdateExpense() {
        if (selectedExpense == null) return;
        boolean ok = controller.updateExpense(
                selectedExpense.getId(),
                amountField.getText(),
                categoryBox.getValue(),
                descField.getText(),
                datePicker.getValue()
        );
        if (ok) {
            clearForm();
            loadTableData();
        }
    }

    private void handleDeleteExpense() {
        Expense exp = expenseTable.getSelectionModel().getSelectedItem();
        if (exp == null) return;
        boolean ok = controller.deleteExpense(exp);
        if (ok) {
            clearForm();
            loadTableData();
        }
    }

    private void handleFilter() {
        String keyword = searchField.getText();
        String category = filterCategoryBox.getValue();
        LocalDate start = filterStartDate.getValue();
        LocalDate end = filterEndDate.getValue();

        List<Expense> filtered = controller.filterExpenses(keyword, category, start, end);
        expenseTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Expenses to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("smartspend_expenses.csv");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            controller.exportToCsv(file, expenseTable.getItems());
        }
    }

    private void clearForm() {
        selectedExpense = null;
        amountField.clear();
        categoryBox.setValue(null);
        descField.clear();
        datePicker.setValue(LocalDate.now());
        updateBtn.setDisable(true);
        addBtn.setDisable(false);
        expenseTable.getSelectionModel().clearSelection();
    }

    public void loadTableData() {
        List<Expense> list = controller.loadAllExpenses();
        expenseTable.setItems(FXCollections.observableArrayList(list));
    }
}
