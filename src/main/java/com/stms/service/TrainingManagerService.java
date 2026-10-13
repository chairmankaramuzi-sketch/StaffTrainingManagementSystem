package com.stms.service;

import com.stms.model.Certificate;
import com.stms.model.Department;
import com.stms.model.Employee;
import com.stms.model.Enrollment;
import com.stms.model.EnrollmentStatus;
import com.stms.model.SessionStatus;
import com.stms.model.Trainer;
import com.stms.model.TrainingCourse;
import com.stms.model.TrainingSession;
import com.stms.repository.CertificateRepository;
import com.stms.repository.CourseRepository;
import com.stms.repository.EmployeeRepository;
import com.stms.repository.EnrollmentRepository;
import com.stms.repository.SessionRepository;
import com.stms.repository.TrainerRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main Business Logic Facade.
 * Orchestrates domain operations, enforces business invariants, and coordinates persistence.
 */
public class TrainingManagerService {
    private final EmployeeRepository employeeRepository;
    private final TrainerRepository trainerRepository;
    private final CourseRepository courseRepository;
    private final SessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CertificateRepository certificateRepository;

    private final DataPersistenceService persistenceService;
    private final NotificationService notificationService;

    private final AtomicInteger idCounter = new AtomicInteger(1000);

    public TrainingManagerService() {
        this.employeeRepository = new EmployeeRepository();
        this.trainerRepository = new TrainerRepository();
        this.courseRepository = new CourseRepository();
        this.sessionRepository = new SessionRepository();
        this.enrollmentRepository = new EnrollmentRepository();
        this.certificateRepository = new CertificateRepository();

        this.persistenceService = new DataPersistenceService();
        this.notificationService = new NotificationService();

        // Load persisted data or seed sample data
        this.persistenceService.loadOrSeedAll(
                employeeRepository, trainerRepository, courseRepository,
                sessionRepository, enrollmentRepository, certificateRepository
        );

        this.notificationService.logEvent("Training Management System initialized successfully.");
    }

    public synchronized void syncToStorage() {
        persistenceService.saveAll(
                employeeRepository, trainerRepository, courseRepository,
                sessionRepository, enrollmentRepository, certificateRepository
        );
    }

    // ==========================================
    // STAFF (EMPLOYEE) OPERATIONS
    // ==========================================

    public String getNextEmployeeId() {
        return "EMP-" + (employeeRepository.count() + 101);
    }

    public String getNextCourseId() {
        return "CRS-" + (courseRepository.count() + 301);
    }

    public void registerEmployee(Employee employee) {
        if (employee.getId() == null || employee.getId().isEmpty()) {
            employee.setId(getNextEmployeeId());
        }
        employeeRepository.save(employee);
        syncToStorage();
        notificationService.logEvent("Registered new employee: " + employee.getFullName() + " [" + employee.getId() + "]");
    }

    public void updateEmployee(Employee employee) {
        employeeRepository.save(employee);
        syncToStorage();
        notificationService.logEvent("Updated employee: " + employee.getFullName());
    }

    public boolean deleteEmployee(String id) {
        boolean removed = employeeRepository.deleteById(id);
        if (removed) {
            syncToStorage();
            notificationService.logEvent("Deleted employee ID: " + id);
        }
        return removed;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> findEmployeeById(String id) {
        return employeeRepository.findById(id);
    }

    public List<Employee> searchEmployees(String query) {
        return employeeRepository.search(query);
    }

    // ==========================================
    // TRAINER OPERATIONS
    // ==========================================

    public void registerTrainer(Trainer trainer) {
        if (trainer.getId() == null || trainer.getId().isEmpty()) {
            trainer.setId("TRN-" + idCounter.incrementAndGet());
        }
        trainerRepository.save(trainer);
        syncToStorage();
        notificationService.logEvent("Registered trainer: " + trainer.getFullName());
    }

    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    public Optional<Trainer> findTrainerById(String id) {
        return trainerRepository.findById(id);
    }

    // ==========================================
    // COURSE CATALOG OPERATIONS
    // ==========================================

    public void registerCourse(TrainingCourse course) {
        if (course.getId() == null || course.getId().isEmpty()) {
            course.setId("CRS-" + idCounter.incrementAndGet());
        }
        courseRepository.save(course);
        syncToStorage();
        notificationService.logEvent("Added new course to catalog: " + course.getTitle() + " (" + course.getCategoryName() + ")");
    }

    public boolean deleteCourse(String courseId) {
        boolean removed = courseRepository.deleteById(courseId);
        if (removed) {
            syncToStorage();
            notificationService.logEvent("Removed course: " + courseId);
        }
        return removed;
    }

    public List<TrainingCourse> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<TrainingCourse> findCourseById(String id) {
        return courseRepository.findById(id);
    }

    public List<TrainingCourse> searchCourses(String query) {
        return courseRepository.search(query);
    }

    // ==========================================
    // SESSION OPERATIONS
    // ==========================================

    public TrainingSession scheduleSession(String courseId, String trainerId,
                                           LocalDate sessionDate, String timeSlot,
                                           String locationOrUrl, int maxSeats) {
        TrainingCourse course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with ID: " + trainerId));

        String sessionId = "SES-" + idCounter.incrementAndGet();
        TrainingSession session = new TrainingSession(sessionId, course.getId(), course.getTitle(),
                trainer.getId(), trainer.getFullName(), sessionDate, timeSlot, locationOrUrl,
                maxSeats, SessionStatus.SCHEDULED);

        sessionRepository.save(session);
        syncToStorage();
        notificationService.logEvent("Scheduled session: " + session.getId() + " for course: " + course.getTitle());
        return session;
    }

    public void updateSession(TrainingSession session) {
        sessionRepository.save(session);
        syncToStorage();
        notificationService.logEvent("Updated session: " + session.getId());
    }

    public List<TrainingSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    public Optional<TrainingSession> findSessionById(String id) {
        return sessionRepository.findById(id);
    }

    // ==========================================
    // ENROLLMENT & GRADING WORKFLOW
    // ==========================================

    public Enrollment enrollStaff(String employeeId, String sessionId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee ID not found: " + employeeId));
        TrainingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session ID not found: " + sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED || session.getStatus() == SessionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot enroll in a session that is " + session.getStatus());
        }

        if (enrollmentRepository.isAlreadyEnrolled(employeeId, sessionId)) {
            throw new IllegalStateException("Employee is already enrolled in this session.");
        }

        int currentEnrolled = enrollmentRepository.countEnrolledInSession(sessionId);
        if (currentEnrolled >= session.getMaxSeats()) {
            throw new IllegalStateException("Session is fully booked! Capacity (" + session.getMaxSeats() + ") reached.");
        }

        String enrId = "ENR-" + idCounter.incrementAndGet();
        Enrollment enrollment = new Enrollment(enrId, session.getId(), employee.getId(), employee.getFullName(),
                session.getCourseId(), session.getCourseTitle(), LocalDate.now());

        enrollmentRepository.save(enrollment);
        syncToStorage();
        notificationService.logEvent("Enrolled staff " + employee.getFullName() + " into session " + session.getId());
        return enrollment;
    }

    public void recordAttendanceAndScore(String enrollmentId, boolean attended, Double score, String notes) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));
        TrainingCourse course = courseRepository.findById(enrollment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + enrollment.getCourseId()));

        enrollment.setAttended(attended);
        enrollment.setScore(score);
        if (notes != null) {
            enrollment.setNotes(notes);
        }

        // Polymorphically evaluate completion!
        boolean passed = enrollment.evaluateCompletion(course);

        // If passed and certificate not yet issued, issue official Certificate
        if (passed && enrollment.getCertificateId() == null) {
            String certId = "CERT-" + idCounter.incrementAndGet();
            Certificate cert = new Certificate(certId, enrollment.getId(),
                    enrollment.getEmployeeId(), enrollment.getEmployeeName(),
                    course.getId(), course.getTitle(), course.getCategoryName(),
                    LocalDate.now(), course.getCertificateValidityMonths());

            certificateRepository.save(cert);
            enrollment.setCertificateId(certId);
            notificationService.logEvent("Issued Certificate " + certId + " to " + enrollment.getEmployeeName() + " for " + course.getTitle());
        }

        enrollmentRepository.save(enrollment);
        syncToStorage();
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public List<Enrollment> getEnrollmentsBySession(String sessionId) {
        return enrollmentRepository.findBySessionId(sessionId);
    }

    public List<Enrollment> getEnrollmentsByEmployee(String employeeId) {
        return enrollmentRepository.findByEmployeeId(employeeId);
    }

    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }

    public List<Certificate> getCertificatesByEmployee(String employeeId) {
        return certificateRepository.findByEmployeeId(employeeId);
    }

    // ==========================================
    // METRICS & REPORTING
    // ==========================================

    public int getTotalStaffCount() {
        return employeeRepository.count();
    }

    public int getTotalCoursesCount() {
        return courseRepository.count();
    }

    public int getTotalSessionsCount() {
        return sessionRepository.count();
    }

    public int getTotalEnrollmentsCount() {
        return enrollmentRepository.count();
    }

    public int getTotalCertificatesCount() {
        return certificateRepository.count();
    }

    public double calculateOverallPassRate() {
        List<Enrollment> all = enrollmentRepository.findAll();
        long evaluated = all.stream().filter(e -> e.getStatus() == EnrollmentStatus.PASSED || e.getStatus() == EnrollmentStatus.FAILED).count();
        if (evaluated == 0) return 0.0;
        long passed = all.stream().filter(e -> e.getStatus() == EnrollmentStatus.PASSED).count();
        return ((double) passed / evaluated) * 100.0;
    }

    /**
     * Generates a printable formal Training Transcript for an Employee.
     */
    public String generateStaffTranscript(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found with ID: " + employeeId));

        List<Enrollment> enrollments = enrollmentRepository.findByEmployeeId(employeeId);
        List<Certificate> certs = certificateRepository.findByEmployeeId(employeeId);

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
        sb.append(String.format("%-12s | %-30s | %-12s | %-20s\n", "Cert ID", "Course", "Issued On", "Validity Status"));
        sb.append("----------------------------------------------------------------------------------------\n");

        if (certs.isEmpty()) {
            sb.append("  (No certificates issued yet)\n");
        } else {
            for (Certificate c : certs) {
                sb.append(String.format("%-12s | %-30s | %-12s | %-20s\n",
                        c.getId(),
                        (c.getCourseTitle().length() > 28 ? c.getCourseTitle().substring(0, 27) + ".." : c.getCourseTitle()),
                        c.getIssueDate(),
                        c.getValidityStatus()));
            }
        }

        sb.append("\n========================================================================================\n");
        sb.append("             Verified and Certified by Corporate Talent Development                    \n");
        sb.append("========================================================================================\n");

        return sb.toString();
    }

    /**
     * Generates a Departmental Summary Report.
     */
    public String generateDepartmentSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                    DEPARTMENT TRAINING PARTICIPATION SUMMARY                           \n");
        sb.append("========================================================================================\n\n");
        sb.append(String.format("%-25s | %-12s | %-14s | %-14s\n", "Department", "Staff Count", "Enrollments", "Certificates"));
        sb.append("----------------------------------------------------------------------------------------\n");

        for (Department dept : Department.values()) {
            List<Employee> emps = employeeRepository.findByDepartment(dept);
            int enrCount = 0;
            int certCount = 0;
            for (Employee e : emps) {
                enrCount += enrollmentRepository.findByEmployeeId(e.getId()).size();
                certCount += certificateRepository.findByEmployeeId(e.getId()).size();
            }
            sb.append(String.format("%-25s | %-12d | %-14d | %-14d\n",
                    dept.getDisplayName(), emps.size(), enrCount, certCount));
        }

        sb.append("\n----------------------------------------------------------------------------------------\n");
        sb.append(String.format("Total Staff: %d | Total Sessions: %d | Pass Rate: %.1f%%\n",
                getTotalStaffCount(), getTotalSessionsCount(), calculateOverallPassRate()));
        sb.append("========================================================================================\n");
        return sb.toString();
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }
}
