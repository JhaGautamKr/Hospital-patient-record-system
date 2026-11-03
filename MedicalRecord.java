package com.hospital.model;

/**
 * Model class representing a Medical Record entity in the MEDICAL_RECORD table.
 * Fields match SRS Section 3.6.1: RecordID, PatientID, DoctorID, VisitDate, Diagnosis, Treatment, Notes.
 * Helper fields patientName and doctorName are populated during joins for easy display.
 */
public class MedicalRecord {
    private int recordId;
    private int patientId;
    private int doctorId;
    private String visitDate;   // YYYY-MM-DD
    private String diagnosis;
    private String treatment;
    private String notes;

    // Join helper fields
    private String patientName;
    private String doctorName;

    public MedicalRecord() {
    }

    public MedicalRecord(int recordId, int patientId, int doctorId, String visitDate, String diagnosis, String treatment, String notes) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.visitDate = visitDate;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
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

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}
