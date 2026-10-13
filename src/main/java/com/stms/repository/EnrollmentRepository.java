package com.stms.repository;

import com.stms.model.Enrollment;
import com.stms.model.EnrollmentStatus;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialized repository for Staff Enrollments.
 */
public class EnrollmentRepository extends InMemoryRepository<Enrollment> {

    public List<Enrollment> findByEmployeeId(String employeeId) {
        if (employeeId == null) return findAll();
        return findAll().stream()
                .filter(e -> employeeId.equalsIgnoreCase(e.getEmployeeId()))
                .collect(Collectors.toList());
    }

    public List<Enrollment> findBySessionId(String sessionId) {
        if (sessionId == null) return findAll();
        return findAll().stream()
                .filter(e -> sessionId.equalsIgnoreCase(e.getSessionId()))
                .collect(Collectors.toList());
    }

    public List<Enrollment> findByStatus(EnrollmentStatus status) {
        if (status == null) return findAll();
        return findAll().stream()
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
    }

    public boolean isAlreadyEnrolled(String employeeId, String sessionId) {
        if (employeeId == null || sessionId == null) return false;
        return findAll().stream().anyMatch(e ->
                employeeId.equalsIgnoreCase(e.getEmployeeId()) &&
                sessionId.equalsIgnoreCase(e.getSessionId()) &&
                e.getStatus() != EnrollmentStatus.DROPPED
        );
    }

    public int countEnrolledInSession(String sessionId) {
        if (sessionId == null) return 0;
        return (int) findAll().stream()
                .filter(e -> sessionId.equalsIgnoreCase(e.getSessionId()) && e.getStatus() != EnrollmentStatus.DROPPED)
                .count();
    }
}
