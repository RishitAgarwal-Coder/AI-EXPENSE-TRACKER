package com.smartspend.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * DatabaseInitializer verifies that necessary database tables exist
 * and creates them automatically if they do not exist.
 * Also seeds initial demo data for smooth testing.
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();

            // 1. Users Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // 2. Expenses Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    amount DECIMAL(10,2) NOT NULL,
                    description VARCHAR(255),
                    expense_date DATE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            // 3. Income Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS income (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    source VARCHAR(50) NOT NULL,
                    amount DECIMAL(10,2) NOT NULL,
                    description VARCHAR(255),
                    income_date DATE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            // 4. Budgets Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    budget_month VARCHAR(7) NOT NULL,
                    amount DECIMAL(10,2) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            stmt.close();
            seedDemoDataIfEmpty(conn);

            System.out.println("[DatabaseInitializer] Database schema verified successfully.");
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Error initializing database tables: " + e.getMessage());
        }
    }

    /**
     * Seeds initial demo user and transactions if the users table is completely empty.
     */
    private static void seedDemoDataIfEmpty(Connection conn) {
        try {
            Statement checkStmt = conn.createStatement();
            ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("[DatabaseInitializer] Empty database detected. Seeding demo account and transactions...");

                // Create demo user
                PreparedStatement userStmt = conn.prepareStatement(
                    "INSERT INTO users (id, name, email, password) VALUES (?, ?, ?, ?)"
                );
                userStmt.setInt(1, 1);
                userStmt.setString(2, "Alex Johnson");
                userStmt.setString(3, "demo@smartspend.com");
                userStmt.setString(4, "password123");
                userStmt.executeUpdate();
                userStmt.close();

                // Create demo income
                Statement incStmt = conn.createStatement();
                incStmt.execute("INSERT INTO income (user_id, source, amount, description, income_date) VALUES " +
                    "(1, 'Internship', 25000.00, 'Monthly Internship Stipend', CURRENT_DATE()), " +
                    "(1, 'Allowance', 8000.00, 'Monthly Family Allowance', CURRENT_DATE()), " +
                    "(1, 'Freelancing', 5000.00, 'Frontend UI Design Project', CURRENT_DATE())");
                incStmt.close();

                // Create demo expenses
                Statement expStmt = conn.createStatement();
                expStmt.execute("INSERT INTO expenses (user_id, category, amount, description, expense_date) VALUES " +
                    "(1, 'Food', 350.00, 'College Canteen Lunch', CURRENT_DATE()), " +
                    "(1, 'Food', 850.00, 'Hostel Dinner & Snacks', CURRENT_DATE()), " +
                    "(1, 'Transport', 180.00, 'Metro Transit Card', CURRENT_DATE()), " +
                    "(1, 'Education', 1800.00, 'Java Programming Textbook', CURRENT_DATE()), " +
                    "(1, 'Shopping', 2200.00, 'Casual Sneakers', CURRENT_DATE()), " +
                    "(1, 'Entertainment', 650.00, 'Weekend Cinema Ticket', CURRENT_DATE()), " +
                    "(1, 'Healthcare', 450.00, 'Pharmacy Medicines', CURRENT_DATE()), " +
                    "(1, 'Travel', 1200.00, 'Weekend Train Ticket', CURRENT_DATE())");
                expStmt.close();

                // Create demo budgets
                Statement bgtStmt = conn.createStatement();
                bgtStmt.execute("INSERT INTO budgets (user_id, category, budget_month, amount) VALUES " +
                    "(1, 'Food', '2026-11', 5000.00), " +
                    "(1, 'Shopping', '2026-11', 3000.00), " +
                    "(1, 'Transport', '2026-11', 2000.00), " +
                    "(1, 'Education', '2026-11', 3000.00), " +
                    "(1, 'Entertainment', '2026-11', 1500.00), " +
                    "(1, 'Healthcare', '2026-11', 1000.00), " +
                    "(1, 'Travel', '2026-11', 2500.00)");
                bgtStmt.close();

                System.out.println("[DatabaseInitializer] Demo seed data created: demo@smartspend.com / password123");
            }
            rs.close();
            checkStmt.close();
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Notice during seeding: " + e.getMessage());
        }
    }
}
