# Hospital Patient Record System
**A Web-Based Centralized Healthcare Record Management System**

Developed for Capstone Project / Software Engineering (CMCA0303)  
NIET Greater Noida — MCA Program  

---

## 📌 Project Overview
The **Hospital Patient Record System** is a lightweight, web-based software application designed to digitally maintain and manage patient files, doctor information, appointment scheduling, consultation medical histories, and hospital reports in one centralized database.

It completely eliminates the errors, delays, and physical storage issues of manual paper-based hospital records.

---

## 🚀 How to Run the Project (One-Click)

### Quickest Way:
1. Double-click **`▶ Run Project.bat`** (or `Run_Project.bat`) located in the root folder.
2. The launcher will automatically:
   - Check your Java environment.
   - Compile Java classes if needed.
   - Start the Java HTTP server with MySQL connectivity.
   - Open **`http://localhost:8080`** automatically in your default web browser.

---

## 🔑 Default Login Credentials (Role-Based Access)

The system supports five user roles with dedicated permissions as defined in the Software Requirements Specification (SRS):

| Role | Username | Password | Access Privileges |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Full system access: Users, Patients, Doctors, Appointments, Medical Records, Search, Reports, and Database Backups. |
| **Doctor** | `dr_sharma` | `doctor123` | View assigned appointments, view patient history, record check-up diagnosis & treatment, search records. |
| **Nurse** | `nurse_priya` | `nurse123` | View patient medical records, update treatment details and notes, search records. |
| **Receptionist** | `recep_amit` | `recep123` | Register new patients, update demographics, book, reschedule, and cancel appointments, search records. |
| **Patient** | `patient_rahul` | `patient123` | View own booked appointments and personal medical consultation history timeline. |

---

## 📁 Simple Project Folder Structure

```
project_hospital record system/
├── ▶ Run Project.bat          <- One-click launcher with Play button
├── Run_Project.bat            <- Alternative standard launcher
├── compile.bat                <- Java compilation script
├── README.md                  <- Project documentation & viva guide
├── database/
│   └── database_setup.sql     <- MySQL database creation and seed data
├── lib/
│   └── mysql-connector-j-8.3.0.jar <- MySQL JDBC Driver
├── src/
│   └── com/hospital/
│       ├── Main.java          <- Application entry point
│       ├── model/             <- Model classes (User, Patient, Doctor, Appointment, MedicalRecord)
│       ├── dao/               <- JDBC Data Access Objects (DBConnection, UserDAO, PatientDAO, DoctorDAO, AppointmentDAO, MedicalRecordDAO, ReportDAO)
│       ├── server/            <- Built-in Java HTTP Server & API Handlers
│       └── util/              <- JSON formatting helper
└── webroot/                   <- Clean Frontend web interface
    ├── index.html             <- Main single-page web app with role tabs
    ├── css/
    │   └── style.css          <- Clean, beginner-friendly medical styling
    └── js/
        └── app.js             <- Frontend client logic (FR1 to FR9)
```

---

## 🗄️ Database Tables (hospital_db)

The database strictly implements the ER Diagram and SRS specifications across 5 relational tables:

1. **`user`** (`UserID`, `Name`, `Role`, `Username`, `Password`)
2. **`patient`** (`PatientID`, `Name`, `DOB`, `Gender`, `Phone`, `Address`, `UserID`)
3. **`doctor`** (`DoctorID`, `Name`, `Specialization`, `Phone`, `UserID`)
4. **`appointment`** (`AppointmentID`, `PatientID`, `DoctorID`, `AppDate`, `AppTime`, `Status`)
5. **`medical_record`** (`RecordID`, `PatientID`, `DoctorID`, `VisitDate`, `Diagnosis`, `Treatment`, `Notes`)

---

## 🎯 Functional Requirements Implemented (FR1 - FR9)

- **FR1 Login:** Secure login with role selection and authentication against MySQL.
- **FR2 Manage Users:** Admin can add new staff, assign roles, view all accounts, and remove users.
- **FR3 Manage Patients:** Front desk registers new patients with unique PatientID, updates address/phone, and lists patients.
- **FR4 Manage Doctors:** Maintain doctor profiles, specializations, and contact numbers.
- **FR5 Appointment Management:** Book visits with doctor double-booking conflict prevention, update status (Scheduled/Completed/Cancelled), and reschedule.
- **FR6 Medical Record Management:** Doctors and nurses record check-up dates, diseases (diagnosis), prescribed medicines (treatment), and notes.
- **FR7 Search Patient Records:** Fast search by Patient Name, Patient ID, or Phone number with a full history timeline modal.
- **FR8 Generate Reports:** Filter appointments and patient visits by date range and print reports.
- **FR9 Logout:** Secure session termination returning to the login screen.

---

## 🎓 College Viva Questions & Answers

**Q1: What technologies are used in this project?**  
*Answer:*  
- Frontend: Pure HTML5, CSS3, and JavaScript.  
- Backend: Java SE using standard JDBC and Java's built-in `com.sun.net.httpserver.HttpServer`.  
- Database: MySQL 8.0.  

**Q2: Why did you not use heavy frameworks like Spring Boot or React?**  
*Answer:*  
This is a core software engineering project designed to demonstrate fundamental programming concepts, clean relational database architecture, JDBC connectivity, and role-based access control without unnecessary third-party dependencies or framework bloat.

**Q3: How does the system prevent double-booking of doctors?**  
*Answer:*  
In `AppointmentDAO.java`, before inserting an appointment, the system executes `isDoctorAvailable(doctorId, appDate, appTime)`. If an active appointment already exists for the same doctor at that exact date and time, the booking is rejected with an alert to the user.

**Q4: How is role-based access control handled?**  
*Answer:*  
Each user has an assigned `Role` in the `user` table. Upon login, the system validates their role and dynamically displays only the authorized tabs and action buttons corresponding to their role (e.g. Doctors and Patients cannot access User Management; Receptionists cannot edit medical diagnoses).
