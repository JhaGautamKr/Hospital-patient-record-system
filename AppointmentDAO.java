package com.hospital.dao;

import com.hospital.model.Appointment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for APPOINTMENT table.
 * Handles Appointment Management (FR5): booking, viewing, rescheduling, cancelling,
 * and checking doctor availability to prevent double-booking.
 */
public class AppointmentDAO {

    /**
     * FR5: Get all appointments with Patient and Doctor names joined
     */
    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.AppointmentID, a.PatientID, a.DoctorID, a.AppDate, a.AppTime, a.Status, " +
                     "p.Name AS PatientName, d.Name AS DoctorName, d.Specialization AS DoctorSpec " +
                     "FROM appointment a " +
                     "JOIN patient p ON a.PatientID = p.PatientID " +
                     "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                     "ORDER BY a.AppDate DESC, a.AppTime DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getAllAppointments error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get appointments for a specific Doctor
     */
    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.AppointmentID, a.PatientID, a.DoctorID, a.AppDate, a.AppTime, a.Status, " +
                     "p.Name AS PatientName, d.Name AS DoctorName, d.Specialization AS DoctorSpec " +
                     "FROM appointment a " +
                     "JOIN patient p ON a.PatientID = p.PatientID " +
                     "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                     "WHERE a.DoctorID = ? " +
                     "ORDER BY a.AppDate DESC, a.AppTime DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getAppointmentsByDoctor error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get appointments for a specific Patient
     */
    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.AppointmentID, a.PatientID, a.DoctorID, a.AppDate, a.AppTime, a.Status, " +
                     "p.Name AS PatientName, d.Name AS DoctorName, d.Specialization AS DoctorSpec " +
                     "FROM appointment a " +
                     "JOIN patient p ON a.PatientID = p.PatientID " +
                     "JOIN doctor d ON a.DoctorID = d.DoctorID " +
                     "WHERE a.PatientID = ? " +
                     "ORDER BY a.AppDate DESC, a.AppTime DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getAppointmentsByPatient error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Check if Doctor is already booked at the requested date and time (Prevents Double-Booking)
     */
    public boolean isDoctorAvailable(int doctorId, String appDate, String appTime, int excludeAppointmentId) {
        String sql = "SELECT COUNT(*) FROM appointment WHERE DoctorID = ? AND AppDate = ? AND AppTime = ? AND Status = 'Scheduled' AND AppointmentID != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(appDate));
            ps.setTime(3, Time.valueOf(formatTime(appTime)));
            ps.setInt(4, excludeAppointmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] isDoctorAvailable error: " + e.getMessage());
        }
        return true;
    }

    /**
     * FR5: Book a new appointment
     * Returns: generated AppointmentID on success, -2 if doctor already booked, -1 on failure
     */
    public int bookAppointment(Appointment appt) {
        if (!isDoctorAvailable(appt.getDoctorId(), appt.getAppDate(), appt.getAppTime(), 0)) {
            return -2; // Conflict / Double-booking
        }

        String sql = "INSERT INTO appointment (PatientID, DoctorID, AppDate, AppTime, Status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, appt.getPatientId());
            ps.setInt(2, appt.getDoctorId());
            ps.setDate(3, Date.valueOf(appt.getAppDate()));
            ps.setTime(4, Time.valueOf(formatTime(appt.getAppTime())));
            ps.setString(5, appt.getStatus() != null && !appt.getStatus().isEmpty() ? appt.getStatus() : "Scheduled");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int genId = rs.getInt(1);
                        appt.setAppointmentId(genId);
                        return genId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] bookAppointment error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * FR5: Update appointment status (Scheduled / Completed / Cancelled)
     */
    public boolean updateStatus(int appointmentId, String status) {
        String sql = "UPDATE appointment SET Status = ? WHERE AppointmentID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] updateStatus error: " + e.getMessage());
        }
        return false;
    }

    /**
     * FR5: Reschedule appointment date and time
     */
    public boolean rescheduleAppointment(int appointmentId, int doctorId, String newDate, String newTime) {
        if (!isDoctorAvailable(doctorId, newDate, newTime, appointmentId)) {
            return false;
        }

        String sql = "UPDATE appointment SET AppDate = ?, AppTime = ?, Status = 'Scheduled' WHERE AppointmentID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(newDate));
            ps.setTime(2, Time.valueOf(formatTime(newTime)));
            ps.setInt(3, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] rescheduleAppointment error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete appointment
     */
    public boolean deleteAppointment(int appointmentId) {
        String sql = "DELETE FROM appointment WHERE AppointmentID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] deleteAppointment error: " + e.getMessage());
        }
        return false;
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("AppointmentID"));
        a.setPatientId(rs.getInt("PatientID"));
        a.setDoctorId(rs.getInt("DoctorID"));

        Date d = rs.getDate("AppDate");
        a.setAppDate(d != null ? d.toString() : "");

        Time t = rs.getTime("AppTime");
        a.setAppTime(t != null ? t.toString() : "");

        a.setStatus(rs.getString("Status"));
        a.setPatientName(rs.getString("PatientName"));
        a.setDoctorName(rs.getString("DoctorName"));
        a.setDoctorSpecialization(rs.getString("DoctorSpec"));
        return a;
    }

    private String formatTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return "10:00:00";
        timeStr = timeStr.trim();
        if (timeStr.length() == 5) {
            return timeStr + ":00";
        }
        return timeStr;
    }
}
