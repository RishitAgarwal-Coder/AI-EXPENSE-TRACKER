package com.smartspend.dao;

import com.smartspend.database.DatabaseConnection;
import com.smartspend.model.CategorySummary;
import com.smartspend.model.Expense;
import com.smartspend.util.DatabaseException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Expense transactions.
 * Demonstrates:
 * 1. CrudDao Interface Implementation
 * 2. Complex SQL Queries (Aggregation, GROUP BY, Date filtering)
 * 3. Method Overloading (Search with multiple criteria)
 * 4. Collections Framework (List, Map, LinkedHashMap)
 */
public class ExpenseDao implements CrudDao<Expense> {

    @Override
    public boolean save(Expense expense) {
        String sql = "INSERT INTO expenses (user_id, category, amount, description, expense_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, expense.getUserId());
            stmt.setString(2, expense.getCategory());
            stmt.setDouble(3, expense.getAmount());
            stmt.setString(4, expense.getDescription());
            stmt.setDate(5, Date.valueOf(expense.getDate()));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        expense.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add expense: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Expense findById(int id) {
        String sql = "SELECT id, user_id, category, amount, description, expense_date FROM expenses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToExpense(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving expense: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Expense> findAllByUserId(int userId) {
        String sql = "SELECT id, user_id, category, amount, description, expense_date FROM expenses WHERE user_id = ? ORDER BY expense_date DESC, id DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error listing user expenses: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean update(Expense expense) {
        String sql = "UPDATE expenses SET category = ?, amount = ?, description = ?, expense_date = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, expense.getCategory());
            stmt.setDouble(2, expense.getAmount());
            stmt.setString(3, expense.getDescription());
            stmt.setDate(4, Date.valueOf(expense.getDate()));
            stmt.setInt(5, expense.getId());
            stmt.setInt(6, expense.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating expense: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting expense: " + e.getMessage(), e);
        }
    }

    /**
     * Advanced filter and search for expenses.
     * Supports search keywords, category filter, and date ranges.
     */
    public List<Expense> searchAndFilter(int userId, String keyword, String category, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("SELECT id, user_id, category, amount, description, expense_date FROM expenses WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(description) LIKE ? OR LOWER(category) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (category != null && !category.equalsIgnoreCase("All") && !category.trim().isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category);
        }

        if (startDate != null) {
            sql.append(" AND expense_date >= ?");
            params.add(Date.valueOf(startDate));
        }

        if (endDate != null) {
            sql.append(" AND expense_date <= ?");
            params.add(Date.valueOf(endDate));
        }

        sql.append(" ORDER BY expense_date DESC, id DESC");

        List<Expense> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) stmt.setInt(i + 1, (Integer) p);
                else if (p instanceof String) stmt.setString(i + 1, (String) p);
                else if (p instanceof Date) stmt.setDate(i + 1, (Date) p);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error filtering expenses: " + e.getMessage(), e);
        }
        return results;
    }

    /**
     * Calculates total expenses for a user in a given month (YYYY-MM).
     */
    public double getTotalExpensesForMonth(int userId, String yearMonth) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id = ? AND " +
                (DatabaseConnection.getInstance().isUsingFallback() 
                    ? "FORMATDATETIME(expense_date, 'yyyy-MM') = ?" 
                    : "DATE_FORMAT(expense_date, '%Y-%m') = ?");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, yearMonth);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating monthly expense: " + e.getMessage(), e);
        }
        return 0.0;
    }

    /**
     * Calculates total expenses of all time for a user.
     */
    public double getTotalExpensesAllTime(int userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating total expenses: " + e.getMessage(), e);
        }
        return 0.0;
    }

    /**
     * Retrieves recent expenses up to a specified limit.
     */
    public List<Expense> getRecentExpenses(int userId, int limit) {
        String sql = "SELECT id, user_id, category, amount, description, expense_date FROM expenses WHERE user_id = ? ORDER BY expense_date DESC, id DESC LIMIT ?";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching recent expenses: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Calculates category-wise total spending for a month or all-time (if yearMonth is null).
     */
    public List<CategorySummary> getCategorySummaries(int userId, String yearMonth) {
        List<CategorySummary> summaries = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT category, SUM(amount) AS total, COUNT(*) AS count FROM expenses WHERE user_id = ? ");
        if (yearMonth != null && !yearMonth.isEmpty()) {
            if (DatabaseConnection.getInstance().isUsingFallback()) {
                sql.append("AND FORMATDATETIME(expense_date, 'yyyy-MM') = ? ");
            } else {
                sql.append("AND DATE_FORMAT(expense_date, '%Y-%m') = ? ");
            }
        }
        sql.append("GROUP BY category ORDER BY total DESC");

        double grandTotal = 0.0;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setInt(1, userId);
            if (yearMonth != null && !yearMonth.isEmpty()) {
                stmt.setString(2, yearMonth);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String cat = rs.getString("category");
                    double total = rs.getDouble("total");
                    int count = rs.getInt("count");
                    summaries.add(new CategorySummary(cat, total, count));
                    grandTotal += total;
                }
            }

            // Calculate percentage share
            if (grandTotal > 0) {
                for (CategorySummary cs : summaries) {
                    cs.setPercentage((cs.getTotalAmount() / grandTotal) * 100.0);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching category summaries: " + e.getMessage(), e);
        }
        return summaries;
    }

    /**
     * Retrieves monthly totals for the past 6 months.
     */
    public Map<String, Double> getMonthlyTotalsForPastMonths(int userId, int monthsCount) {
        Map<String, Double> map = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = monthsCount - 1; i >= 0; i--) {
            LocalDate target = now.minusMonths(i);
            String ym = String.format("%04d-%02d", target.getYear(), target.getMonthValue());
            double total = getTotalExpensesForMonth(userId, ym);
            map.put(ym, total);
        }
        return map;
    }

    private Expense mapRowToExpense(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getDouble("amount"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDate("expense_date").toLocalDate()
        );
    }
}
