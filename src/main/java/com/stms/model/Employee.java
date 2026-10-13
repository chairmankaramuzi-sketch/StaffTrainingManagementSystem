package com.stms.model;

import java.time.LocalDate;

/**
 * Concrete class representing a corporate staff employee.
 * Demonstrates Inheritance from Person and Encapsulation.
 */
public class Employee extends Person {
    private static final long serialVersionUID = 1L;

    private Department department;
    private String jobTitle;
    private LocalDate hireDate;
    private boolean active;

    public Employee() {
        super();
        this.department = Department.ENGINEERING;
        this.jobTitle = "Staff Associate";
        this.hireDate = LocalDate.now();
        this.active = true;
    }

    public Employee(String id, String firstName, String lastName, String email, String phone,
                    Department department, String jobTitle, LocalDate hireDate) {
        super(id, firstName, lastName, email, phone);
        setDepartment(department);
        setJobTitle(jobTitle);
        this.hireDate = (hireDate != null) ? hireDate : LocalDate.now();
        this.active = true;
    }

    @Override
    public String getRoleTitle() {
        return "Staff Member - " + (jobTitle != null ? jobTitle : "General");
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = (department != null) ? department : Department.ENGINEERING;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        if (jobTitle == null || jobTitle.trim().isEmpty()) {
            this.jobTitle = "Staff Member";
        } else {
            this.jobTitle = jobTitle.trim();
        }
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean matchesQuery(String query) {
        if (super.matchesQuery(query)) {
            return true;
        }
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.toLowerCase().trim();
        return (department != null && department.getDisplayName().toLowerCase().contains(q)) ||
               (jobTitle != null && jobTitle.toLowerCase().contains(q));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s (%s)", getId(), getFullName(), jobTitle, department.getDisplayName());
    }
}
