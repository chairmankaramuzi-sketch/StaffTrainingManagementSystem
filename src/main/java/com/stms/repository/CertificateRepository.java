package com.stms.repository;

import com.stms.model.Certificate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialized repository for Issued Certificates.
 */
public class CertificateRepository extends InMemoryRepository<Certificate> {

    public List<Certificate> findByEmployeeId(String employeeId) {
        if (employeeId == null) return findAll();
        return findAll().stream()
                .filter(c -> employeeId.equalsIgnoreCase(c.getEmployeeId()))
                .collect(Collectors.toList());
    }

    public List<Certificate> findByCourseId(String courseId) {
        if (courseId == null) return findAll();
        return findAll().stream()
                .filter(c -> courseId.equalsIgnoreCase(c.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<Certificate> search(String query) {
        if (query == null || query.trim().isEmpty()) return findAll();
        return findAll().stream()
                .filter(c -> c.matchesQuery(query))
                .collect(Collectors.toList());
    }
}
