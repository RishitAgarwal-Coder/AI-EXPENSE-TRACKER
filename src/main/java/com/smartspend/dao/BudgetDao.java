package com.smartspend.dao;

import com.smartspend.database.DatabaseConnection;
import com.smartspend.model.Budget;
import com.smartspend.util.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Budget entities.
 * Demonstrates:
 * 1. CrudDao Interface Implementation
 * 2. Cross-table Aggregation (Calculating spent amount vs budget limit)
 * 3. UPSERT / Duplicate-key handling
 */
public class BudgetDao implements CrudDao<Budget> {

    @Override
    public boolean save(Budget budget) {
        String sql = "INSERT INTO budgets (user_id, category, budget_month, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, budget.getUserId());
            stmt.setString(2, budget.getCategory());
            stmt.setString(3, budget.getMonth());
            stmt.setDouble(4, budget.getAmount());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        budget.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save budget: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Budget findById(int id) {
        String sql = "SELECT id, user_id, category, budget_month, amount FROM budgets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBudget(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving budget: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Budget> findAllByUserId(int userId) {
        String sql = "SELECT id, user_id, category, budget_month, amount FROM budgets WHERE user_id = ? ORDER BY budget_month DESC, category ASC";
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBudget(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error listing user budgets: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean update(Budget budget) {
        String sql = "UPDATE budgets SET amount = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, budget.getAmount());
            stmt.setInt(2, budget.getId());
            stmt.setInt(3, budget.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating budget: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting budget: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all budgets for a given month with their actual spent amounts calculated.
     */
    public List<Budget> getBudgetsWithSpentForMonth(int userId, String month) {
        List<Budget> list = new ArrayList<>();
        // Query joins or computes the sum of expenses for each category in the specified month
        String sql;
        if (DatabaseConnection.getInstance().isUsingFallback()) {
            sql = "SELECT b.id, b.user_id, b.category, b.budget_month, b.amount, " +
                  "COALESCE((SELECT SUM(e.amount) FROM expenses e WHERE e.user_id = b.user_id AND e.category = b.category AND FORMATDATETIME(e.expense_date, 'yyyy-MM') = b.budget_month), 0.0) AS spent " +
                  "FROM budgets b WHERE b.user_id = ? AND b.budget_month = ? ORDER BY b.category ASC";
        } else {
            sql = "SELECT b.id, b.user_id, b.category, b.budget_month, b.amount, " +
                  "COALESCE((SELECT SUM(e.amount) FROM expenses e WHERE e.user_id = b.user_id AND e.category = b.category AND DATE_FORMAT(e.expense_date, '%Y-%m') = b.budget_month), 0.0) AS spent " +
                  "FROM budgets b WHERE b.user_id = ? AND b.budget_month = ? ORDER BY b.category ASC";
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, month);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Budget b = mapRowToBudget(rs);
                    b.setSpentAmount(rs.getDouble("spent"));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving monthly budgets with spent totals: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Saves or updates a budget for a category and month.
     */
    public boolean saveOrUpdate(Budget budget) {
        String checkSql = "SELECT id FROM budgets WHERE user_id = ? AND category = ? AND budget_month = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, budget.getUserId());
            checkStmt.setString(2, budget.getCategory());
            checkStmt.setString(3, budget.getMonth());

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    budget.setId(rs.getInt("id"));
                    return update(budget);
                } else {
                    return save(budget);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error setting budget: " + e.getMessage(), e);
        }
    }

    private Budget mapRowToBudget(ResultSet rs) throws SQLException {
        return new Budget(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("category"),
                rs.getString("budget_month"),
                rs.getDouble("amount")
        );
    }
}
