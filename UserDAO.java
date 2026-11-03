package com.hospital.dao;

import com.hospital.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for USER table.
 * Handles Authentication (FR1) and User Management (FR2).
 */
public class UserDAO {

    /**
     * FR1: Login function
     * Validates username, password, and role.
     */
    public User login(String username, String password) {
        //String sql = "SELECT UserID, Name, Role, Username FROM user WHERE Username = ? AND Role = ? AND (Password = ? OR Password = SHA2(?, 256))";
        String sql = "SELECT UserID, Name, Role, Username FROM user WHERE Username = ? AND (Password = ? OR Password = SHA2(?, 256))";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            //ps.setString(1, username.trim());
            //ps.setString(2, role.trim());
            //ps.setString(3, password.trim());
            //ps.setString(4, password.trim());
            ps.setString(1, username.trim());
            ps.setString(2, password.trim());
            ps.setString(3, password.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("UserID"));
                    u.setName(rs.getString("Name"));
                    u.setRole(rs.getString("Role"));
                    u.setUsername(rs.getString("Username"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Login error: " + e.getMessage());
        }
        return null;
    }

    /**
     * FR2: Get all users (Admin view)
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT UserID, Name, Role, Username FROM user ORDER BY UserID ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("UserID"));
                u.setName(rs.getString("Name"));
                u.setRole(rs.getString("Role"));
                u.setUsername(rs.getString("Username"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }
        return list;
    }

    /**
     * FR2: Add new user
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO user (Name, Role, Username, Password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getPassword());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] addUser error: " + e.getMessage());
        }
        return false;
    }

    /**
     * FR2: Update user details
     */
    public boolean updateUser(User user) {
        StringBuilder sql = new StringBuilder("UPDATE user SET Name = ?, Role = ?, Username = ?");
        boolean hasPassword = user.getPassword() != null && !user.getPassword().trim().isEmpty();
        if (hasPassword) {
            sql.append(", Password = ?");
        }
        sql.append(" WHERE UserID = ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getUsername());

            int idx = 4;
            if (hasPassword) {
                ps.setString(idx++, user.getPassword().trim());
            }
            ps.setInt(idx, user.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] updateUser error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete user by ID
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM user WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] deleteUser error: " + e.getMessage());
        }
        return false;
    }
}
