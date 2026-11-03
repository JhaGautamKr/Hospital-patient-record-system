package com.hospital.model;

/**
 * Model class representing a Patient entity in the PATIENT table.
 * Fields match SRS Section 3.6.1: PatientID, Name, DOB, Gender, Phone, Address, UserID.
 */
public class Patient {
    private int patientId;
    private String name;
    private String dob;        // YYYY-MM-DD
    private String gender;     // Male, Female, Other
    private String phone;
    private String address;
    private Integer userId;

    public Patient() {
    }

    public Patient(int patientId, String name, String dob, String gender, String phone, String address, Integer userId) {
        this.patientId = patientId;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.userId = userId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
