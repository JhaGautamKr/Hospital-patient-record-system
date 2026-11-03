package com.hospital.model;

/**
 * Model class representing a Doctor entity in the DOCTOR table.
 * Fields match SRS Section 3.6.1: DoctorID, Name, Specialization, Phone, UserID.
 */
public class Doctor {
    private int doctorId;
    private String name;
    private String specialization;
    private String phone;
    private Integer userId;

    public Doctor() {
    }

    public Doctor(int doctorId, String name, String specialization, String phone, Integer userId) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.userId = userId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
