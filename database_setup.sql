-- =========================================================================
-- HOSPITAL PATIENT RECORD SYSTEM
-- Database Setup Script (MySQL)
-- Database Name: hospital_db
-- =========================================================================

CREATE DATABASE IF NOT EXISTS `hospital_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `hospital_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------------------------------------
-- 1. USER Table (Section 3.6.1)
-- Stores login credentials and system roles for Admin, Doctor, Nurse, Receptionist, Patient
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `UserID` INT NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(100) NOT NULL,
  `Role` ENUM('Admin', 'Doctor', 'Nurse', 'Receptionist', 'Patient') NOT NULL,
  `Username` VARCHAR(50) NOT NULL UNIQUE,
  `Password` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------------
-- 2. PATIENT Table (Section 3.6.1)
-- Stores patient personal details and demographic information
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `patient` (
  `PatientID` INT NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(100) NOT NULL,
  `DOB` DATE NOT NULL,
  `Gender` VARCHAR(20) NOT NULL,
  `Phone` VARCHAR(20) NOT NULL,
  `Address` VARCHAR(255) NOT NULL,
  `UserID` INT DEFAULT NULL UNIQUE,
  PRIMARY KEY (`PatientID`),
  CONSTRAINT `fk_patient_user` FOREIGN KEY (`UserID`) REFERENCES `user` (`UserID`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------------
-- 3. DOCTOR Table (Section 3.6.1)
-- Stores doctor profiles, medical specializations, and contact details
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `doctor` (
  `DoctorID` INT NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(100) NOT NULL,
  `Specialization` VARCHAR(100) NOT NULL,
  `Phone` VARCHAR(20) NOT NULL,
  `UserID` INT DEFAULT NULL UNIQUE,
  PRIMARY KEY (`DoctorID`),
  CONSTRAINT `fk_doctor_user` FOREIGN KEY (`UserID`) REFERENCES `user` (`UserID`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------------
-- 4. APPOINTMENT Table (Section 3.6.1)
-- Connects Patient and Doctor for scheduled visits
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appointment` (
  `AppointmentID` INT NOT NULL AUTO_INCREMENT,
  `PatientID` INT NOT NULL,
  `DoctorID` INT NOT NULL,
  `AppDate` DATE NOT NULL,
  `AppTime` TIME NOT NULL,
  `Status` ENUM('Scheduled', 'Completed', 'Cancelled') NOT NULL DEFAULT 'Scheduled',
  PRIMARY KEY (`AppointmentID`),
  KEY `idx_appointment_patient` (`PatientID`),
  KEY `idx_appointment_doctor` (`DoctorID`),
  CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`PatientID`) REFERENCES `patient` (`PatientID`) ON DELETE CASCADE,
  CONSTRAINT `fk_appointment_doctor` FOREIGN KEY (`DoctorID`) REFERENCES `doctor` (`DoctorID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------------
-- 5. MEDICAL RECORD Table (Section 3.6.1)
-- Stores patient consultation details, diagnosis, treatment, and clinical notes
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `medical_record` (
  `RecordID` INT NOT NULL AUTO_INCREMENT,
  `PatientID` INT NOT NULL,
  `DoctorID` INT NOT NULL,
  `VisitDate` DATE NOT NULL,
  `Diagnosis` TEXT NOT NULL,
  `Treatment` TEXT NOT NULL,
  `Notes` TEXT,
  PRIMARY KEY (`RecordID`),
  KEY `idx_record_patient` (`PatientID`),
  KEY `idx_record_doctor` (`DoctorID`),
  CONSTRAINT `fk_record_patient` FOREIGN KEY (`PatientID`) REFERENCES `patient` (`PatientID`) ON DELETE CASCADE,
  CONSTRAINT `fk_record_doctor` FOREIGN KEY (`DoctorID`) REFERENCES `doctor` (`DoctorID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- -------------------------------------------------------------------------
-- SAMPLE SEED DATA
-- Default User Logins for College Viva & Testing:
-- 1. Admin        : admin        / admin123
-- 2. Doctor       : dr_sharma    / doctor123
-- 3. Doctor       : dr_verma     / doctor123
-- 4. Nurse        : nurse_priya  / nurse123
-- 5. Receptionist : recep_amit   / recep123
-- 6. Patient      : patient_rahul/ patient123
-- -------------------------------------------------------------------------

INSERT INTO `user` (`UserID`, `Name`, `Role`, `Username`, `Password`) VALUES
(1, 'System Administrator', 'Admin', 'admin', 'admin123'),
(2, 'Dr. Rajesh Sharma', 'Doctor', 'dr_sharma', 'doctor123'),
(3, 'Dr. Sunita Verma', 'Doctor', 'dr_verma', 'doctor123'),
(4, 'Nurse Priya Sharma', 'Nurse', 'nurse_priya', 'nurse123'),
(5, 'Amit Kumar', 'Receptionist', 'recep_amit', 'recep123'),
(6, 'Rahul Verma', 'Patient', 'patient_rahul', 'patient123')
ON DUPLICATE KEY UPDATE `Name`=VALUES(`Name`), `Password`=VALUES(`Password`);

INSERT INTO `doctor` (`DoctorID`, `Name`, `Specialization`, `Phone`, `UserID`) VALUES
(1, 'Dr. Rajesh Sharma', 'Cardiology', '9876543211', 2),
(2, 'Dr. Sunita Verma', 'Pediatrics', '9876543212', 3)
ON DUPLICATE KEY UPDATE `Name`=VALUES(`Name`), `Specialization`=VALUES(`Specialization`), `Phone`=VALUES(`Phone`);

INSERT INTO `patient` (`PatientID`, `Name`, `DOB`, `Gender`, `Phone`, `Address`, `UserID`) VALUES
(1, 'Rahul Verma', '1990-05-15', 'Male', '9876543215', '123 Park Street, City', 6),
(2, 'Anita Roy', '1985-08-22', 'Female', '9812345678', '45 MG Road, City', NULL)
ON DUPLICATE KEY UPDATE `Name`=VALUES(`Name`), `DOB`=VALUES(`DOB`), `Gender`=VALUES(`Gender`), `Phone`=VALUES(`Phone`), `Address`=VALUES(`Address`);

INSERT INTO `appointment` (`AppointmentID`, `PatientID`, `DoctorID`, `AppDate`, `AppTime`, `Status`) VALUES
(1, 1, 1, '2026-10-28', '10:00:00', 'Scheduled'),
(2, 2, 2, '2026-10-29', '11:30:00', 'Scheduled'),
(3, 1, 2, '2026-10-20', '09:30:00', 'Completed')
ON DUPLICATE KEY UPDATE `Status`=VALUES(`Status`);

INSERT INTO `medical_record` (`RecordID`, `PatientID`, `DoctorID`, `VisitDate`, `Diagnosis`, `Treatment`, `Notes`) VALUES
(1, 1, 1, '2026-10-20', 'Mild Hypertension', 'Amlodipine 5mg once daily, Low sodium diet', 'Patient advised 30 mins daily brisk walk. BP 138/88 mmHg. Follow up in 2 weeks.'),
(2, 2, 2, '2026-10-22', 'Seasonal Bronchitis', 'Azithromycin 500mg daily, Levocetirizine 5mg', 'Vitals normal, lungs clear. Warm steam inhalation and adequate rest recommended.')
ON DUPLICATE KEY UPDATE `Diagnosis`=VALUES(`Diagnosis`), `Treatment`=VALUES(`Treatment`), `Notes`=VALUES(`Notes`);
