package com.hospital.dao;

import com.hospital.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for PATIENT table.
 * Handles Patient Management (FR3) and Search Records (FR7).
 */
public class PatientDAO {

    /**
     * FR3: Get all registered patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT PatientID, Name, DOB, Gender, Phone, Address, UserID FROM patient ORDER BY PatientID DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] getAllPatients error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get patient by ID
     */
    public Patient getPatientById(int patientId) {
        String sql = "SELECT PatientID, Name, DOB, Gender, Phone, Address, UserID FROM patient WHERE PatientID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] getPatientById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get patient by UserID (used when logged-in patient views own records)
     */
    public Patient getPatientByUserId(int userId) {
        String sql = "SELECT PatientID, Name, DOB, Gender, Phone, Address, UserID FROM patient WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] getPatientByUserId error: " + e.getMessage());
        }
        return null;
    }

    /**
     * FR3: Add new patient
     * Inputs: Name, DOB, Gender, Phone, Address
     * Output: New unique PatientID
     */
    public int addPatient(Patient p) {
        String sql = "INSERT INTO patient (Name, DOB, Gender, Phone, Address, UserID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getName());
            ps.setDate(2, Date.valueOf(p.getDob()));
            ps.setString(3, p.getGender());
            ps.setString(4, p.getPhone());
            ps.setString(5, p.getAddress());

            if (p.getUserId() != null && p.getUserId() > 0) {
                ps.setInt(6, p.getUserId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        p.setPatientId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] addPatient error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * FR3: Update patient details
     */
    public boolean updatePatient(Patient p) {
        String sql = "UPDATE patient SET Name = ?, DOB = ?, Gender = ?, Phone = ?, Address = ? WHERE PatientID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setDate(2, Date.valueOf(p.getDob()));
            ps.setString(3, p.getGender());
            ps.setString(4, p.getPhone());
            ps.setString(5, p.getAddress());
            ps.setInt(6, p.getPatientId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PatientDAO] updatePatient error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete patient by ID
     */
    public boolean deletePatient(int patientId) {
        String sql = "DELETE FROM patient WHERE PatientID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PatientDAO] deletePatient error: " + e.getMessage());
        }
        return false;
    }

    /**
     * FR7: Search Patient Records
     * Search by Patient Name, PatientID, or Phone Number
     */
    public List<Patient> searchPatients(String keyword) {
        List<Patient> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPatients();
        }

        String search = "%" + keyword.trim() + "%";
        String sql = "SELECT PatientID, Name, DOB, Gender, Phone, Address, UserID FROM patient " +
                     "WHERE Name LIKE ? OR Phone LIKE ? OR CAST(PatientID AS CHAR) LIKE ? " +
                     "ORDER BY PatientID DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] searchPatients error: " + e.getMessage());
        }
        return list;
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("PatientID"));
        p.setName(rs.getString("Name"));
        Date dob = rs.getDate("DOB");
        p.setDob(dob != null ? dob.toString() : "");
        p.setGender(rs.getString("Gender"));
        p.setPhone(rs.getString("Phone"));
        p.setAddress(rs.getString("Address"));
        int uId = rs.getInt("UserID");
        p.setUserId(rs.wasNull() ? null : uId);
        return p;
    }
}
