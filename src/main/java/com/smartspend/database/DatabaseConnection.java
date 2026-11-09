package com.smartspend.database;

import com.smartspend.util.DatabaseException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection manages JDBC database connectivity for SmartSpend.
 * Demonstrates:
 * 1. Singleton Design Pattern (Private constructor, static instance)
 * 2. JDBC Connection management (DriverManager)
 * 3. Exception Handling (try-catch, custom DatabaseException)
 * 4. Properties File Handling
 * 5. Automatic Fallback mechanism (MySQL primary, embedded DB fallback)
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private boolean usingFallback = false;

    private String url;
    private String user;
    private String password;
    private String driver;

    private boolean fallbackEnabled = true;
    private String fallbackUrl;
    private String fallbackUser;
    private String fallbackPassword;

    private String activeUrl;
    private String activeUser;
    private String activePassword;

    // Private constructor ensures only one instance is created (Singleton pattern)
    private DatabaseConnection() {
        loadProperties();
        determineActiveConnection();
    }

    /**
     * Loads database configuration properties from db.properties file.
     */
    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
                this.driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/smartspend_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                this.user = props.getProperty("db.user", "root");
                this.password = props.getProperty("db.password", "root");

                this.fallbackEnabled = Boolean.parseBoolean(props.getProperty("db.fallback.enabled", "true"));
                this.fallbackUrl = props.getProperty("db.fallback.url", "jdbc:h2:mem:smartspend_db;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
                this.fallbackUser = props.getProperty("db.fallback.user", "sa");
                this.fallbackPassword = props.getProperty("db.fallback.password", "");
            } else {
                this.driver = "com.mysql.cj.jdbc.Driver";
                this.url = "jdbc:mysql://localhost:3306/smartspend_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                this.user = "root";
                this.password = "root";
                this.fallbackUrl = "jdbc:h2:mem:smartspend_db;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE";
                this.fallbackUser = "sa";
                this.fallbackPassword = "";
            }
        } catch (Exception e) {
            System.err.println("[DatabaseConnection] Warning: Could not read db.properties: " + e.getMessage());
        }
    }

    /**
     * Determines whether to connect to MySQL or fallback database.
     */
    private void determineActiveConnection() {
        // Try MySQL first
        try {
            Class.forName(driver);
            System.out.println("[DatabaseConnection] Attempting MySQL connection: " + url);
            try (Connection testConn = DriverManager.getConnection(url, user, password)) {
                this.activeUrl = url;
                this.activeUser = user;
                this.activePassword = password;
                this.usingFallback = false;
                System.out.println("[DatabaseConnection] Successfully connected to MySQL database!");
                return;
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL Driver not found on classpath: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Could not connect to MySQL server: " + e.getMessage());
        }

        // Fallback to embedded local DB
        if (fallbackEnabled) {
            try {
                System.out.println("[DatabaseConnection] Falling back to embedded database: " + fallbackUrl);
                Class.forName("org.h2.Driver");
                try (Connection testConn = DriverManager.getConnection(fallbackUrl, fallbackUser, fallbackPassword)) {
                    this.activeUrl = fallbackUrl;
                    this.activeUser = fallbackUser;
                    this.activePassword = fallbackPassword;
                    this.usingFallback = true;
                    System.out.println("[DatabaseConnection] Connected to embedded local database successfully!");
                    return;
                }
            } catch (Exception ex) {
                System.err.println("[DatabaseConnection] Fallback connection also failed: " + ex.getMessage());
                throw new DatabaseException("Could not connect to MySQL or fallback database.", ex);
            }
        }

        throw new DatabaseException("Could not connect to MySQL database at: " + url);
    }

    /**
     * Global access point for the Singleton DatabaseConnection instance.
     * @return DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Retrieves an active JDBC Connection.
     * Callers should manage the connection using try-with-resources.
     * @return active java.sql.Connection
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(activeUrl, activeUser, activePassword);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to obtain database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Returns true if using fallback embedded database.
     */
    public boolean isUsingFallback() {
        return usingFallback;
    }

    /**
     * Closes resources if needed.
     */
    public void closeConnection() {
        // Individual connections are managed by try-with-resources in DAOs
    }
}
