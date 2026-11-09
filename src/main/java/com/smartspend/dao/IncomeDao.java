package com.smartspend.dao;

import com.smartspend.database.DatabaseConnection;
import com.smartspend.model.Income;
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
 * Data Access Object for Income transactions.
 * Demonstrates:
 * 1. CrudDao Interface Implementation
 * 2. JDBC PreparedStatement and Parameterization
 * 3. Monthly Aggregations and Date Operations
 */
public class IncomeDao implements CrudDao<Income> {

    @Override
    public boolean save(Income income) {
        String sql = "INSERT INTO income (user_id, source, amount, description, income_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, income.getUserId());
            stmt.setString(2, income.getSource());
            stmt.setDouble(3, income.getAmount());
            stmt.setString(4, income.getDescription());
            stmt.setDate(5, Date.valueOf(income.getDate()));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        income.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add income record: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Income findById(int id) {
        String sql = "SELECT id, user_id, source, amount, description, income_date FROM income WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToIncome(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving income: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Income> findAllByUserId(int userId) {
        String sql = "SELECT id, user_id, source, amount, description, income_date FROM income WHERE user_id = ? ORDER BY income_date DESC, id DESC";
        List<Income> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToIncome(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error listing user income: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean update(Income income) {
        String sql = "UPDATE income SET source = ?, amount = ?, description = ?, income_date = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, income.getSource());
            stmt.setDouble(2, income.getAmount());
            stmt.setString(3, income.getDescription());
            stmt.setDate(4, Date.valueOf(income.getDate()));
            stmt.setInt(5, income.getId());
            stmt.setInt(6, income.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating income: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM income WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting income: " + e.getMessage(), e);
        }
    }

    /**
     * Search and filter income records.
     */
    public List<Income> searchAndFilter(int userId, String keyword, String source, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("SELECT id, user_id, source, amount, description, income_date FROM income WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(description) LIKE ? OR LOWER(source) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (source != null && !source.equalsIgnoreCase("All") && !source.trim().isEmpty()) {
            sql.append(" AND source = ?");
            params.add(source);
        }

        if (startDate != null) {
            sql.append(" AND income_date >= ?");
            params.add(Date.valueOf(startDate));
        }

        if (endDate != null) {
            sql.append(" AND income_date <= ?");
            params.add(Date.valueOf(endDate));
        }

        sql.append(" ORDER BY income_date DESC, id DESC");

        List<Income> results = new ArrayList<>();
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
                    results.add(mapRowToIncome(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error filtering income records: " + e.getMessage(), e);
        }
        return results;
    }

    /**
     * Calculates total income for a specific month (YYYY-MM).
     */
    public double getTotalIncomeForMonth(int userId, String yearMonth) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM income WHERE user_id = ? AND " +
                (DatabaseConnection.getInstance().isUsingFallback() 
                    ? "FORMATDATETIME(income_date, 'yyyy-MM') = ?" 
                    : "DATE_FORMAT(income_date, '%Y-%m') = ?");

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
            throw new DatabaseException("Error calculating monthly income: " + e.getMessage(), e);
        }
        return 0.0;
    }

    /**
     * Calculates total income of all time for a user.
     */
    public double getTotalIncomeAllTime(int userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM income WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating total income: " + e.getMessage(), e);
        }
        return 0.0;
    }

    /**
     * Retrieves recent income records.
     */
    public List<Income> getRecentIncome(int userId, int limit) {
        String sql = "SELECT id, user_id, source, amount, description, income_date FROM income WHERE user_id = ? ORDER BY income_date DESC, id DESC LIMIT ?";
        List<Income> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToIncome(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving recent income: " + e.getMessage(), e);
        }
        return list;
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
            double total = getTotalIncomeForMonth(userId, ym);
            map.put(ym, total);
        }
        return map;
    }

    private Income mapRowToIncome(ResultSet rs) throws SQLException {
        return new Income(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getDouble("amount"),
                rs.getString("source"),
                rs.getString("description"),
                rs.getDate("income_date").toLocalDate()
        );
    }
}
