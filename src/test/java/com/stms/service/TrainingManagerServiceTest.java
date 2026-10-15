package com.stms.service;

import com.stms.model.CourseLevel;
import com.stms.model.Department;
import com.stms.model.Employee;
import com.stms.model.Enrollment;
import com.stms.model.EnrollmentStatus;
import com.stms.model.SessionStatus;
import com.stms.model.TechnicalCourse;
import com.stms.model.Trainer;
import com.stms.model.TrainingCourse;
import com.stms.model.TrainingSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite for TrainingManagerService verifying core OOP business logic,
 * relational database persistence, constraints, and enrollment workflows.
 */
public class TrainingManagerServiceTest {

    private TrainingManagerService service;

    @BeforeEach
    public void setUp() {
        service = new TrainingManagerService();
    }

    @Test
    @DisplayName("Verify service initializes with non-null repositories and default data")
    public void testInitialization() {
        assertNotNull(service);
        assertTrue(service.getTotalStaffCount() > 0, "Staff count should be greater than zero after seeding");
        assertTrue(service.getTotalCoursesCount() > 0, "Course count should be greater than zero after seeding");
        assertTrue(service.getTotalSessionsCount() > 0, "Session count should be greater than zero after seeding");
    }

    @Test
    @DisplayName("Verify employee creation, search, update and status modification")
    public void testEmployeeLifecycle() {
        String testEmpId = "EMP-TEST-" + (System.currentTimeMillis() % 100000);
        Employee emp = new Employee(
                testEmpId,
                "Test",
                "Engineer",
                "test.engineer@example.com",
                "+1-555-0199",
                Department.ENGINEERING,
                "Software QA Specialist",
                LocalDate.of(2023, 1, 15)
        );

        service.registerEmployee(emp);

        Optional<Employee> fetched = service.findEmployeeById(testEmpId);
        assertTrue(fetched.isPresent(), "Newly created employee should be found");
        assertEquals("Test Engineer", fetched.get().getFullName());
        assertEquals(Department.ENGINEERING, fetched.get().getDepartment());

        // Update employee
        emp.setJobTitle("Lead QA Architect");
        service.updateEmployee(emp);
        Optional<Employee> updated = service.findEmployeeById(testEmpId);
        assertTrue(updated.isPresent());
        assertEquals("Lead QA Architect", updated.get().getJobTitle());

        // Deactivate employee
        service.setEmployeeActive(testEmpId, false);
        Optional<Employee> deactivated = service.findEmployeeById(testEmpId);
        assertTrue(deactivated.isPresent());
        assertFalse(deactivated.get().isActive());

        // Clean up
        service.deleteEmployee(testEmpId);
        assertFalse(service.findEmployeeById(testEmpId).isPresent());
    }

    @Test
    @DisplayName("Verify trainer creation, search, and update")
    public void testTrainerLifecycle() {
        String testTrainerId = "TRN-TEST-" + (System.currentTimeMillis() % 100000);
        Trainer trainer = new Trainer(
                testTrainerId,
                "Grace",
                "Hopper",
                "grace.hopper@example.com",
                "+1-555-0199",
                "Compilers & Distributed Systems",
                15,
                95.0,
                true
        );

        service.registerTrainer(trainer);

        Optional<Trainer> found = service.findTrainerById(testTrainerId);
        assertTrue(found.isPresent(), "Registered trainer should be retrieved");
        assertEquals("Grace Hopper", found.get().getFullName());
        assertEquals("Compilers & Distributed Systems", found.get().getSpecialization());

        // Search trainer
        List<Trainer> searchResults = service.searchTrainers("Hopper", null);
        assertFalse(searchResults.isEmpty());

        // Clean up
        service.deleteTrainer(testTrainerId);
        assertFalse(service.findTrainerById(testTrainerId).isPresent());
    }

    @Test
    @DisplayName("Verify course registration and polymorphic behavior")
    public void testCourseCatalog() {
        String courseId = "CRS-TEST-" + (System.currentTimeMillis() % 100000);
        TechnicalCourse techCourse = new TechnicalCourse(
                courseId,
                "Modern Java 21 & Concurrency",
                "Deep dive into Virtual Threads and Structured Concurrency",
                16,
                CourseLevel.ADVANCED,
                20,
                350.0,
                "Java 21, Concurrency",
                true
        );

        service.registerCourse(techCourse);

        Optional<TrainingCourse> found = service.findCourseById(courseId);
        assertTrue(found.isPresent(), "Registered course should be found");
        assertEquals("Technical / Engineering", found.get().getCategoryName());
        assertEquals(16, found.get().getDurationHours());

        // Search courses
        List<TrainingCourse> results = service.searchCourses("Concurrency");
        assertFalse(results.isEmpty());

        // Clean up
        service.deleteCourse(courseId);
        assertFalse(service.findCourseById(courseId).isPresent());
    }

    @Test
    @DisplayName("Verify session scheduling, enrollment, and grade recording")
    public void testSessionAndEnrollmentWorkflow() {
        // Retrieve or ensure active course exists
        List<TrainingCourse> courses = service.getAllCourses();
        assertFalse(courses.isEmpty(), "Courses should exist");
        TrainingCourse course = courses.get(0);

        // Register dedicated active trainer for this test to avoid slot conflicts
        String testTrainerId = "TRN-SESS-" + (System.currentTimeMillis() % 100000);
        Trainer testTrainer = new Trainer(
                testTrainerId, "Session", "Trainer", "sess.trainer@test.com", "555-9988",
                "Advanced Testing", 8, 80.0, false
        );
        service.registerTrainer(testTrainer);

        // Schedule session in future
        LocalDate futureDate = LocalDate.now().plusDays(20 + (long)(Math.random() * 50));
        TrainingSession session = service.scheduleSession(
                course.getId(),
                testTrainer.getId(),
                futureDate,
                "10:00 - 16:00",
                "Conference Room B",
                Math.min(10, course.getMaxCapacity())
        );
        assertNotNull(session);
        assertEquals(SessionStatus.SCHEDULED, session.getStatus());

        // Register temp employee for enrollment test
        String empId = "EMP-ENR-" + (System.currentTimeMillis() % 100000);
        Employee emp = new Employee(empId, "Alice", "Tester", "alice@test.com", "555-1234", Department.ENGINEERING, "QA", LocalDate.now());
        service.registerEmployee(emp);

        // Enroll employee
        Enrollment enrollment = service.enrollStaff(empId, session.getId());
        assertNotNull(enrollment);
        assertEquals(EnrollmentStatus.REGISTERED, enrollment.getStatus());

        // Grade employee with passing score
        service.recordGradeAndAttendance(enrollment.getId(), true, 88.5, "Excellent performance and project submission.");

        Optional<Enrollment> graded = service.getAllEnrollments().stream()
                .filter(e -> e.getId().equals(enrollment.getId()))
                .findFirst();
        assertTrue(graded.isPresent());
        assertEquals(EnrollmentStatus.PASSED, graded.get().getStatus());
        assertEquals(88.5, graded.get().getScore());

        // Verify certificate is generated
        assertTrue(service.getTotalCertificatesCount() > 0);

        // Cancel session instead of delete to respect referential integrity
        service.updateSessionStatus(session.getId(), SessionStatus.CANCELLED);
        assertEquals(SessionStatus.CANCELLED, service.findSessionById(session.getId()).get().getStatus());
    }

    @Test
    @DisplayName("Verify audit log recording and retrieval")
    public void testAuditLog() {
        String testLogMsg = "Unit test audit log verification " + System.currentTimeMillis();
        service.logEvent(testLogMsg);

        List<String> logs = service.getRecentAuditLogs(20);
        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(l -> l.contains("Unit test audit log verification")));
    }
}
