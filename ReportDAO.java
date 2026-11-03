package com.hospital.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Generating Reports (FR8) and Dashboard Statistics.
 */
public class ReportDAO {

    /**
     * Dashboard Statistics counts
     */
    public Map<String, Integer> getDashboardStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalPatients", getCount("SELECT COUNT(*) FROM patient"));
        stats.put("totalDoctors", getCount("SELECT COUNT(*) FROM doctor"));
        stats.put("totalAppointments", getCount("SELECT COUNT(*) FROM appointment"));
        stats.put("scheduledAppointments", getCount("SELECT COUNT(*) FROM appointment WHERE Status = 'Scheduled'"));
        stats.put("totalMedicalRecords", getCount("SELECT COUNT(*) FROM medical_record"));
        stats.put("totalUsers", getCount("SELECT COUNT(*) FROM user"));
        return stats;
    }

    /**
     * FR8: Appointments Report by Date Range
     */
    public List<Map<String, Object>> getAppointmentsReport(String startDate, String endDate) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT a.AppointmentID, a.AppDate, a.AppTime, a.Status, " +
            "p.PatientID, p.Name AS PatientName, p.Phone AS PatientPhone, " +
            "d.DoctorID, d.Name AS DoctorName, d.Specialization " +
            "FROM appointment a " +
            "JOIN patient p ON a.PatientID = p.PatientID " +
            "JOIN doctor d ON a.DoctorID = d.DoctorID "
        );

        boolean hasStart = startDate != null && !startDate.trim().isEmpty();
        boolean hasEnd = endDate != null && !endDate.trim().isEmpty();

        if (hasStart && hasEnd) {
            sql.append("WHERE a.AppDate BETWEEN ? AND ? ");
        } else if (hasStart) {
            sql.append("WHERE a.AppDate >= ? ");
        } else if (hasEnd) {
            sql.append("WHERE a.AppDate <= ? ");
        }

        sql.append("ORDER BY a.AppDate DESC, a.AppTime ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (hasStart && hasEnd) {
                ps.setDate(idx++, Date.valueOf(startDate.trim()));
                ps.setDate(idx++, Date.valueOf(endDate.trim()));
            } else if (hasStart) {
                ps.setDate(idx++, Date.valueOf(startDate.trim()));
            } else if (hasEnd) {
                ps.setDate(idx++, Date.valueOf(endDate.trim()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("appointmentId", rs.getInt("AppointmentID"));
                    row.put("date", rs.getDate("AppDate").toString());
                    row.put("time", rs.getTime("AppTime").toString());
                    row.put("status", rs.getString("Status"));
                    row.put("patientId", rs.getInt("PatientID"));
                    row.put("patientName", rs.getString("PatientName"));
                    row.put("patientPhone", rs.getString("PatientPhone"));
                    row.put("doctorId", rs.getInt("DoctorID"));
                    row.put("doctorName", rs.getString("DoctorName"));
                    row.put("specialization", rs.getString("Specialization"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getAppointmentsReport error: " + e.getMessage());
        }
        return list;
    }

    /**
     * FR8: Patient Visits / Consultations Report by Date Range
     */
    public List<Map<String, Object>> getPatientVisitsReport(String startDate, String endDate) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT m.RecordID, m.VisitDate, m.Diagnosis, m.Treatment, m.Notes, " +
            "p.PatientID, p.Name AS PatientName, p.Phone AS PatientPhone, " +
            "d.DoctorID, d.Name AS DoctorName, d.Specialization " +
            "FROM medical_record m " +
            "JOIN patient p ON m.PatientID = p.PatientID " +
            "JOIN doctor d ON m.DoctorID = d.DoctorID "
        );

        boolean hasStart = startDate != null && !startDate.trim().isEmpty();
        boolean hasEnd = endDate != null && !endDate.trim().isEmpty();

        if (hasStart && hasEnd) {
            sql.append("WHERE m.VisitDate BETWEEN ? AND ? ");
        } else if (hasStart) {
            sql.append("WHERE m.VisitDate >= ? ");
        } else if (hasEnd) {
            sql.append("WHERE m.VisitDate <= ? ");
        }

        sql.append("ORDER BY m.VisitDate DESC, m.RecordID DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (hasStart && hasEnd) {
                ps.setDate(idx++, Date.valueOf(startDate.trim()));
                ps.setDate(idx++, Date.valueOf(endDate.trim()));
            } else if (hasStart) {
                ps.setDate(idx++, Date.valueOf(startDate.trim()));
            } else if (hasEnd) {
                ps.setDate(idx++, Date.valueOf(endDate.trim()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("recordId", rs.getInt("RecordID"));
                    row.put("visitDate", rs.getDate("VisitDate").toString());
                    row.put("diagnosis", rs.getString("Diagnosis"));
                    row.put("treatment", rs.getString("Treatment"));
                    row.put("notes", rs.getString("Notes"));
                    row.put("patientId", rs.getInt("PatientID"));
                    row.put("patientName", rs.getString("PatientName"));
                    row.put("patientPhone", rs.getString("PatientPhone"));
                    row.put("doctorId", rs.getInt("DoctorID"));
                    row.put("doctorName", rs.getString("DoctorName"));
                    row.put("specialization", rs.getString("Specialization"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getPatientVisitsReport error: " + e.getMessage());
        }
        return list;
    }

    private int getCount(String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getCount error: " + e.getMessage());
        }
        return 0;
    }
}
