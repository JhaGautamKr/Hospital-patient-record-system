package com.hospital.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection handles JDBC connection to MySQL database (hospital_db).
 * Uses standard JDBC Driver Manager.
 */
public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/hospital_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";

    // Common candidate passwords to automatically connect on any lab or home machine
    private static final String[] CANDIDATE_PASSWORDS = {
        "@Madhubani123",
        "root",
        "password",
        "1234",
        "admin",
        ""
    };

    private static String activePassword = null;
    private static String activeUrl = DEFAULT_URL;
    private static String activeUser = DEFAULT_USER;

    static {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found: " + e.getMessage());
        }

        // Allow environment variable overrides
        String envUser = System.getenv("MYSQL_USER");
        String envPass = System.getenv("MYSQL_PASSWORD");
        String envUrl = System.getenv("MYSQL_URL");

        if (envUser != null && !envUser.isEmpty()) activeUser = envUser;
        if (envUrl != null && !envUrl.isEmpty()) activeUrl = envUrl;

        if (envPass != null) {
            activePassword = envPass;
        } else {
            // Test connection candidates
            for (String pass : CANDIDATE_PASSWORDS) {
                try (Connection conn = DriverManager.getConnection(activeUrl, activeUser, pass)) {
                    if (conn != null && !conn.isClosed()) {
                        activePassword = pass;
                        break;
                    }
                } catch (SQLException ignored) {
                }
            }
            if (activePassword == null) {
                activePassword = "@Madhubani123";
            }
        }
    }

    /**
     * Get a live Connection to MySQL hospital_db
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(activeUrl, activeUser, activePassword);
    }

    /**
     * Helper to verify if database is currently reachable
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getActiveUser() {
        return activeUser;
    }

    public static String getActiveUrl() {
        return activeUrl;
    }
}
