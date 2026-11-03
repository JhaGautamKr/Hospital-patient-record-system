package com.hospital;

import com.hospital.dao.DBConnection;
import com.hospital.server.HospitalHttpServer;

import java.io.File;

/**
 * Main application entrypoint for Hospital Patient Record System.
 * Connects to MySQL using JDBC and launches the built-in HTTP Web Server.
 */
public class Main {

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        // Locate webroot directory
        String webroot = "webroot";
        File dir = new File(webroot);
        if (!dir.exists()) {
            File alt = new File("../webroot");
            if (alt.exists()) {
                webroot = "../webroot";
            }
        }

        System.out.println("==================================================================");
        System.out.println("      HOSPITAL PATIENT RECORD SYSTEM - JAVA BACKEND");
        System.out.println("==================================================================");

        // Verify Database Connection
        boolean dbOk = DBConnection.testConnection();
        if (dbOk) {
            System.out.println("[Database] Status   : CONNECTED to MySQL database (hospital_db)");
            System.out.println("[Database] JDBC URL : " + DBConnection.getActiveUrl());
            System.out.println("[Database] DB User  : " + DBConnection.getActiveUser());
        } else {
            System.err.println("[Database] Status   : FAILED to connect to MySQL.");
            System.err.println("[Database] Please verify MySQL service is running and database_setup.sql was run.");
        }

        try {
            HospitalHttpServer server = new HospitalHttpServer(port, webroot);
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nStopping Hospital System Server...");
                server.stop();
            }));

        } catch (Exception e) {
            System.err.println("[Server Error] Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
