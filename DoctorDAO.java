package com.hospital.dao;

import com.hospital.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for DOCTOR table.
 * Handles Doctor Management (FR4).
 */
public class DoctorDAO {

    /**
     * FR4: Get all doctors
     */
    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT DoctorID, Name, Specialization, Phone, UserID FROM doctor ORDER BY DoctorID ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] getAllDoctors error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get doctor by ID
     */
    public Doctor getDoctorById(int doctorId) {
        String sql = "SELECT DoctorID, Name, Specialization, Phone, UserID FROM doctor WHERE DoctorID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] getDoctorById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get doctor by UserID (used when logged-in doctor views assigned appointments)
     */
    public Doctor getDoctorByUserId(int userId) {
        String sql = "SELECT DoctorID, Name, Specialization, Phone, UserID FROM doctor WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] getDoctorByUserId error: " + e.getMessage());
        }
        return null;
    }

    /**
     * FR4: Add new doctor
     * Inputs: Name, Specialization, Phone
     * Output: New unique DoctorID
     */
    public int addDoctor(Doctor d) {
        String sql = "INSERT INTO doctor (Name, Specialization, Phone, UserID) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, d.getName());
            ps.setString(2, d.getSpecialization());
            ps.setString(3, d.getPhone());

            if (d.getUserId() != null && d.getUserId() > 0) {
                ps.setInt(4, d.getUserId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        d.setDoctorId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] addDoctor error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * FR4: Update doctor details
     */
    public boolean updateDoctor(Doctor d) {
        String sql = "UPDATE doctor SET Name = ?, Specialization = ?, Phone = ? WHERE DoctorID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getName());
            ps.setString(2, d.getSpecialization());
            ps.setString(3, d.getPhone());
            ps.setInt(4, d.getDoctorId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] updateDoctor error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete doctor by ID
     */
    public boolean deleteDoctor(int doctorId) {
        String sql = "DELETE FROM doctor WHERE DoctorID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] deleteDoctor error: " + e.getMessage());
        }
        return false;
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("DoctorID"));
        d.setName(rs.getString("Name"));
        d.setSpecialization(rs.getString("Specialization"));
        d.setPhone(rs.getString("Phone"));
        int uId = rs.getInt("UserID");
        d.setUserId(rs.wasNull() ? null : uId);
        return d;
    }
}
