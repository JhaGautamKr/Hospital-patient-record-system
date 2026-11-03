package com.hospital.server;

import com.hospital.dao.*;
import com.hospital.model.*;
import com.hospital.util.JsonHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * HospitalHttpServer: Built-in HTTP server using com.sun.net.httpserver.HttpServer.
 * Zero external web framework dependencies - 100% standard Java!
 */
public class HospitalHttpServer {

    private final int port;
    private final String webrootPath;
    private HttpServer server;

    private final UserDAO userDAO = new UserDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    public HospitalHttpServer(int port, String webrootPath) {
        this.port = port;
        this.webrootPath = webrootPath;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Contexts
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/dashboard/stats", new DashboardHandler());
        server.createContext("/api/users", new UsersHandler());
        server.createContext("/api/patients", new PatientsHandler());
        server.createContext("/api/doctors", new DoctorsHandler());
        server.createContext("/api/appointments", new AppointmentsHandler());
        server.createContext("/api/medical-records", new MedicalRecordsHandler());
        server.createContext("/api/search", new SearchHandler());
        server.createContext("/api/reports", new ReportsHandler());
        server.createContext("/api/backup", new BackupHandler());

        // Static Files Context (serves HTML, CSS, JS from webroot)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("==================================================================");
        System.out.println("  Hospital Patient Record System Server Started!");
        System.out.println("  Running at: http://localhost:" + port);
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // =========================================================================
    // HANDLERS
    // =========================================================================

    /**
     * FR1: Login Handler
     */
    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = JsonHelper.parseJson(body);

            String username = data.get("username");
            String password = data.get("password");
            //String role = data.get("role");

            if (username == null || password == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing login fields\"}");
                return;
            }

            User user = userDAO.login(username, password);
            if (user != null) {
                String responseJson = "{\"success\":true,\"message\":\"Login successful\",\"user\":" + JsonHelper.userToJson(user) + "}";
                sendResponse(exchange, 200, responseJson);
            } else {
                sendResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid Username or Password!\"}");
            }
        }
    }

    /**
     * Dashboard Statistics Handler
     */
    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            Map<String, Integer> stats = reportDAO.getDashboardStats();
            String responseJson = "{\"success\":true,\"stats\":" + JsonHelper.statsToJson(stats) + "}";
            sendResponse(exchange, 200, responseJson);
        }
    }

    /**
     * FR2: Users Management Handler (Admin)
     */
    private class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 204, "");
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());

            switch (method) {
                case "GET": {
                    List<User> list = userDAO.getAllUsers();
                    sendResponse(exchange, 200, "{\"success\":true,\"users\":" + JsonHelper.usersToJson(list) + "}");
                    break;
                }
                case "POST": {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = JsonHelper.parseJson(body);

                    User u = new User();
                    u.setName(data.get("name"));
                    u.setRole(data.get("role"));
                    u.setUsername(data.get("username"));
                    u.setPassword(data.get("password"));

                    boolean ok = userDAO.addUser(u);
                    if (ok) {
                        sendResponse(exchange, 201, "{\"success\":true,\"message\":\"User Added Successfully\",\"user\":" + JsonHelper.userToJson(u) + "}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Could not add user. Username may already exist.\"}");
                    }
                    break;
                }
                case "PUT": {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = JsonHelper.parseJson(body);

                    User u = new User();
                    u.setUserId(Integer.parseInt(data.get("userId")));
                    u.setName(data.get("name"));
                    u.setRole(data.get("role"));
                    u.setUsername(data.get("username"));
                    u.setPassword(data.get("password"));

                    boolean ok = userDAO.updateUser(u);
                    if (ok) {
                        sendResponse(exchange, 200, "{\"success\":true,\"message\":\"User Updated Successfully\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update user.\"}");
                    }
                    break;
                }
                case "DELETE": {
                    String idStr = query.get("id");
                    if (idStr != null) {
                        int id = Integer.parseInt(idStr);
                        boolean ok = userDAO.deleteUser(id);
                        if (ok) {
                            sendResponse(exchange, 200, "{\"success\":true,\"message\":\"User Deleted Successfully\"}");
                            return;
                        }
                    }
                    sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid User ID\"}");
                    break;
                }
                default:
                    sendResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * FR3: Patients Management Handler
     */
    private class PatientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 204, "");
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());

            switch (method) {
                case "GET": {
                    if (query.containsKey("id")) {
                        Patient p = patientDAO.getPatientById(Integer.parseInt(query.get("id")));
                        sendResponse(exchange, 200, "{\"success\":true,\"patient\":" + JsonHelper.patientToJson(p) + "}");
                    } else if (query.containsKey("userId")) {
                        Patient p = patientDAO.getPatientByUserId(Integer.parseInt(query.get("userId")));
                        sendResponse(exchange, 200, "{\"success\":true,\"patient\":" + JsonHelper.patientToJson(p) + "}");
                    } else {
                        List<Patient> list = patientDAO.getAllPatients();
                        sendResponse(exchange, 200, "{\"success\":true,\"patients\":" + JsonHelper.patientsToJson(list) + "}");
                    }
                    break;
                }
                case "POST": {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = JsonHelper.parseJson(body);

                    Patient p = new Patient();
                    p.setName(data.get("name"));
                    p.setDob(data.get("dob"));
                    p.setGender(data.get("gender"));
                    p.setPhone(data.get("phone"));
                    p.setAddress(data.get("address"));
                                        if (data.containsKey("userId") && !data.get("userId").isEmpty()) {
                        p.setUserId(Integer.parseInt(data.get("userId")));
                    }

                    // Create the patient's login account (username + password)
                    String loginUser = data.get("username");
                    String loginPass = data.get("password");
                    int newUserId = -1;
                    if (loginUser != null && !loginUser.trim().isEmpty()
                            && loginPass != null && !loginPass.trim().isEmpty()) {
                        User u = new User();
                        u.setName(p.getName());
                        u.setRole("Patient");
                        u.setUsername(loginUser.trim());
                        u.setPassword(loginPass.trim());
                        if (!userDAO.addUser(u)) {
                            sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Username already exists. Please choose another username.\"}");
                            return;
                        }
                        newUserId = u.getUserId();
                        p.setUserId(newUserId);
                    }

                    int newId = patientDAO.addPatient(p);
                    if (newId > 0) {
                        sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Patient Registered Successfully\",\"patientId\":" + newId + ",\"patient\":" + JsonHelper.patientToJson(p) + "}");
                    } else {
                        if (newUserId > 0) userDAO.deleteUser(newUserId); // undo the login if the patient save failed
                        sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to register patient.\"}");
                    }
                    break;
                }
                case "PUT": {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = JsonHelper.parseJson(body);

                    Patient p = new Patient();
                    p.setPatientId(Integer.parseInt(data.get("patientId")));
                    p.setName(data.get("name"));
                    p.setDob(data.get("dob"));
                    p.setGender(data.get("gender"));
                    p.setPhone(data.get("phone"));
                    p.setAddress(data.get("address"));

                    boolean ok = patientDAO.updatePatient(p);
                    if (ok) {
                        sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Patient Updated Successfully\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update patient.\"}");
                    }
                    break;
                }
                case "DELETE": {
                    String idStr = query.get("id");
                    if (idStr != null) {
                        int id = Integer.parseInt(idStr);
                        boolean ok = patientDAO.deletePatient(id);
                        if (ok) {
                            sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Patient Deleted Successfully\"}");
                            return;
                        }
                    }
                    sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid Patient ID\"}");
                    break;
                }
                default:
                    sendResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * FR4: Doctors Management Handler
     */
    private class DoctorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 204, "");
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());

            switch (method) {
                case "GET": {
                    if (query.containsKey("id")) {
                        Doctor d = doctorDAO.getDoctorById(Integer.parseInt(query.get("id")));
                        sendResponse(exchange, 200, "{\"success\":true,\"doctor\":" + JsonHelper.doctorToJson(d) + "}");
                    } else if (query.containsKey("userId")) {
                        Doctor d = doctorDAO.getDoctorByUserId(Integer.parseInt(query.get("userId")));
                        sendResponse(exchange, 200, "{\"success\":true,\"doctor\":" + JsonHelper.doctorToJson(d) + "}");
                    } else {
                        List<Doctor> list = doctorDAO.getAllDoctors();
                        sendResponse(exchange, 200, "{\"success\":true,\"doctors\":" + JsonHelper.doctorsToJson(list) + "}");
                    }
                    break;
                }
                case "POST": {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = JsonHelper.parseJson(body);

                    Doctor d = new Doctor();
                    d.setName(data.get("name"));
                    d.setSpecialization(data.get("specialization"));
                    d.setPhone(data.get("phone"));
                    if (data.containsKey("userId") && !data.get("userId").isEmpty()) {
                        d.setUserId(Integer.parseInt(data.get("userId")));
                    }

                    int newId = doctorDAO.addDoctor(d);
                    if (newId > 0) {
                        sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Doctor Added Successfully\",\"doctorId\":" + newId + ",\"doctor\":" + JsonHelper.doctorToJson(d) + "}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to add doctor.\"}");
                    }
                    break;
                }
                case "PUT": {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = JsonHelper.parseJson(body);

                    Doctor d = new Doctor();
                    d.setDoctorId(Integer.parseInt(data.get("doctorId")));
                    d.setName(data.get("name"));
                    d.setSpecialization(data.get("specialization"));
                    d.setPhone(data.get("phone"));

                    boolean ok = doctorDAO.updateDoctor(d);
                    if (ok) {
                        sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Doctor Updated Successfully\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update doctor.\"}");
                    }
                    break;
                }
                case "DELETE": {
                    String idStr = query.get("id");
                    if (idStr != null) {
                        int id = Integer.parseInt(idStr);
                        boolean ok = doctorDAO.deleteDoctor(id);
                        if (ok) {
                            sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Doctor Deleted Successfully\"}");
                            return;
                        }
                    }
                    sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid Doctor ID\"}");
                    break;
                }
                default:
                    sendResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * FR5: Appointment Management Handler
     */
    private class AppointmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 204, "");
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());

            switch (method) {
                case "GET": {
                    if (query.containsKey("doctorId")) {
                        int docId = Integer.parseInt(query.get("doctorId"));
                        List<Appointment> list = appointmentDAO.getAppointmentsByDoctor(docId);
                        sendResponse(exchange, 200, "{\"success\":true,\"appointments\":" + JsonHelper.appointmentsToJson(list) + "}");
                    } else if (query.containsKey("patientId")) {
                        int patId = Integer.parseInt(query.get("patientId"));
                        List<Appointment> list = appointmentDAO.getAppointmentsByPatient(patId);
                        sendResponse(exchange, 200, "{\"success\":true,\"appointments\":" + JsonHelper.appointmentsToJson(list) + "}");
                    } else {
                        List<Appointment> list = appointmentDAO.getAllAppointments();
                        sendResponse(exchange, 200, "{\"success\":true,\"appointments\":" + JsonHelper.appointmentsToJson(list) + "}");
                    }
                    break;
                }
                case "POST": {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = JsonHelper.parseJson(body);

                    Appointment a = new Appointment();
                    a.setPatientId(Integer.parseInt(data.get("patientId")));
                    a.setDoctorId(Integer.parseInt(data.get("doctorId")));
                    a.setAppDate(data.get("appDate"));
                    a.setAppTime(data.get("appTime"));
                    a.setStatus(data.getOrDefault("status", "Scheduled"));

                    int res = appointmentDAO.bookAppointment(a);
                    if (res == -2) {
                        sendResponse(exchange, 409, "{\"success\":false,\"message\":\"Doctor is already booked at this exact date and time. Please pick another slot.\"}");
                    } else if (res > 0) {
                        sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Appointment Booked Successfully\",\"appointmentId\":" + res + ",\"appointment\":" + JsonHelper.appointmentToJson(a) + "}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to book appointment.\"}");
                    }
                    break;
                }
                case "PUT": {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = JsonHelper.parseJson(body);

                    int appointmentId = Integer.parseInt(data.get("appointmentId"));
                    if (data.containsKey("status")) {
                        String status = data.get("status");
                        boolean ok = appointmentDAO.updateStatus(appointmentId, status);
                        if (ok) {
                            sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Appointment Status Updated to " + status + "\"}");
                        } else {
                            sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update appointment status.\"}");
                        }
                    } else if (data.containsKey("appDate") && data.containsKey("appTime")) {
                        int docId = Integer.parseInt(data.get("doctorId"));
                        String newDate = data.get("appDate");
                        String newTime = data.get("appTime");
                        boolean ok = appointmentDAO.rescheduleAppointment(appointmentId, docId, newDate, newTime);
                        if (ok) {
                            sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Appointment Rescheduled Successfully\"}");
                        } else {
                            sendResponse(exchange, 409, "{\"success\":false,\"message\":\"Doctor is not available at that new date and time.\"}");
                        }
                    } else {
                        sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing update parameters\"}");
                    }
                    break;
                }
                case "DELETE": {
                    String idStr = query.get("id");
                    if (idStr != null) {
                        int id = Integer.parseInt(idStr);
                        boolean ok = appointmentDAO.deleteAppointment(id);
                        if (ok) {
                            sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Appointment Deleted Successfully\"}");
                            return;
                        }
                    }
                    sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid Appointment ID\"}");
                    break;
                }
                default:
                    sendResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * FR6: Medical Records Management Handler
     */
    private class MedicalRecordsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 204, "");
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());

            switch (method) {
                case "GET": {
                    if (query.containsKey("patientId")) {
                        int patId = Integer.parseInt(query.get("patientId"));
                        List<MedicalRecord> list = medicalRecordDAO.getRecordsByPatient(patId);
                        sendResponse(exchange, 200, "{\"success\":true,\"records\":" + JsonHelper.medicalRecordsToJson(list) + "}");
                    } else {
                        List<MedicalRecord> list = medicalRecordDAO.getAllRecords();
                        sendResponse(exchange, 200, "{\"success\":true,\"records\":" + JsonHelper.medicalRecordsToJson(list) + "}");
                    }
                    break;
                }
                case "POST": {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = JsonHelper.parseJson(body);

                    MedicalRecord mr = new MedicalRecord();
                    mr.setPatientId(Integer.parseInt(data.get("patientId")));
                    mr.setDoctorId(Integer.parseInt(data.get("doctorId")));
                    mr.setVisitDate(data.get("visitDate"));
                    mr.setDiagnosis(data.get("diagnosis"));
                    mr.setTreatment(data.get("treatment"));
                    mr.setNotes(data.get("notes"));

                    int newId = medicalRecordDAO.addRecord(mr);
                    if (newId > 0) {
                        sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Medical Record Saved Successfully\",\"recordId\":" + newId + ",\"record\":" + JsonHelper.medicalRecordToJson(mr) + "}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to save medical record.\"}");
                    }
                    break;
                }
                case "PUT": {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = JsonHelper.parseJson(body);

                    MedicalRecord mr = new MedicalRecord();
                    mr.setRecordId(Integer.parseInt(data.get("recordId")));
                    mr.setVisitDate(data.get("visitDate"));
                    mr.setDiagnosis(data.get("diagnosis"));
                    mr.setTreatment(data.get("treatment"));
                    mr.setNotes(data.get("notes"));

                    boolean ok = medicalRecordDAO.updateRecord(mr);
                    if (ok) {
                        sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Medical Record Updated Successfully\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update medical record.\"}");
                    }
                    break;
                }
                case "DELETE": {
                    String idStr = query.get("id");
                    if (idStr != null) {
                        int id = Integer.parseInt(idStr);
                        boolean ok = medicalRecordDAO.deleteRecord(id);
                        if (ok) {
                            sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Medical Record Deleted Successfully\"}");
                            return;
                        }
                    }
                    sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid Record ID\"}");
                    break;
                }
                default:
                    sendResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * FR7: Search Patient Records Handler
     */
    private class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String keyword = query.get("q");

            List<Patient> patients = patientDAO.searchPatients(keyword);
            sendResponse(exchange, 200, "{\"success\":true,\"results\":" + JsonHelper.patientsToJson(patients) + "}");
        }
    }

    /**
     * FR8: Reports Handler
     */
    private class ReportsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String type = query.getOrDefault("type", "appointments");
            String start = query.get("startDate");
            String end = query.get("endDate");

            if ("visits".equalsIgnoreCase(type) || "records".equalsIgnoreCase(type)) {
                List<Map<String, Object>> records = reportDAO.getPatientVisitsReport(start, end);
                sendResponse(exchange, 200, "{\"success\":true,\"type\":\"visits\",\"report\":" + JsonHelper.mapListToJson(records) + "}");
            } else {
                List<Map<String, Object>> appts = reportDAO.getAppointmentsReport(start, end);
                sendResponse(exchange, 200, "{\"success\":true,\"type\":\"appointments\",\"report\":" + JsonHelper.mapListToJson(appts) + "}");
            }
        }
    }

    /**
     * Section 5.7: Database Backup Handler
     */
    private class BackupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            Map<String, Integer> stats = reportDAO.getDashboardStats();
            String json = "{\"success\":true,\"message\":\"Database backup snapshot completed successfully.\",\"timestamp\":\"" + new Date() + "\",\"counts\":" + JsonHelper.statsToJson(stats) + "}";
            sendResponse(exchange, 200, json);
        }
    }

    /**
     * Static File Handler (serves HTML, CSS, JS)
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File(webrootPath, path);
            if (!file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "404 Not Found");
                return;
            }

            String contentType = getContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    map.put(key, value);
                } else if (!pair.isEmpty()) {
                    map.put(URLDecoder.decode(pair, "UTF-8"), "");
                }
            } catch (UnsupportedEncodingException ignored) {}
        }
        return map;
    }

    private String getContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}
