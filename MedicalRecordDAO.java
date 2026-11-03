package com.hospital.dao;

import com.hospital.model.MedicalRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for MEDICAL_RECORD table.
 * Handles Medical Record Management (FR6): adding check-up diagnosis, treatments,
 * notes, and viewing past medical history timeline.
 */
public class MedicalRecordDAO {

    /**
     * FR6: Get all medical records with Patient and Doctor names joined
     */
    public List<MedicalRecord> getAllRecords() {
        List<MedicalRecord> list = new ArrayList<>();
        String sql = "SELECT m.RecordID, m.PatientID, m.DoctorID, m.VisitDate, m.Diagnosis, m.Treatment, m.Notes, " +
                     "p.Name AS PatientName, d.Name AS DoctorName " +
                     "FROM medical_record m " +
                     "JOIN patient p ON m.PatientID = p.PatientID " +
                     "JOIN doctor d ON m.DoctorID = d.DoctorID " +
                     "ORDER BY m.VisitDate DESC, m.RecordID DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[MedicalRecordDAO] getAllRecords error: " + e.getMessage());
        }
        return list;
    }

    /**
     * FR6: Get medical history timeline for a specific patient
     */
    public List<MedicalRecord> getRecordsByPatient(int patientId) {
        List<MedicalRecord> list = new ArrayList<>();
        String sql = "SELECT m.RecordID, m.PatientID, m.DoctorID, m.VisitDate, m.Diagnosis, m.Treatment, m.Notes, " +
                     "p.Name AS PatientName, d.Name AS DoctorName " +
                     "FROM medical_record m " +
                     "JOIN patient p ON m.PatientID = p.PatientID " +
                     "JOIN doctor d ON m.DoctorID = d.DoctorID " +
                     "WHERE m.PatientID = ? " +
                     "ORDER BY m.VisitDate DESC, m.RecordID DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MedicalRecordDAO] getRecordsByPatient error: " + e.getMessage());
        }
        return list;
    }

    /**
     * FR6: Add new medical record
     * Inputs: PatientID, DoctorID, VisitDate, Diagnosis, Treatment, Notes
     */
    public int addRecord(MedicalRecord mr) {
        String sql = "INSERT INTO medical_record (PatientID, DoctorID, VisitDate, Diagnosis, Treatment, Notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, mr.getPatientId());
            ps.setInt(2, mr.getDoctorId());
            ps.setDate(3, Date.valueOf(mr.getVisitDate()));
            ps.setString(4, mr.getDiagnosis());
            ps.setString(5, mr.getTreatment());
            ps.setString(6, mr.getNotes() != null ? mr.getNotes() : "");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int genId = rs.getInt(1);
                        mr.setRecordId(genId);
                        return genId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[MedicalRecordDAO] addRecord error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * FR6: Update medical record (treatment details / notes by Doctor or Nurse)
     */
    public boolean updateRecord(MedicalRecord mr) {
        String sql = "UPDATE medical_record SET Diagnosis = ?, Treatment = ?, Notes = ?, VisitDate = ? WHERE RecordID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mr.getDiagnosis());
            ps.setString(2, mr.getTreatment());
            ps.setString(3, mr.getNotes());
            ps.setDate(4, Date.valueOf(mr.getVisitDate()));
            ps.setInt(5, mr.getRecordId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MedicalRecordDAO] updateRecord error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete record
     */
    public boolean deleteRecord(int recordId) {
        String sql = "DELETE FROM medical_record WHERE RecordID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MedicalRecordDAO] deleteRecord error: " + e.getMessage());
        }
        return false;
    }

    private MedicalRecord mapRow(ResultSet rs) throws SQLException {
        MedicalRecord mr = new MedicalRecord();
        mr.setRecordId(rs.getInt("RecordID"));
        mr.setPatientId(rs.getInt("PatientID"));
        mr.setDoctorId(rs.getInt("DoctorID"));

        Date d = rs.getDate("VisitDate");
        mr.setVisitDate(d != null ? d.toString() : "");

        mr.setDiagnosis(rs.getString("Diagnosis"));
        mr.setTreatment(rs.getString("Treatment"));
        mr.setNotes(rs.getString("Notes"));

        mr.setPatientName(rs.getString("PatientName"));
        mr.setDoctorName(rs.getString("DoctorName"));
        return mr;
    }
}
