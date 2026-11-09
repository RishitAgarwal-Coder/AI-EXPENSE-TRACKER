# SmartSpend – AI-Integrated Student Expense Tracker 💰🤖

An attractive, functional, and production-grade Desktop Expense Tracker built specifically as an academic mini-project for **BTech Computer Science / Engineering students (2nd Year)**.

Developed purely in **Java (JDK 21)** using **JavaFX**, **MySQL**, **JDBC**, and the **Google Gemini API**, following a clean **MVC (Model-View-Controller) + DAO + Service** architectural pattern.

---

## 📑 Table of Contents
1. [Project Highlights](#-project-highlights)
2. [Technology Stack](#-technology-stack)
3. [Architecture & Project Structure](#-architecture--project-structure)
4. [Database Setup (MySQL)](#-database-setup-mysql)
5. [Gemini AI Configuration](#-gemini-ai-configuration)
6. [How to Build and Run](#-how-to-build-and-run)
7. [Core Features Walkthrough](#-core-features-walkthrough)
8. [Explanation of Important Java Classes](#-explanation-of-important-java-classes)
9. [Demonstration of OOP and Java Concepts (Viva Guide)](#-demonstration-of-oop-and-java-concepts-viva-guide)
10. [Sample Viva Examination Questions & Answers](#-sample-viva-examination-questions--answers)

---

## 🌟 Project Highlights

- **Pure Java**: 100% written in Java. Zero external runtimes like Python, Node.js, or JavaScript.
- **Modern Student Dashboard**: Styled with a dark blue & purple modern palette (`#0f172a`, `#1e1b4b`, `#4338ca`, `#ede9fe`), rounded cards, stat badges, and a sidebar navigation menu.
- **Real Database-Backed AI Assistant**: The AI does **NOT** generate fake or random financial data. It reads real transactions, categories, and active budget deficits directly from MySQL and prompts Google Gemini with the student's true financial context.
- **Offline Fallback Engine**: If evaluating offline or without an active internet connection / API key, the built-in intelligent rule analyzer steps in automatically so the AI tab never breaks during a viva demonstration.
- **Graceful DB Fallback**: Supports local MySQL natively; if MySQL server is offline during evaluation, it boots an in-memory SQL fallback so that graders never encounter connection crashes.
- **File Handling**: Demonstrates writing and reading reports to CSV and formatted TXT files using `FileWriter`, `PrintWriter`, and `BufferedReader`.

---

## 🛠️ Technology Stack

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java | JDK 21 | Core programming language |
| **GUI Framework** | OpenJFX (JavaFX) | 21.0.6 | Modern desktop graphical interface |
| **Database** | MySQL | 8.0+ | Relational data persistence |
| **Connectivity** | JDBC (`com.mysql.cj.jdbc.Driver`) | 8.3.0 | PreparedStatement parameterized queries |
| **AI Integration** | Google Gemini API | 1.5 Flash / 2.5 | Financial advisory via standard `java.net.http.HttpClient` |
| **Build Tool** | Apache Maven | 3.9+ | Dependency and build lifecycle management |

---

## 📁 Architecture & Project Structure

The project implements a clean separation of concerns:

```
SmartSpend/
├── pom.xml                               # Maven project configuration
├── run.bat                               # Windows one-click runner script
├── README.md                             # Comprehensive project documentation
├── database/
│   └── schema.sql                        # MySQL database schema and seed data
├── database.sql                          # Root schema copy for quick import
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/smartspend/
    │   │       ├── Main.java             # JavaFX Application entry point
    │   │       │
    │   │       ├── model/                # Domain Models (Encapsulation, Inheritance)
    │   │       │   ├── User.java
    │   │       │   ├── Transaction.java  # Abstract Base Class
    │   │       │   ├── Expense.java      # Subclass extending Transaction
    │   │       │   ├── Income.java       # Subclass extending Transaction
    │   │       │   ├── Budget.java
    │   │       │   ├── CategorySummary.java
    │   │       │   └── MonthlySummary.java
    │   │       │
    │   │       ├── dao/                  # Data Access Objects (JDBC & SQL Queries)
    │   │       │   ├── CrudDao.java      # Generic Interface (<T>)
    │   │       │   ├── UserDao.java
    │   │       │   ├── ExpenseDao.java
    │   │       │   ├── IncomeDao.java
    │   │       │   └── BudgetDao.java
    │   │       │
    │   │       ├── service/              # Business Logic & AI Services
    │   │       │   ├── AuthService.java
    │   │       │   ├── ExpenseService.java
    │   │       │   ├── IncomeService.java
    │   │       │   ├── BudgetService.java
    │   │       │   ├── ReportService.java
    │   │       │   └── GeminiService.java
    │   │       │
    │   │       ├── controller/           # Event Handlers & View-Model Mediators
    │   │       │   ├── AuthController.java
    │   │       │   ├── DashboardController.java
    │   │       │   ├── ExpenseController.java
    │   │       │   ├── IncomeController.java
    │   │       │   ├── BudgetController.java
    │   │       │   ├── AIAssistantController.java
    │   │       │   └── ReportsController.java
    │   │       │
    │   │       ├── view/                 # JavaFX UI Components & Layouts
    │   │       │   ├── MainView.java     # Master window with sidebar navigation
    │   │       │   ├── LoginView.java
    │   │       │   ├── RegisterView.java
    │   │       │   ├── DashboardView.java
    │   │       │   ├── ExpenseView.java
    │   │       │   ├── IncomeView.java
    │   │       │   ├── BudgetView.java
    │   │       │   ├── AIAssistantView.java
    │   │       │   └── ReportsView.java
    │   │       │
    │   │       └── util/                 # Helper & Utility Classes
    │   │           ├── SessionManager.java   # Singleton user session
    │   │           ├── JsonUtil.java         # Native JSON serializer/parser
    │   │           ├── AlertHelper.java      # JavaFX dialog helper
    │   │           ├── FileExportUtil.java   # File I/O (CSV & TXT)
    │   │           ├── DateUtil.java         # Java Time API formatter
    │   │           └── DatabaseException.java# Custom runtime exception
    │   │
    │   └── resources/
    │       ├── db.properties             # Database credentials configuration
    │       └── styles/
    │           └── style.css             # Dark Blue & Purple modern CSS
    └── test/
        └── java/com/smartspend/
            └── AppTest.java              # Complete 33-test automated test suite
```

---

## 🗄️ Database Setup (MySQL)

### Step 1: Open MySQL Command Line or MySQL Workbench
Log into your MySQL instance:
```bash
mysql -u root -p
```

### Step 2: Import the Database Schema
Execute the script located in `database/schema.sql`:
```sql
SOURCE C:/Users/DAKSH RAJPUT/Downloads/SmartSpend/database/schema.sql;
```
Or from the system command prompt:
```bash
mysql -u root -p < database/schema.sql
```

### Database Tables Created:
1. **`users`**: User profile information (`id`, `name`, `email`, `password`, `created_at`).
2. **`expenses`**: Transaction records (`id`, `user_id`, `category`, `amount`, `description`, `expense_date`).
3. **`income`**: Income streams (`id`, `user_id`, `source`, `amount`, `description`, `income_date`).
4. **`budgets`**: Monthly category limits (`id`, `user_id`, `category`, `budget_month`, `amount`).

### Step 3: Configure Database Credentials
Open `src/main/resources/db.properties` and verify your credentials:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/smartspend_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root
```

> **Default Demo Account Seeded:**  
> - **Email:** `demo@smartspend.com`  
> - **Password:** `password123`

---

## 🤖 Gemini AI Configuration

SmartSpend communicates with Google Gemini API via Java 21's `java.net.http.HttpClient` without exposing keys in code.

### Option A: Set System Environment Variable (Recommended)
On Windows Command Prompt:
```cmd
setx GEMINI_API_KEY "your_gemini_api_key_here"
```
On Windows PowerShell:
```powershell
[System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', 'your_gemini_api_key_here', 'User')
```

### Option B: Enter via In-App Dialog
Launch the application, navigate to the **🤖 AI Assistant** tab, click the **🔑 Set API Key** button in the top right, and paste your key.

### Option C: Offline Smart Analysis Mode
If no API key is provided, SmartSpend automatically activates its **Smart Offline Financial Engine**. It reads your real database records and answers queries like:
- *"Where am I spending most of my money?"*
- *"How can I reduce my expenses?"*
- *"How much did I spend this month?"*
- *"Give me some saving tips."*

---

## 🚀 How to Build and Run

### Method 1: Using Maven (Standard)
Open your terminal in the `SmartSpend` directory:
```bash
# Compile and run the application
mvn clean javafx:run
```

### Method 2: One-Click Windows Batch File
Double-click `run.bat` or run it from CMD:
```cmd
run.bat
```
This project requires JDK 21. If multiple Java versions are installed, set `JAVA_HOME` to the JDK 21 installation; `run.bat` uses it ahead of an older Java version on `PATH`.

### Method 3: Running via IntelliJ IDEA / Eclipse / VS Code
1. Open the `SmartSpend` folder as a Maven project.
2. Wait for Maven dependencies to download.
3. Open `src/main/java/com/smartspend/Main.java`.
4. Right-click and choose **Run 'Main.main()'**.

### Method 4: Running the Automated Test Suite
To verify all 8 phases (33 tests) programmatically:
```bash
mvn test
```

---

## 🖥️ Core Features Walkthrough

### 1. Authentication & Security
- **Registration**: Form with name, email regex validation, 6+ character password validation, and duplicate email prevention.
- **Login**: Verifies credentials and initializes the `SessionManager` singleton.
- **Data Isolation**: All database queries strictly bind `WHERE user_id = ?` to ensure users see only their own data.

### 2. Modern Dashboard
- Four real-time KPI metric cards: **Total Income**, **Total Expenses**, **Net Balance**, and **Monthly Savings**.
- **Recent Transactions Table**: Shows both expenses and incomes polymorphically sorted chronologically.
- **JavaFX PieChart**: Visualizes category percentage breakdown for the current month.

### 3. Expense Management
- Form to add, update, and delete expenses.
- Standard categories: `Food`, `Shopping`, `Travel`, `Education`, `Transport`, `Entertainment`, `Healthcare`, and `Other`.
- Real-time search bar + Category filter + From/To Date range pickers.
- **Export to CSV**: Exports filtered or all expenses into CSV using `FileExportUtil`.

### 4. Income Management
- Add, update, delete income records.
- Track stipend, allowance, freelancing, scholarships, part-time jobs, and gifts.
- Real-time monthly income and net savings calculation.

### 5. Monthly Budget Planner
- Set monthly budget limits per category (e.g. Food: $300, Shopping: $100).
- Dynamically aggregates expenses incurred in that category for the month.
- Displays visual **ProgressBar** turning Green (<80%), Orange (80-99%), and Red (100%+).
- Status badges: `SAFE [OK]`, `NEAR LIMIT [!]`, `EXCEEDED [!]`.

### 6. AI Financial Assistant
- Clean conversational chat interface with avatar bubbles and loading spinner.
- Pre-loaded quick prompt chips for common college financial questions.
- Injects real database metrics into the prompt:
  - Exact total spent and earned.
  - Top spending category.
  - Exceeded budget categories.
  - Recent transactions list.

### 7. Financial Reports & Analytics
- Monthly summary metrics: Total Income, Total Expenses, Net Savings, and Savings Rate %.
- **JavaFX BarChart**: Compares Income vs Expenses over the past 6 months.
- Category distribution summary table with transaction counts and percentage share.
- Export options: Formatted financial text statement (`.txt`) and full transaction CSV (`.csv`).

---

## 📚 Explanation of Important Java Classes

### Model Layer (`com.smartspend.model`)
- **`Transaction.java`**: Abstract base class containing shared fields (`id`, `userId`, `amount`, `category`, `description`, `date`) and the abstract method `public abstract String getTransactionType()`.
- **`Expense.java`**: Subclass representing an expense; overrides `getTransactionType()` returning `"EXPENSE"`.
- **`Income.java`**: Subclass representing income; overrides `getTransactionType()` returning `"INCOME"`.
- **`Budget.java`**: Represents a category budget limit and computes `getRemainingAmount()`, `getPercentageSpent()`, and `isExceeded()`.
- **`User.java`**: Encapsulates user profile details with private fields and getter/setter accessors.

### DAO Layer (`com.smartspend.dao`)
- **`CrudDao<T>.java`**: Generic interface defining standard database CRUD contracts (`save`, `findById`, `findAllByUserId`, `update`, `delete`).
- **`ExpenseDao.java`**: Manages SQL operations for expenses using `PreparedStatement`, handles date filtering, category aggregations (`GROUP BY`), and monthly sums.
- **`IncomeDao.java`**: Manages SQL operations for income streams and calculates monthly totals.
- **`BudgetDao.java`**: Manages budgets and executes cross-table queries computing actual spent amounts against category limits.

### Service Layer (`com.smartspend.service`)
- **`AuthService.java`**: Encapsulates business validation (email regex, password rules) and coordinates user authentication.
- **`ExpenseService.java` & `IncomeService.java`**: Business rules validation (positive numbers, valid dates) and DAO delegation.
- **`BudgetService.java`**: Budget limit evaluation and exceeded budget detection.
- **`ReportService.java`**: Compiles monthly summaries and coordinates file exports.
- **`GeminiService.java`**: AI integration engine using `java.net.http.HttpClient` with environment variable API key resolution and offline database analysis fallback.

### Controller Layer (`com.smartspend.controller`)
- Mediates between JavaFX view events and underlying services. Keeps SQL and view code cleanly decoupled.

### View Layer (`com.smartspend.view`)
- **`MainView.java`**: Master window layout with sidebar navigation, active view caching, and dynamic view switching.
- **`DashboardView.java`**, **`ExpenseView.java`**, **`IncomeView.java`**, **`BudgetView.java`**, **`AIAssistantView.java`**, **`ReportsView.java`**: Modular screens built using JavaFX layout panes (`GridPane`, `HBox`, `VBox`, `ScrollPane`, `TableView`, `PieChart`, `BarChart`).

### Utility Layer (`com.smartspend.util`)
- **`SessionManager.java`**: Singleton storing the active authenticated `User`.
- **`FileExportUtil.java`**: Implements Java File I/O (`FileWriter`, `PrintWriter`, `BufferedReader`).
- **`JsonUtil.java`**: Lightweight native JSON serializer and parser for Gemini API payloads.
- **`DateUtil.java`**: Formats dates using modern `java.time.LocalDate` and `DateTimeFormatter`.

---

## 🎓 Demonstration of OOP and Java Concepts (Viva Guide)

Here is how the project maps directly to the 12 core Java syllabus concepts:

### 1. Classes and Objects
- Defined in classes like `User`, `Expense`, `Income`, `Budget`.
- Instantiated with `new Expense(userId, amount, category, description, date)`.

### 2. Constructors & Constructor Chaining
- Default and parameterized constructors throughout all models.
- **Constructor Chaining** via `this(...)` in `Transaction.java` and `super(...)` in `Expense.java` and `Income.java`.

### 3. Encapsulation
- All instance variables in models (`amount`, `category`, `date`, `password`) are declared `private` or `protected`.
- Accessible strictly through public getter and setter methods with validation (e.g. `setAmount` throws `IllegalArgumentException` on negative amounts).

### 4. Inheritance
- Subclasses `Expense` and `Income` inherit common attributes (`id`, `userId`, `amount`, `category`, `description`, `date`) from `Transaction`.
- Eliminates code duplication and establishes an *"is-a"* relationship.

### 5. Polymorphism
- **Dynamic Method Dispatch**: Subclasses override the abstract method:
  ```java
  // In Expense.java:
  @Override
  public String getTransactionType() { return "EXPENSE"; }
  
  // In Income.java:
  @Override
  public String getTransactionType() { return "INCOME"; }
  ```
- **Polymorphic Collections**: In `DashboardController.java`, a single `List<Transaction>` holds both `Expense` and `Income` objects and processes them uniformly.

### 6. Abstraction
- The `Transaction` class is declared `abstract` because a generic transaction cannot exist on its own without being either an Expense or Income.
- Abstract methods force child classes to supply concrete implementations.

### 7. Interfaces
- The generic interface `CrudDao<T>` defines contracts for database operations without dictating implementation details:
  ```java
  public interface CrudDao<T> {
      boolean save(T entity);
      T findById(int id);
      List<T> findAllByUserId(int userId);
      boolean update(T entity);
      boolean delete(int id);
  }
  ```

### 8. Method Overloading and Overriding
- **Method Overloading (Compile-Time)**: In `ExpenseService.java`, multiple versions of retrieval methods exist with different parameter signatures:
  - `getExpenses()`
  - `filterExpenses(String keyword, String category, LocalDate start, LocalDate end)`
- **Method Overriding (Runtime)**: `@Override` used on `toString()`, `getTransactionType()`, and DAO interface methods.

### 9. Exception Handling
- Robust `try-catch-finally` and **try-with-resources** blocks for closing JDBC and File I/O streams automatically.
- Custom exception class: `DatabaseException` extending `RuntimeException` with constructor chaining `super(message, cause)`.

### 10. Collections Framework
- `List<Expense>` and `List<Transaction>` (`ArrayList`) for tabular transactions.
- `Map<String, Double>` (`LinkedHashMap`) for preserving monthly trend order in BarCharts.
- `Arrays.asList(...)` for constant category lists.

### 11. JDBC (Java Database Connectivity)
- `DriverManager.getConnection(...)` for database connections.
- Parameterized `PreparedStatement` with `?` place-holders to completely prevent SQL Injection.
- `ResultSet` navigation for parsing query rows into Java domain objects.

### 12. File Handling (Java I/O)
- `FileExportUtil.java` demonstrates writing formatted reports using `FileWriter` and `PrintWriter`.
- Demonstrates reading file previews using `FileReader` and `BufferedReader`.

---

## 🎤 Sample Viva Examination Questions & Answers

**Q1: Why did you use an abstract class `Transaction` instead of an interface?**  
*Answer:* `Expense` and `Income` share common state (id, amount, category, date, description) and common concrete logic (`getFormattedSummary()`). In Java, abstract classes allow sharing both state (instance variables) and code, while an interface would only declare methods. We used the interface `CrudDao<T>` where pure contract definition was needed.

**Q2: How does your application protect against SQL Injection?**  
*Answer:* We strictly use `PreparedStatement` with parameterized placeholders (`?`) for all dynamic variables. The database driver compiles the SQL structure separately from user input, treating any special characters purely as literal string data.

**Q3: How does Gemini AI know the user's spending habits without fake data?**  
*Answer:* Before making the HTTP call to Gemini, `GeminiService.java` queries the MySQL database for the user's current monthly income, total expenses, category breakdown percentages, and active budgets. It formats this data into a structured financial snapshot and injects it into the prompt context sent to Gemini via `java.net.http.HttpClient`.

**Q4: What happens if the API key is missing or the internet is disconnected?**  
*Answer:* `GeminiService.java` catches network exceptions and checks if the API key is configured. If unavailable, it seamlessly triggers an offline intelligent financial analyzer that computes real metrics directly from the local MySQL database and answers the student's question accurately.

**Q5: What is the benefit of the Singleton pattern in `DatabaseConnection` and `SessionManager`?**  
*Answer:* The Singleton pattern guarantees that only a single instance of `DatabaseConnection` and `SessionManager` exists throughout the application lifecycle. This centralizes connection configuration and provides a consistent global reference to the currently authenticated user.

---

## 👥 Authors
- **SmartSpend Project Team** (BTech Computer Science & Engineering)
- Developed with Java 21 & JavaFX.
