-- ==============================================================
-- SmartSpend - AI Expense Tracker Database Schema
-- Designed for MySQL 8.0+
-- Database: smartspend_db
-- ==============================================================

-- Create Database if not exists
CREATE DATABASE IF NOT EXISTS smartspend_db;
USE smartspend_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Expenses Table
CREATE TABLE IF NOT EXISTS expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(255),
    expense_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Income Table
CREATE TABLE IF NOT EXISTS income (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    source VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(255),
    income_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Budgets Table
CREATE TABLE IF NOT EXISTS budgets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category VARCHAR(50) NOT NULL,
    budget_month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_category_month (user_id, category, budget_month),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_expenses_user_date ON expenses (user_id, expense_date);
CREATE INDEX idx_income_user_date ON income (user_id, income_date);
CREATE INDEX idx_budgets_user_month ON budgets (user_id, budget_month);

-- ==============================================================
-- Sample Demo Data (Optional for immediate testing)
-- Demo Credentials: demo@smartspend.com / password123
-- ==============================================================

INSERT IGNORE INTO users (id, name, email, password) 
VALUES (1, 'Alex Johnson', 'demo@smartspend.com', 'password123');

-- Demo Income
INSERT IGNORE INTO income (user_id, source, amount, description, income_date) VALUES
(1, 'Part-Time Job', 25000.00, 'Monthly Internship Stipend', CURDATE()),
(1, 'Allowance', 8000.00, 'Monthly allowance from parents', CURDATE()),
(1, 'Freelancing', 5000.00, 'Web design mini-project', DATE_SUB(CURDATE(), INTERVAL 10 DAY));

-- Demo Expenses
INSERT IGNORE INTO expenses (user_id, category, amount, description, expense_date) VALUES
(1, 'Food', 350.00, 'College Canteen Lunch', CURDATE()),
(1, 'Food', 850.00, 'Dinner with roommates', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(1, 'Transport', 150.00, 'Bus pass recharge', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(1, 'Education', 1800.00, 'Java Programming Textbook', DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(1, 'Shopping', 2200.00, 'New Sneakers', DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(1, 'Entertainment', 650.00, 'Weekend Movie Ticket & Popcorn', DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(1, 'Food', 420.00, 'Subway sandwich & coffee', DATE_SUB(CURDATE(), INTERVAL 7 DAY)),
(1, 'Healthcare', 450.00, 'Medical prescription', DATE_SUB(CURDATE(), INTERVAL 9 DAY)),
(1, 'Travel', 1200.00, 'Train ticket home', DATE_SUB(CURDATE(), INTERVAL 12 DAY)),
(1, 'Education', 500.00, 'Notebooks and stationery', DATE_SUB(CURDATE(), INTERVAL 14 DAY));

-- Demo Budgets for current month
INSERT IGNORE INTO budgets (user_id, category, budget_month, amount) VALUES
(1, 'Food', DATE_FORMAT(CURDATE(), '%Y-%m'), 5000.00),
(1, 'Shopping', DATE_FORMAT(CURDATE(), '%Y-%m'), 3000.00),
(1, 'Transport', DATE_FORMAT(CURDATE(), '%Y-%m'), 2000.00),
(1, 'Education', DATE_FORMAT(CURDATE(), '%Y-%m'), 3000.00),
(1, 'Entertainment', DATE_FORMAT(CURDATE(), '%Y-%m'), 1500.00),
(1, 'Travel', DATE_FORMAT(CURDATE(), '%Y-%m'), 2500.00),
(1, 'Healthcare', DATE_FORMAT(CURDATE(), '%Y-%m'), 1000.00);
