package com.stms.service;

import com.stms.dao.AuditLogDAO;
import com.stms.dao.CertificateDAO;
import com.stms.dao.CourseDAO;
import com.stms.dao.DatabaseConnectionManager;
import com.stms.dao.EmployeeDAO;
import com.stms.dao.EnrollmentDAO;
import com.stms.dao.TrainerDAO;
import com.stms.dao.TrainingSessionDAO;
import com.stms.model.Certificate;
import com.stms.model.ComplianceCourse;
import com.stms.model.CourseLevel;
import com.stms.model.Department;
import com.stms.model.Employee;
import com.stms.model.Enrollment;
import com.stms.model.EnrollmentStatus;
import com.stms.model.SessionStatus;
import com.stms.model.SoftSkillsCourse;
import com.stms.model.TechnicalCourse;
import com.stms.model.Trainer;
import com.stms.model.TrainingCourse;
import com.stms.model.TrainingSession;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enterprise Service Layer Facade.
 * Orchestrates business logic, domain invariants, transactions, and SQLite DAO persistence.
 */
public class TrainingManagerService {
    private final DatabaseConnectionManager dbManager;
    private final EmployeeDAO employeeDAO;
    private final TrainerDAO trainerDAO;
    private final CourseDAO courseDAO;
    private final TrainingSessionDAO sessionDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final CertificateDAO certificateDAO;
    private final AuditLogDAO auditLogDAO;
    private final NotificationService notificationService;

    private final AtomicInteger idCounter = new AtomicInteger(1000);

    public TrainingManagerService() {
        this.dbManager = DatabaseConnectionManager.getInstance();
        this.employeeDAO = new EmployeeDAO();
        this.trainerDAO = new TrainerDAO();
        this.courseDAO = new CourseDAO();
        this.sessionDAO = new TrainingSessionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.certificateDAO = new CertificateDAO();
        this.auditLogDAO = new AuditLogDAO();
        this.notificationService = new NotificationService();

        // 1. Initialize Relational Schema in SQLite
        try {
            dbManager.initializeSchema();
            System.out.println("SQLite Database schema verified and initialized.");
        } catch (SQLException e) {
            System.err.println("Fatal error initializing database schema: " + e.getMessage());
        }

        // 2. Auto-seed demonstration data if database is empty
        if (dbManager.isDatabaseEmpty()) {
            seedInitialDemoData();
        }

        logEvent("Staff Training Management System initialized with SQLite JDBC.");
    }

    public void logEvent(String message) {
        auditLogDAO.logEvent(message);
        notificationService.logEvent(message);
    }

    public List<String> getRecentAuditLogs(int limit) {
        return auditLogDAO.getRecentLogs(limit);
    }

    public List<String> getAuditEvents() {
        return auditLogDAO.getRecentLogs(50);
    }

    // =========================================================================
    // STAFF (EMPLOYEE) OPERATIONS
    // =========================================================================

    public String getNextEmployeeId() {
        return "EMP-" + (employeeDAO.count() + 101);
    }

    public void registerEmployee(Employee employee) {
        if (employee.getId() == null || employee.getId().isEmpty()) {
            employee.setId(getNextEmployeeId());
        }
        try {
            employeeDAO.insert(employee);
            logEvent("Registered new employee: " + employee.getFullName() + " [" + employee.getId() + "]");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register employee: " + e.getMessage(), e);
        }
    }

    public void updateEmployee(Employee employee) {
        try {
            employeeDAO.update(employee);
            logEvent("Updated employee: " + employee.getFullName() + " [" + employee.getId() + "]");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update employee: " + e.getMessage(), e);
        }
    }

    public void setEmployeeActive(String id, boolean active) {
        try {
            employeeDAO.setActiveStatus(id, active);
            logEvent("Updated employee status [" + id + "] to " + (active ? "Active" : "Inactive"));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update employee status: " + e.getMessage(), e);
        }
    }

    public boolean deleteEmployee(String id) {
        try {
            boolean deleted = employeeDAO.delete(id);
            if (deleted) {
                logEvent("Deleted employee ID: " + id);
            }
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.findAll();
    }

    public Optional<Employee> findEmployeeById(String id) {
        return employeeDAO.findById(id);
    }

    public List<Employee> searchEmployees(String query) {
        return employeeDAO.search(query, "ALL", null);
    }

    public List<Employee> searchEmployees(String query, String dept, Boolean activeOnly) {
        return employeeDAO.search(query, dept, activeOnly);
    }

    // =========================================================================
    // TRAINER OPERATIONS
    // =========================================================================

    public String getNextTrainerId() {
        return "TRN-" + (trainerDAO.count() + 201);
    }

    public void registerTrainer(Trainer trainer) {
        if (trainer.getId() == null || trainer.getId().isEmpty()) {
            trainer.setId(getNextTrainerId());
        }
        try {
            trainerDAO.insert(trainer);
            logEvent("Registered trainer: " + trainer.getFullName() + " [" + trainer.getId() + "]");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register trainer: " + e.getMessage(), e);
        }
    }

    public void updateTrainer(Trainer trainer) {
        try {
            trainerDAO.update(trainer);
            logEvent("Updated trainer: " + trainer.getFullName() + " [" + trainer.getId() + "]");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update trainer: " + e.getMessage(), e);
        }
    }

    public void setTrainerActive(String id, boolean active) {
        try {
            trainerDAO.setActiveStatus(id, active);
            logEvent("Updated trainer status [" + id + "] to " + (active ? "Active" : "Inactive"));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update trainer status: " + e.getMessage(), e);
        }
    }

    public boolean deleteTrainer(String id) {
        try {
            boolean deleted = trainerDAO.delete(id);
            if (deleted) {
                logEvent("Deleted trainer ID: " + id);
            }
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<Trainer> getAllTrainers() {
        return trainerDAO.findAll();
    }

    public Optional<Trainer> findTrainerById(String id) {
        return trainerDAO.findById(id);
    }

    public List<Trainer> searchTrainers(String query, Boolean activeOnly) {
        return trainerDAO.search(query, activeOnly);
    }

    // =========================================================================
    // COURSE CATALOG OPERATIONS
    // =========================================================================

    public String getNextCourseId() {
        return "CRS-" + (courseDAO.count() + 301);
    }

    public void registerCourse(TrainingCourse course) {
        if (course.getId() == null || course.getId().isEmpty()) {
            course.setId(getNextCourseId());
        }
        try {
            courseDAO.insert(course);
            logEvent("Added new course to catalog: " + course.getTitle() + " (" + course.getCategoryName() + ")");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register course: " + e.getMessage(), e);
        }
    }

    public void addCourse(TrainingCourse course) {
        registerCourse(course);
    }

    public void updateCourse(TrainingCourse course) {
        try {
            courseDAO.update(course);
            logEvent("Updated course: " + course.getTitle() + " [" + course.getId() + "]");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update course: " + e.getMessage(), e);
        }
    }

    public void setCourseArchived(String id, boolean archived) {
        try {
            courseDAO.setArchivedStatus(id, archived);
            logEvent("Set course archived status [" + id + "] to " + archived);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update course archive status: " + e.getMessage(), e);
        }
    }

    public boolean deleteCourse(String courseId) {
        try {
            boolean deleted = courseDAO.delete(courseId);
            if (deleted) {
                logEvent("Removed course: " + courseId);
            }
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<TrainingCourse> getAllCourses() {
        return courseDAO.findAll(false);
    }

    public List<TrainingCourse> getAllCourses(boolean includeArchived) {
        return courseDAO.findAll(includeArchived);
    }

    public Optional<TrainingCourse> findCourseById(String id) {
        return courseDAO.findById(id);
    }

    public List<TrainingCourse> searchCourses(String query) {
        return courseDAO.search(query, "ALL", false);
    }

    public List<TrainingCourse> searchCourses(String query, String category, boolean includeArchived) {
        return courseDAO.search(query, category, includeArchived);
    }

    // =========================================================================
    // SESSION OPERATIONS
    // =========================================================================

    public String getNextSessionId() {
        return "SES-" + (sessionDAO.count() + 401);
    }

    public TrainingSession scheduleSession(String courseId, String trainerId,
                                           LocalDate sessionDate, String timeSlot,
                                           String locationOrUrl, int maxSeats) {
        TrainingCourse course = courseDAO.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));
        Trainer trainer = trainerDAO.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with ID: " + trainerId));

        if (!trainer.isActive()) {
            throw new IllegalStateException("Cannot assign inactive trainer " + trainer.getFullName() + " to a session.");
        }
        if (sessionDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Session date cannot be in the past.");
        }
        if (maxSeats <= 0 || maxSeats > course.getMaxCapacity()) {
            throw new IllegalArgumentException("Session capacity must be between 1 and course limit (" + course.getMaxCapacity() + ").");
        }

        String sessionId = getNextSessionId();
        TrainingSession session = new TrainingSession(sessionId, course.getId(), course.getTitle(),
                trainer.getId(), trainer.getFullName(), sessionDate, timeSlot, locationOrUrl,
                maxSeats, SessionStatus.SCHEDULED);

        try {
            sessionDAO.insert(session);
            logEvent("Scheduled session: " + session.getId() + " for course: " + course.getTitle());
            return session;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void updateSession(TrainingSession session) {
        try {
            sessionDAO.update(session);
            logEvent("Updated session: " + session.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void updateSessionStatus(String sessionId, SessionStatus status) {
        try {
            sessionDAO.updateStatus(sessionId, status);
            logEvent("Updated session " + sessionId + " status to " + status);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean deleteSession(String id) {
        try {
            boolean deleted = sessionDAO.delete(id);
            if (deleted) {
                logEvent("Deleted session ID: " + id);
            }
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<TrainingSession> getAllSessions() {
        return sessionDAO.findAll();
    }

    public Optional<TrainingSession> findSessionById(String id) {
        return sessionDAO.findById(id);
    }

    public int getAvailableSeats(String sessionId) {
        TrainingSession session = sessionDAO.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        int booked = enrollmentDAO.countEnrolledInSession(sessionId);
        return Math.max(0, session.getMaxSeats() - booked);
    }

    // =========================================================================
    // ENROLLMENT & GRADING WORKFLOW (TRANSACTIONAL)
    // =========================================================================

    public String getNextEnrollmentId() {
        return "ENR-" + (enrollmentDAO.count() + 501);
    }

    public Enrollment enrollStaff(String employeeId, String sessionId) {
        Employee employee = employeeDAO.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee ID not found: " + employeeId));
        TrainingSession session = sessionDAO.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session ID not found: " + sessionId));

        if (!employee.isActive()) {
            throw new IllegalStateException("Cannot enroll inactive employee " + employee.getFullName());
        }
        if (session.getStatus() == SessionStatus.COMPLETED || session.getStatus() == SessionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot enroll in a session that is " + session.getStatus());
        }
        if (enrollmentDAO.isAlreadyEnrolled(sessionId, employeeId)) {
            throw new IllegalStateException("Employee " + employee.getFullName() + " is already enrolled in this session.");
        }

        int currentEnrolled = enrollmentDAO.countEnrolledInSession(sessionId);
        if (currentEnrolled >= session.getMaxSeats()) {
            throw new IllegalStateException("Session is fully booked! Capacity (" + session.getMaxSeats() + ") reached.");
        }

        String enrId = getNextEnrollmentId();
        Enrollment enrollment = new Enrollment(enrId, session.getId(), employee.getId(), employee.getFullName(),
                session.getCourseId(), session.getCourseTitle(), LocalDate.now());

        try {
            enrollmentDAO.insert(enrollment);
            logEvent("Enrolled staff " + employee.getFullName() + " into session " + session.getId());
            return enrollment;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Records attendance and grade with atomic transaction handling and automatic certification.
     */
    public void recordAttendanceAndScore(String enrollmentId, boolean attended, Double score, String notes) {
        if (score != null && (score < 0.0 || score > 100.0)) {
            throw new IllegalArgumentException("Score must be between 0.0 and 100.0%");
        }

        Enrollment enrollment = enrollmentDAO.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));
        TrainingCourse course = courseDAO.findById(enrollment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + enrollment.getCourseId()));

        enrollment.setAttended(attended);
        enrollment.setScore(score);
        if (notes != null) {
            enrollment.setNotes(notes);
        }

        // Polymorphically evaluate completion threshold
        boolean passed = enrollment.evaluateCompletion(course);

        // Execute in an atomic database transaction
        try {
            dbManager.executeInTransaction(conn -> {
                if (passed && enrollment.getCertificateId() == null) {
                    String certId = "CERT-" + (certificateDAO.count() + 901);
                    String verificationCode = "VER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                    Certificate cert = new Certificate(certId, enrollment.getId(),
                            enrollment.getEmployeeId(), enrollment.getEmployeeName(),
                            course.getId(), course.getTitle(), course.getCategoryName(),
                            LocalDate.now(), course.getCertificateValidityMonths());
                    cert.setVerificationCode(verificationCode);

                    certificateDAO.insertInConnection(cert, conn);
                    enrollment.setCertificateId(certId);
                }

                enrollmentDAO.updateInConnection(enrollment, conn);
                return null;
            });

            if (passed) {
                logEvent("Issued Certificate to " + enrollment.getEmployeeName() + " for course: " + course.getTitle());
            } else {
                logEvent("Graded enrollment " + enrollment.getId() + " - Score: " + score + "% (Status: " + enrollment.getStatus() + ")");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error recording grade: " + e.getMessage(), e);
        }
    }

    public void recordGradeAndAttendance(String enrId, boolean attended, double score, String notes) {
        recordAttendanceAndScore(enrId, attended, score, notes);
    }

    public void cancelEnrollment(String enrollmentId, String reason) {
        Enrollment en = enrollmentDAO.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));
        en.setStatus(EnrollmentStatus.CANCELLED);
        en.setNotes((en.getNotes() != null ? en.getNotes() + " | " : "") + "Cancelled: " + reason);
        try {
            enrollmentDAO.update(en);
            logEvent("Cancelled enrollment: " + enrollmentId + " (" + reason + ")");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to cancel enrollment: " + e.getMessage(), e);
        }
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentDAO.findAll();
    }

    public List<Enrollment> getEnrollmentsBySession(String sessionId) {
        return enrollmentDAO.findBySessionId(sessionId);
    }

    public List<Enrollment> getEnrollmentsByEmployee(String employeeId) {
        return enrollmentDAO.findByEmployeeId(employeeId);
    }

    // =========================================================================
    // CERTIFICATES & REPORTING
    // =========================================================================

    public List<Certificate> getAllCertificates() {
        return certificateDAO.findAll();
    }

    public List<Certificate> getCertificatesByEmployee(String employeeId) {
        return certificateDAO.findByEmployeeId(employeeId);
    }

    public Optional<Certificate> verifyCertificate(String verificationCode) {
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            return Optional.empty();
        }
        return certificateDAO.findByVerificationCode(verificationCode.trim());
    }

    public int getTotalStaffCount() {
        return employeeDAO.count();
    }

    public int getTotalCoursesCount() {
        return courseDAO.count();
    }

    public int getTotalSessionsCount() {
        return sessionDAO.count();
    }

    public int getTotalEnrollmentsCount() {
        return enrollmentDAO.count();
    }

    public int getTotalCertificatesCount() {
        return certificateDAO.count();
    }

    public double calculateOverallPassRate() {
        return enrollmentDAO.calculateOverallPassRate();
    }

    public double getOverallPassRate() {
        return calculateOverallPassRate();
    }

    public String generateStaffTranscript(String employeeId) {
        Employee employee = employeeDAO.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found with ID: " + employeeId));

        List<Enrollment> enrollments = enrollmentDAO.findByEmployeeId(employeeId);
        List<Certificate> certs = certificateDAO.findByEmployeeId(employeeId);

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                      OFFICIAL STAFF TRAINING TRANSCRIPT                                \n");
        sb.append("                         ENTERPRISE STMS REPORTING                                      \n");
        sb.append("========================================================================================\n\n");
        sb.append(String.format("Staff ID        : %s\n", employee.getId()));
        sb.append(String.format("Full Name       : %s\n", employee.getFullName()));
        sb.append(String.format("Department      : %s\n", employee.getDepartment().getDisplayName()));
        sb.append(String.format("Job Title       : %s\n", employee.getJobTitle()));
        sb.append(String.format("Email           : %s\n", employee.getEmail()));
        sb.append(String.format("Date Generated  : %s\n\n", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))));

        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append("COURSE ENROLLMENT & ASSESSMENT HISTORY\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-10s | %-30s | %-10s | %-8s | %-16s\n", "Enr ID", "Course Title", "Attended", "Score", "Status"));
        sb.append("----------------------------------------------------------------------------------------\n");

        if (enrollments.isEmpty()) {
            sb.append("  (No training enrollment records found for this staff member)\n");
        } else {
            for (Enrollment en : enrollments) {
                sb.append(String.format("%-10s | %-30s | %-10s | %-8s | %-16s\n",
                        en.getId(),
                        (en.getCourseTitle().length() > 28 ? en.getCourseTitle().substring(0, 27) + ".." : en.getCourseTitle()),
                        en.isAttended() ? "YES" : "NO",
                        (en.getScore() != null ? String.format("%.1f%%", en.getScore()) : "N/A"),
                        en.getStatus().getLabel()));
            }
        }

        sb.append("\n----------------------------------------------------------------------------------------\n");
        sb.append("EARNED CERTIFICATIONS\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-12s | %-26s | %-12s | %-16s | %-16s\n", "Cert ID", "Course", "Issued On", "Verification", "Validity"));
        sb.append("----------------------------------------------------------------------------------------\n");

        if (certs.isEmpty()) {
            sb.append("  (No certificates issued yet)\n");
        } else {
            for (Certificate c : certs) {
                sb.append(String.format("%-12s | %-26s | %-12s | %-16s | %-16s\n",
                        c.getId(),
                        (c.getCourseTitle().length() > 24 ? c.getCourseTitle().substring(0, 23) + ".." : c.getCourseTitle()),
                        c.getIssueDate(),
                        c.getVerificationCode(),
                        c.getValidityStatus()));
            }
        }

        sb.append("\n========================================================================================\n");
        sb.append("             Verified and Certified by Corporate Talent Development                    \n");
        sb.append("========================================================================================\n");

        return sb.toString();
    }

    public String generateDepartmentSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                    DEPARTMENT TRAINING PARTICIPATION SUMMARY                           \n");
        sb.append("========================================================================================\n\n");
        sb.append(String.format("%-25s | %-12s | %-14s | %-14s\n", "Department", "Staff Count", "Enrollments", "Certificates"));
        sb.append("----------------------------------------------------------------------------------------\n");

        for (Department dept : Department.values()) {
            List<Employee> emps = employeeDAO.search(null, dept.name(), null);
            int enrCount = 0;
            int certCount = 0;
            for (Employee e : emps) {
                enrCount += enrollmentDAO.findByEmployeeId(e.getId()).size();
                certCount += certificateDAO.findByEmployeeId(e.getId()).size();
            }
            sb.append(String.format("%-25s | %-12d | %-14d | %-14d\n",
                    dept.getDisplayName(), emps.size(), enrCount, certCount));
        }

        sb.append("\n----------------------------------------------------------------------------------------\n");
        sb.append(String.format("Total Staff: %d | Total Sessions: %d | Overall Pass Rate: %.1f%%\n",
                getTotalStaffCount(), getTotalSessionsCount(), calculateOverallPassRate()));
        sb.append("========================================================================================\n");
        return sb.toString();
    }

    public void exportTranscriptToCsv(String employeeId, File file) throws IOException {
        Employee employee = employeeDAO.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found with ID: " + employeeId));
        List<Enrollment> enrollments = enrollmentDAO.findByEmployeeId(employeeId);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("EnrollmentID,EmployeeID,EmployeeName,Department,CourseTitle,EnrollmentDate,Attended,Score,Status,CertificateID\n");
            for (Enrollment en : enrollments) {
                writer.write(String.format("%s,%s,\"%s\",%s,\"%s\",%s,%s,%s,%s,%s\n",
                        en.getId(), employee.getId(), employee.getFullName(),
                        employee.getDepartment().name(), en.getCourseTitle(),
                        en.getEnrollmentDate(), en.isAttended(),
                        (en.getScore() != null ? en.getScore().toString() : ""),
                        en.getStatus().name(),
                        (en.getCertificateId() != null ? en.getCertificateId() : "")));
            }
        }
    }

    public void exportDepartmentSummaryToCsv(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Department,StaffCount,TotalEnrollments,TotalCertificates\n");
            for (Department dept : Department.values()) {
                List<Employee> emps = employeeDAO.search(null, dept.name(), null);
                int enrCount = 0;
                int certCount = 0;
                for (Employee e : emps) {
                    enrCount += enrollmentDAO.findByEmployeeId(e.getId()).size();
                    certCount += certificateDAO.findByEmployeeId(e.getId()).size();
                }
                writer.write(String.format("\"%s\",%d,%d,%d\n", dept.getDisplayName(), emps.size(), enrCount, certCount));
            }
        }
    }

    // =========================================================================
    // DATABASE DIAGNOSTICS & BACKUP
    // =========================================================================

    public boolean testDatabaseConnection() {
        return dbManager.testConnection();
    }

    public long getDatabaseLatencyMs() {
        return dbManager.getConnectionLatencyMs();
    }

    public void backupDatabase(File destinationFile) throws IOException, SQLException {
        dbManager.backupDatabase(destinationFile);
        logEvent("Database successfully backed up to: " + destinationFile.getName());
    }

    public void restoreDatabase(File backupFile) throws IOException, SQLException {
        dbManager.restoreDatabase(backupFile);
        logEvent("Database successfully restored from: " + backupFile.getName());
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void syncToStorage() {
        // SQLite is the active source of truth.
        // We log synchronization confirmation.
        logEvent("Data store synced with SQLite relational tables.");
    }

    // =========================================================================
    // SEED INITIAL REALISTIC DEMONSTRATION DATA
    // =========================================================================

    private void seedInitialDemoData() {
        try {
            System.out.println("Seeding initial demonstration data into SQLite database...");

            // Employees
            Employee e1 = new Employee("EMP-101", "Alex", "Morgan", "alex.morgan@company.com", "+1-555-0101", Department.ENGINEERING, "Senior Software Engineer", LocalDate.now().minusMonths(24));
            Employee e2 = new Employee("EMP-102", "Samantha", "Ray", "samantha.ray@company.com", "+1-555-0102", Department.HUMAN_RESOURCES, "Talent Development Lead", LocalDate.now().minusMonths(18));
            Employee e3 = new Employee("EMP-103", "David", "Kim", "david.kim@company.com", "+1-555-0103", Department.FINANCE, "Financial Analyst", LocalDate.now().minusMonths(12));
            Employee e4 = new Employee("EMP-104", "Elena", "Rostova", "elena.rostova@company.com", "+1-555-0104", Department.ENGINEERING, "DevOps Engineer", LocalDate.now().minusMonths(8));
            Employee e5 = new Employee("EMP-105", "Marcus", "Johnson", "marcus.j@company.com", "+1-555-0105", Department.MARKETING, "Product Marketing Specialist", LocalDate.now().minusMonths(30));
            Employee e6 = new Employee("EMP-106", "Chloe", "Bennett", "chloe.b@company.com", "+1-555-0106", Department.LEGAL_COMPLIANCE, "Compliance Officer", LocalDate.now().minusMonths(6));

            employeeDAO.insert(e1);
            employeeDAO.insert(e2);
            employeeDAO.insert(e3);
            employeeDAO.insert(e4);
            employeeDAO.insert(e5);
            employeeDAO.insert(e6);

            // Trainers
            Trainer t1 = new Trainer("TRN-201", "Dr. Robert", "Chen", "robert.chen@training.org", "+1-555-0201", "Advanced Java & Cloud Architectures", 12, 120.0, true);
            Trainer t2 = new Trainer("TRN-202", "Sarah", "Jenkins", "sarah.jenkins@company.com", "+1-555-0202", "Corporate Leadership & Agile Coaching", 8, 85.0, false);
            Trainer t3 = new Trainer("TRN-203", "Michael", "Vance", "michael.vance@legalgroup.com", "+1-555-0203", "Cybersecurity & Data Privacy (GDPR/ISO)", 15, 140.0, true);

            trainerDAO.insert(t1);
            trainerDAO.insert(t2);
            trainerDAO.insert(t3);

            // Courses (Polymorphic hierarchy)
            TrainingCourse c1 = new TechnicalCourse("CRS-301", "Design Patterns & Object-Oriented Java", "Comprehensive deep-dive into clean architecture and SOLID design patterns.", 24, CourseLevel.ADVANCED, 25, 450.0, "Java 21 / Spring / Clean Architecture", true);
            TrainingCourse c2 = new SoftSkillsCourse("CRS-302", "High-Impact Agile Leadership", "Mastering team psychological safety, active coaching, and rapid feedback loops.", 16, CourseLevel.INTERMEDIATE, 20, 300.0, "Empathetic Leadership", true);
            TrainingCourse c3 = new ComplianceCourse("CRS-303", "Enterprise Information Security & GDPR", "Annual mandatory cyber defense, phishing mitigation, and data governance compliance.", 8, CourseLevel.BEGINNER, 50, 150.0, "ISO 27001 / GDPR Article 32", true);
            TrainingCourse c4 = new TechnicalCourse("CRS-304", "Docker, Kubernetes & Cloud Native CI/CD", "Production-grade microservices deployment and container orchestration.", 32, CourseLevel.ADVANCED, 20, 600.0, "Kubernetes / Helm / AWS", true);

            courseDAO.insert(c1);
            courseDAO.insert(c2);
            courseDAO.insert(c3);
            courseDAO.insert(c4);

            // Sessions
            TrainingSession s1 = new TrainingSession("SES-401", c1.getId(), c1.getTitle(), t1.getId(), t1.getFullName(), LocalDate.now().minusDays(10), "09:00 - 17:00", "Tech Lab Room 302", 20, SessionStatus.COMPLETED);
            TrainingSession s2 = new TrainingSession("SES-402", c3.getId(), c3.getTitle(), t3.getId(), t3.getFullName(), LocalDate.now().minusDays(5), "10:00 - 14:00", "Zoom Virtual Auditorium", 50, SessionStatus.COMPLETED);
            TrainingSession s3 = new TrainingSession("SES-403", c2.getId(), c2.getTitle(), t2.getId(), t2.getFullName(), LocalDate.now().plusDays(7), "09:30 - 16:30", "Executive Suite Room 101", 15, SessionStatus.SCHEDULED);

            sessionDAO.insert(s1);
            sessionDAO.insert(s2);
            sessionDAO.insert(s3);

            // Enrollments & Certificates
            Enrollment en1 = new Enrollment("ENR-501", s1.getId(), e1.getId(), e1.getFullName(), c1.getId(), c1.getTitle(), LocalDate.now().minusDays(20));
            en1.setAttended(true);
            en1.setScore(92.0);
            en1.setStatus(EnrollmentStatus.PASSED);
            en1.setCertificateId("CERT-901");
            enrollmentDAO.insert(en1);

            Certificate cert1 = new Certificate("CERT-901", en1.getId(), e1.getId(), e1.getFullName(), c1.getId(), c1.getTitle(), c1.getCategoryName(), LocalDate.now().minusDays(9), c1.getCertificateValidityMonths());
            cert1.setVerificationCode("VER-JAVA-901");
            certificateDAO.insert(cert1);

            Enrollment en2 = new Enrollment("ENR-502", s2.getId(), e2.getId(), e2.getFullName(), c3.getId(), c3.getTitle(), LocalDate.now().minusDays(14));
            en2.setAttended(true);
            en2.setScore(95.0);
            en2.setStatus(EnrollmentStatus.PASSED);
            en2.setCertificateId("CERT-902");
            enrollmentDAO.insert(en2);

            Certificate cert2 = new Certificate("CERT-902", en2.getId(), e2.getId(), e2.getFullName(), c3.getId(), c3.getTitle(), c3.getCategoryName(), LocalDate.now().minusDays(4), c3.getCertificateValidityMonths());
            cert2.setVerificationCode("VER-GDPR-902");
            certificateDAO.insert(cert2);

            Enrollment en3 = new Enrollment("ENR-503", s2.getId(), e3.getId(), e3.getFullName(), c3.getId(), c3.getTitle(), LocalDate.now().minusDays(14));
            en3.setAttended(true);
            en3.setScore(72.0);
            en3.setStatus(EnrollmentStatus.FAILED);
            en3.setNotes("Scored 72%. Below compliance passing threshold of 85%. Retake required.");
            enrollmentDAO.insert(en3);

            Enrollment en4 = new Enrollment("ENR-504", s3.getId(), e5.getId(), e5.getFullName(), c2.getId(), c2.getTitle(), LocalDate.now().minusDays(2));
            en4.setStatus(EnrollmentStatus.REGISTERED);
            enrollmentDAO.insert(en4);

            logEvent("Initial demonstration data seeded successfully into SQLite.");
        } catch (SQLException e) {
            System.err.println("Error seeding demonstration data: " + e.getMessage());
        }
    }
}
