package com.stms.model;

import com.stms.model.interfaces.Identifiable;
import com.stms.model.interfaces.Reportable;
import com.stms.model.interfaces.Searchable;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Certificate issued upon successful completion of a training course.
 */
public class Certificate implements Identifiable, Searchable, Reportable, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String enrollmentId;
    private String employeeId;
    private String employeeName;
    private String courseId;
    private String courseTitle;
    private String courseCategory;
    private LocalDate issueDate;
    private LocalDate expiryDate; // null means lifetime validity
    private String verificationCode;

    public Certificate() {
        this.issueDate = LocalDate.now();
        this.verificationCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Certificate(String id, String enrollmentId, String employeeId, String employeeName,
                       String courseId, String courseTitle, String courseCategory,
                       LocalDate issueDate, int validityMonths) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Certificate ID cannot be empty.");
        }
        this.id = id.trim();
        this.enrollmentId = enrollmentId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.courseCategory = courseCategory;
        this.issueDate = (issueDate != null) ? issueDate : LocalDate.now();
        if (validityMonths > 0) {
            this.expiryDate = this.issueDate.plusMonths(validityMonths);
        } else {
            this.expiryDate = null; // Lifetime certificate
        }
        this.verificationCode = "STMS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }

    public String getValidityStatus() {
        if (expiryDate == null) {
            return "Lifetime (No Expiry)";
        }
        if (isExpired()) {
            return "EXPIRED on " + expiryDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return "Valid until " + expiryDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseCategory() {
        return courseCategory;
    }

    public void setCourseCategory(String courseCategory) {
        this.courseCategory = courseCategory;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    @Override
    public boolean matchesQuery(String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.toLowerCase().trim();
        return (id != null && id.toLowerCase().contains(q)) ||
               (employeeName != null && employeeName.toLowerCase().contains(q)) ||
               (courseTitle != null && courseTitle.toLowerCase().contains(q)) ||
               (verificationCode != null && verificationCode.toLowerCase().contains(q));
    }

    @Override
    public String toFormattedReport() {
        return String.format("Certificate [%s] | Holder: %s | Course: %s | Issued: %s | Status: %s",
                id, employeeName, courseTitle, issueDate, getValidityStatus());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Certificate that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", id, courseTitle, employeeName);
    }
}
