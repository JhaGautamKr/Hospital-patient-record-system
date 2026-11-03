package com.hospital.util;

import com.hospital.model.*;

import java.util.*;

/**
 * Lightweight JSON Helper for serialization and simple parsing.
 * Uses zero external libraries - 100% pure standard Java!
 */
public class JsonHelper {

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32 || c >= 127) {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - hex.length(); k++) sb.append('0');
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Parses simple JSON object string into Map<String, String>
     */
    public static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        // Simple tokenizer for key-value pairs
        boolean inQuotes = false;
        StringBuilder currentKey = new StringBuilder();
        StringBuilder currentValue = new StringBuilder();
        boolean isParsingValue = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '\\' && i + 1 < json.length()) {
                if (isParsingValue) {
                    currentValue.append(json.charAt(i + 1));
                } else {
                    currentKey.append(json.charAt(i + 1));
                }
                i++;
                continue;
            }

            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (!inQuotes) {
                if (c == ':') {
                    isParsingValue = true;
                    continue;
                } else if (c == ',') {
                    String k = currentKey.toString().trim();
                    String v = currentValue.toString().trim();
                    if (!k.isEmpty()) {
                        map.put(k, v);
                    }
                    currentKey.setLength(0);
                    currentValue.setLength(0);
                    isParsingValue = false;
                    continue;
                }
            }

            if (isParsingValue) {
                currentValue.append(c);
            } else {
                currentKey.append(c);
            }
        }

        String k = currentKey.toString().trim();
        String v = currentValue.toString().trim();
        if (!k.isEmpty()) {
            map.put(k, v);
        }

        return map;
    }

    // --- Object to JSON converters ---

    public static String userToJson(User u) {
        if (u == null) return "null";
        return "{" +
                "\"userId\":" + u.getUserId() + "," +
                "\"name\":\"" + escape(u.getName()) + "\"," +
                "\"role\":\"" + escape(u.getRole()) + "\"," +
                "\"username\":\"" + escape(u.getUsername()) + "\"" +
                "}";
    }

    public static String usersToJson(List<User> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(userToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String patientToJson(Patient p) {
        if (p == null) return "null";
        return "{" +
                "\"patientId\":" + p.getPatientId() + "," +
                "\"name\":\"" + escape(p.getName()) + "\"," +
                "\"dob\":\"" + escape(p.getDob()) + "\"," +
                "\"gender\":\"" + escape(p.getGender()) + "\"," +
                "\"phone\":\"" + escape(p.getPhone()) + "\"," +
                "\"address\":\"" + escape(p.getAddress()) + "\"," +
                "\"userId\":" + (p.getUserId() == null ? "null" : p.getUserId()) +
                "}";
    }

    public static String patientsToJson(List<Patient> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(patientToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String doctorToJson(Doctor d) {
        if (d == null) return "null";
        return "{" +
                "\"doctorId\":" + d.getDoctorId() + "," +
                "\"name\":\"" + escape(d.getName()) + "\"," +
                "\"specialization\":\"" + escape(d.getSpecialization()) + "\"," +
                "\"phone\":\"" + escape(d.getPhone()) + "\"," +
                "\"userId\":" + (d.getUserId() == null ? "null" : d.getUserId()) +
                "}";
    }

    public static String doctorsToJson(List<Doctor> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(doctorToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String appointmentToJson(Appointment a) {
        if (a == null) return "null";
        return "{" +
                "\"appointmentId\":" + a.getAppointmentId() + "," +
                "\"patientId\":" + a.getPatientId() + "," +
                "\"doctorId\":" + a.getDoctorId() + "," +
                "\"appDate\":\"" + escape(a.getAppDate()) + "\"," +
                "\"appTime\":\"" + escape(a.getAppTime()) + "\"," +
                "\"status\":\"" + escape(a.getStatus()) + "\"," +
                "\"patientName\":\"" + escape(a.getPatientName()) + "\"," +
                "\"doctorName\":\"" + escape(a.getDoctorName()) + "\"," +
                "\"doctorSpecialization\":\"" + escape(a.getDoctorSpecialization()) + "\"" +
                "}";
    }

    public static String appointmentsToJson(List<Appointment> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(appointmentToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String medicalRecordToJson(MedicalRecord m) {
        if (m == null) return "null";
        return "{" +
                "\"recordId\":" + m.getRecordId() + "," +
                "\"patientId\":" + m.getPatientId() + "," +
                "\"doctorId\":" + m.getDoctorId() + "," +
                "\"visitDate\":\"" + escape(m.getVisitDate()) + "\"," +
                "\"diagnosis\":\"" + escape(m.getDiagnosis()) + "\"," +
                "\"treatment\":\"" + escape(m.getTreatment()) + "\"," +
                "\"notes\":\"" + escape(m.getNotes()) + "\"," +
                "\"patientName\":\"" + escape(m.getPatientName()) + "\"," +
                "\"doctorName\":\"" + escape(m.getDoctorName()) + "\"" +
                "}";
    }

    public static String medicalRecordsToJson(List<MedicalRecord> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(medicalRecordToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String mapListToJson(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            Map<String, Object> map = list.get(i);
            sb.append("{");
            int j = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (j++ > 0) sb.append(",");
                sb.append("\"").append(escape(entry.getKey())).append("\":");
                Object val = entry.getValue();
                if (val == null) {
                    sb.append("null");
                } else if (val instanceof Number || val instanceof Boolean) {
                    sb.append(val);
                } else {
                    sb.append("\"").append(escape(val.toString())).append("\"");
                }
            }
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String statsToJson(Map<String, Integer> stats) {
        StringBuilder sb = new StringBuilder("{");
        int j = 0;
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            if (j++ > 0) sb.append(",");
            sb.append("\"").append(escape(entry.getKey())).append("\":").append(entry.getValue());
        }
        sb.append("}");
        return sb.toString();
    }
}
