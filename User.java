package com.hospital.model;

/**
 * Model class representing a User entity in the USER table.
 * Used for authentication and role-based access control.
 */
public class User {
    private int userId;
    private String name;
    private String role;     // Admin, Doctor, Nurse, Receptionist, Patient
    private String username;
    private String password;

    public User() {
    }

    public User(int userId, String name, String role, String username, String password) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.username = username;
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
