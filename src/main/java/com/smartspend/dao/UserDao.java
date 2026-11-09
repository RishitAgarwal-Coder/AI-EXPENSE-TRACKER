package com.smartspend.dao;

import com.smartspend.database.DatabaseConnection;
import com.smartspend.model.User;
import com.smartspend.util.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entities.
 * Demonstrates:
 * 1. Interface Implementation (implements CrudDao<User>)
 * 2. JDBC PreparedStatement and ResultSet handling
 * 3. Exception Handling with try-with-resources
 * 4. SQL Injection prevention via parameterized queries
 */
public class UserDao implements CrudDao<User> {

    @Override
    public boolean save(User user) {
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail().trim().toLowerCase());
            stmt.setString(3, user.getPassword());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to register user: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT id, name, email, password, created_at FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by ID: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Authenticates user using email and password.
     * @param email User email
     * @param password User password
     * @return User object if valid, null if credentials fail
     */
    public User authenticate(String email, String password) {
        String sql = "SELECT id, name, email, password, created_at FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Authentication query error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Checks if an email is already registered in the system.
     */
    public boolean isEmailTaken(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking email availability: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAllByUserId(int userId) {
        List<User> list = new ArrayList<>();
        User user = findById(userId);
        if (user != null) {
            list.add(user);
        }
        return list;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET name = ?, password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPassword());
            stmt.setInt(3, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user profile: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting user: " + e.getMessage(), e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getTimestamp("created_at")
        );
    }
}
