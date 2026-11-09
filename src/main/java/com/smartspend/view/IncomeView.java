package com.smartspend.view;

import com.smartspend.controller.IncomeController;
import com.smartspend.model.Income;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX View for Income Management.
 * Demonstrates:
 * 1. CRUD GUI for Income sources
 * 2. Real-time balance calculations
 * 3. Search and filtering
 */
public class IncomeView extends ScrollPane {

    private final IncomeController controller;

    private TableView<Income> incomeTable;
    private TextField amountField;
    private ComboBox<String> sourceBox;
    private TextField descField;
    private DatePicker datePicker;

    private Button addBtn;
    private Button updateBtn;
    private Button clearBtn;

    private Label totalMonthlyLabel;
    private Label remainingBalanceLabel;

    // Filter controls
    private TextField searchField;
    private ComboBox<String> filterSourceBox;
    private DatePicker filterStartDate;
    private DatePicker filterEndDate;

    private Income selectedIncome = null;

    public IncomeView(IncomeController controller) {
        this.controller = controller;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        initUI();
        loadTableData();
    }

    private void initUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        // 1. Header with monthly KPI banners
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Income Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");
        Label subtitle = new Label("Manage your stipend, allowance, freelance, and job income");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalMonthlyLabel = new Label("$0.00");
        totalMonthlyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        VBox incomeKpi = new VBox(2, new Label("Monthly Income:"), totalMonthlyLabel);
        incomeKpi.setStyle("-fx-background-color: #dcfce7; -fx-padding: 8px 16px; -fx-background-radius: 8px;");

        remainingBalanceLabel = new Label("$0.00");
        remainingBalanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4338ca;");
        VBox balanceKpi = new VBox(2, new Label("Remaining Balance:"), remainingBalanceLabel);
        balanceKpi.setStyle("-fx-background-color: #ede9fe; -fx-padding: 8px 16px; -fx-background-radius: 8px;");

        header.getChildren().addAll(titleBox, spacer, incomeKpi, balanceKpi);

        // 2. Add / Edit Income Form
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("content-card");

        Label formTitle = new Label("Add / Edit Income");
        formTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e1b4b;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(12);

        amountField = new TextField();
        amountField.setPromptText("e.g. 500.00");

        sourceBox = new ComboBox<>(FXCollections.observableArrayList(Income.SOURCES));
        sourceBox.setPromptText("Select Source");
        sourceBox.setPrefWidth(200);

        descField = new TextField();
        descField.setPromptText("e.g. Monthly Internship Stipend");

        datePicker = new DatePicker(LocalDate.now());

        formGrid.add(new Label("Amount ($):"), 0, 0);
        formGrid.add(amountField, 0, 1);
        formGrid.add(new Label("Source:"), 1, 0);
        formGrid.add(sourceBox, 1, 1);
        formGrid.add(new Label("Date:"), 2, 0);
        formGrid.add(datePicker, 2, 1);
        formGrid.add(new Label("Description:"), 3, 0);
        formGrid.add(descField, 3, 1);

        GridPane.setHgrow(descField, Priority.ALWAYS);

        HBox formActions = new HBox(10);
        formActions.setAlignment(Pos.CENTER_LEFT);

        addBtn = new Button("➕ Add Income");
        addBtn.getStyleClass().add("btn-success");
        addBtn.setOnAction(e -> handleAddIncome());

        updateBtn = new Button("💾 Update Selected");
        updateBtn.getStyleClass().add("btn-secondary");
        updateBtn.setDisable(true);
        updateBtn.setOnAction(e -> handleUpdateIncome());

        clearBtn = new Button("Clear Form");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> clearForm());

        formActions.getChildren().addAll(addBtn, updateBtn, clearBtn);
        formCard.getChildren().addAll(formTitle, formGrid, formActions);

        // 3. Search & Filter Bar
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

        List<String> filterSources = new ArrayList<>();
        filterSources.add("All");
        filterSources.addAll(Income.SOURCES);
        filterSourceBox = new ComboBox<>(FXCollections.observableArrayList(filterSources));
        filterSourceBox.setValue("All");
        filterSourceBox.setOnAction(e -> handleFilter());

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
            filterSourceBox.setValue("All");
            filterStartDate.setValue(null);
            filterEndDate.setValue(null);
            loadTableData();
        });

        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> handleDeleteIncome());

        filterBar.getChildren().addAll(
                new Label("Filter:"), searchField, filterSourceBox,
                new Label("From:"), filterStartDate,
                new Label("To:"), filterEndDate,
                resetFilterBtn, fSpacer, deleteBtn
        );

        // 4. Income TableView
        incomeTable = createTable();
        incomeTable.setPrefHeight(380);

        incomeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedIncome = newSel;
                amountField.setText(String.valueOf(newSel.getAmount()));
                sourceBox.setValue(newSel.getSource());
                descField.setText(newSel.getDescription());
                datePicker.setValue(newSel.getDate());
                updateBtn.setDisable(false);
                addBtn.setDisable(true);
            }
        });

        root.getChildren().addAll(header, formCard, filterBar, incomeTable);
        setContent(root);
    }

    private TableView<Income> createTable() {
        TableView<Income> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Income, String> idCol = new TableColumn<>("#");
        idCol.setPrefWidth(50);
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<Income, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(110);
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(DateUtil.formatDisplay(data.getValue().getDate())));

        TableColumn<Income, String> srcCol = new TableColumn<>("Source");
        srcCol.setPrefWidth(140);
        srcCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSource()));

        TableColumn<Income, String> amountCol = new TableColumn<>("Amount ($)");
        amountCol.setPrefWidth(110);
        amountCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("+$%.2f", data.getValue().getAmount())));
        amountCol.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");

        TableColumn<Income, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(260);
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        table.getColumns().addAll(idCol, dateCol, srcCol, amountCol, descCol);
        return table;
    }

    private void handleAddIncome() {
        boolean ok = controller.addIncome(
                amountField.getText(),
                sourceBox.getValue(),
                descField.getText(),
                datePicker.getValue()
        );
        if (ok) {
            clearForm();
            loadTableData();
        }
    }

    private void handleUpdateIncome() {
        if (selectedIncome == null) return;
        boolean ok = controller.updateIncome(
                selectedIncome.getId(),
                amountField.getText(),
                sourceBox.getValue(),
                descField.getText(),
                datePicker.getValue()
        );
        if (ok) {
            clearForm();
            loadTableData();
        }
    }

    private void handleDeleteIncome() {
        Income inc = incomeTable.getSelectionModel().getSelectedItem();
        if (inc == null) return;
        boolean ok = controller.deleteIncome(inc);
        if (ok) {
            clearForm();
            loadTableData();
        }
    }

    private void handleFilter() {
        String keyword = searchField.getText();
        String source = filterSourceBox.getValue();
        LocalDate start = filterStartDate.getValue();
        LocalDate end = filterEndDate.getValue();

        List<Income> filtered = controller.filterIncome(keyword, source, start, end);
        incomeTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void clearForm() {
        selectedIncome = null;
        amountField.clear();
        sourceBox.setValue(null);
        descField.clear();
        datePicker.setValue(LocalDate.now());
        updateBtn.setDisable(true);
        addBtn.setDisable(false);
        incomeTable.getSelectionModel().clearSelection();
    }

    public void loadTableData() {
        List<Income> list = controller.loadAllIncome();
        incomeTable.setItems(FXCollections.observableArrayList(list));

        totalMonthlyLabel.setText(String.format("$%.2f", controller.getMonthlyTotalIncome()));
        remainingBalanceLabel.setText(String.format("$%.2f", controller.getRemainingBalance()));
    }
}
