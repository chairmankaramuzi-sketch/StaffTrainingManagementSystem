package com.stms.repository;

import com.stms.model.Department;
import com.stms.model.Employee;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialized repository for Staff Employees with domain query methods.
 */
public class EmployeeRepository extends InMemoryRepository<Employee> {

    public List<Employee> findByDepartment(Department department) {
        if (department == null) return findAll();
        return findAll().stream()
                .filter(e -> e.getDepartment() == department)
                .collect(Collectors.toList());
    }

    public List<Employee> findActiveOnly() {
        return findAll().stream()
                .filter(Employee::isActive)
                .collect(Collectors.toList());
    }

    public List<Employee> search(String query) {
        if (query == null || query.trim().isEmpty()) return findAll();
        return findAll().stream()
                .filter(e -> e.matchesQuery(query))
                .collect(Collectors.toList());
    }
}
