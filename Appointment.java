package com.hospital.model;

/**
 * Model class representing an Appointment entity in the APPOINTMENT table.
 * Fields match SRS Section 3.6.1: AppointmentID, PatientID, DoctorID, AppDate, AppTime, Status.
 * Helper fields patientName and doctorName are populated during joins for easy display.
 */
public class Appointment {
    private int appointmentId;
    private int patientId;
    private int doctorId;
    private String appDate;     // YYYY-MM-DD
    private String appTime;     // HH:MM:SS
    private String status;      // Scheduled, Completed, Cancelled

    // Join helper fields
    private String patientName;
    private String doctorName;
    private String doctorSpecialization;

    public Appointment() {
    }

    public Appointment(int appointmentId, int patientId, int doctorId, String appDate, String appTime, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appDate = appDate;
        this.appTime = appTime;
        this.status = status;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getAppDate() {
        return appDate;
    }

    public void setAppDate(String appDate) {
        this.appDate = appDate;
    }

    public String getAppTime() {
        return appTime;
    }

    public void setAppTime(String appTime) {
        this.appTime = appTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }
}
