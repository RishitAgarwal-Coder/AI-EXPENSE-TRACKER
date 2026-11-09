package com.smartspend.view;

import com.smartspend.controller.AIAssistantController;
import com.smartspend.controller.BudgetController;
import com.smartspend.controller.DashboardController;
import com.smartspend.controller.ExpenseController;
import com.smartspend.controller.IncomeController;
import com.smartspend.controller.ReportsController;
import com.smartspend.model.User;
import com.smartspend.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Main application window layout with Sidebar navigation.
 * Demonstrates:
 * 1. BorderPane master layout
 * 2. Dynamic View Swapping in MVC
 * 3. Event-driven UI state management
 */
public class MainView extends BorderPane {

    private final Runnable logoutCallback;

    private StackPane contentArea;
    private final Map<String, Node> viewCache = new HashMap<>();
    private final Map<String, Button> navButtons = new HashMap<>();
    private String currentView = "";

    // Sub-views
    private DashboardView dashboardView;
    private ExpenseView expenseView;
    private IncomeView incomeView;
    private BudgetView budgetView;
    private AIAssistantView aiAssistantView;
    private ReportsView reportsView;

    public MainView(Runnable logoutCallback) {
        this.logoutCallback = logoutCallback;
        initUI();
    }

    private void initUI() {
        setStyle("-fx-background-color: #f1f5f9;");

        // 1. Sidebar (Left)
        VBox sidebar = createSidebar();
        setLeft(sidebar);

        // 2. Content Area (Center)
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f8fafc;");
        setCenter(contentArea);

        // Initialize Views
        dashboardView = new DashboardView(new DashboardController(), this::navigateTo);
        expenseView = new ExpenseView(new ExpenseController());
        incomeView = new IncomeView(new IncomeController());
        budgetView = new BudgetView(new BudgetController());
        aiAssistantView = new AIAssistantView(new AIAssistantController());
        reportsView = new ReportsView(new ReportsController());

        viewCache.put("Dashboard", dashboardView);
        viewCache.put("Expenses", expenseView);
        viewCache.put("Income", incomeView);
        viewCache.put("Budgets", budgetView);
        viewCache.put("AI Assistant", aiAssistantView);
        viewCache.put("Reports", reportsView);

        // Default initial view
        navigateTo("Dashboard");
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(24, 14, 20, 14));

        // Brand Logo
        Label brandTitle = new Label("SmartSpend");
        brandTitle.getStyleClass().add("sidebar-brand");
        brandTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Label brandSub = new Label("AI Student Expense Tracker");
        brandSub.getStyleClass().add("sidebar-subtitle");
        brandSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-padding: 0 0 12px 6px;");

        // User Profile Pill
        User user = SessionManager.getInstance().getCurrentUser();
        String userName = (user != null && user.getName() != null) ? user.getName() : "Student User";
        String userEmail = (user != null && user.getEmail() != null) ? user.getEmail() : "user@smartspend.com";

        VBox userCard = new VBox(2);
        userCard.setPadding(new Insets(10, 12, 10, 12));
        userCard.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8px;");

        Label nameLabel = new Label("👤 " + userName);
        nameLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label emailLabel = new Label(userEmail);
        emailLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        userCard.getChildren().addAll(nameLabel, emailLabel);

        VBox navBox = new VBox(6);
        navBox.setPadding(new Insets(14, 0, 0, 0));

        // Navigation Items
        createNavItem(navBox, "Dashboard", "📊 Dashboard");
        createNavItem(navBox, "Expenses", "💳 Expenses");
        createNavItem(navBox, "Income", "💰 Income");
        createNavItem(navBox, "Budgets", "🎯 Monthly Budgets");
        createNavItem(navBox, "AI Assistant", "🤖 AI Assistant");
        createNavItem(navBox, "Reports", "📈 Reports & Analytics");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout Button
        Button logoutBtn = new Button("🚪 Sign Out");
        logoutBtn.getStyleClass().add("logout-button");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            SessionManager.getInstance().logout();
            if (logoutCallback != null) {
                logoutCallback.run();
            }
        });

        sidebar.getChildren().addAll(brandTitle, brandSub, userCard, navBox, spacer, logoutBtn);
        return sidebar;
    }

    private void createNavItem(VBox container, String key, String labelText) {
        Button btn = new Button(labelText);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> navigateTo(key));
        navButtons.put(key, btn);
        container.getChildren().add(btn);
    }

    public void navigateTo(String viewKey) {
        if (!viewCache.containsKey(viewKey)) return;

        // Update active nav button styles
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(viewKey)) {
                entry.getValue().getStyleClass().add("nav-button-active");
            } else {
                entry.getValue().getStyleClass().remove("nav-button-active");
            }
        }

        Node view = viewCache.get(viewKey);
        contentArea.getChildren().setAll(view);
        currentView = viewKey;

        // Auto-refresh views upon switching
        if (view instanceof DashboardView dv) {
            dv.refresh();
        } else if (view instanceof ExpenseView ev) {
            ev.loadTableData();
        } else if (view instanceof IncomeView iv) {
            iv.loadTableData();
        } else if (view instanceof BudgetView bv) {
            bv.loadBudgetData();
        } else if (view instanceof ReportsView rv) {
            rv.loadReportData();
        }
    }
}
