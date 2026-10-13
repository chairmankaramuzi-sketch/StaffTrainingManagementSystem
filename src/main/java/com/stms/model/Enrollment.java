package com.stms.model;

import com.stms.model.interfaces.Identifiable;
import com.stms.model.interfaces.Reportable;
import com.stms.model.interfaces.Searchable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing an Employee's enrollment in a Training Session.
 * Bridges Staff and Course Sessions; performs polymorphic evaluation of results.
 */
public class Enrollment implements Identifiable, Searchable, Reportable, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String sessionId;
    private String employeeId;
    private String employeeName;
    private String courseId;
    private String courseTitle;
    private LocalDate enrollmentDate;
    private boolean attended;
    private Double score; // Nullable if not graded yet
    private EnrollmentStatus status;
    private String certificateId;
    private String notes;

    public Enrollment() {
        this.enrollmentDate = LocalDate.now();
        this.attended = false;
        this.status = EnrollmentStatus.REGISTERED;
    }

    public Enrollment(String id, String sessionId, String employeeId, String employeeName,
                      String courseId, String courseTitle, LocalDate enrollmentDate) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        this.id = id.trim();
        this.sessionId = sessionId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.enrollmentDate = (enrollmentDate != null) ? enrollmentDate : LocalDate.now();
        this.attended = false;
        this.score = null;
        this.status = EnrollmentStatus.REGISTERED;
        this.certificateId = null;
        this.notes = "";
    }

    /**
     * Polymorphically evaluates whether the staff member passed the course.
     * Uses Dynamic Dispatch on the passed TrainingCourse object!
     */
    public boolean evaluateCompletion(TrainingCourse course) {
        if (course == null) return false;

        if (!attended) {
            this.status = EnrollmentStatus.REGISTERED;
            return false;
        }

        if (score == null) {
            this.status = EnrollmentStatus.ATTENDED;
            return false;
        }

        // Polymorphic check: technical vs soft skills vs compliance courses
        // have different required passing score thresholds and criteria!
        boolean passed = course.isEligibleForCertificate(score, attended);
        if (passed) {
            this.status = EnrollmentStatus.PASSED;
        } else {
            this.status = EnrollmentStatus.FAILED;
        }
        return passed;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public boolean isAttended() {
        return attended;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
        if (attended && status == EnrollmentStatus.REGISTERED) {
            this.status = EnrollmentStatus.ATTENDED;
        }
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        if (score != null) {
            this.score = Math.max(0.0, Math.min(100.0, score));
        } else {
            this.score = null;
        }
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean matchesQuery(String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.toLowerCase().trim();
        return (id != null && id.toLowerCase().contains(q)) ||
               (employeeName != null && employeeName.toLowerCase().contains(q)) ||
               (employeeId != null && employeeId.toLowerCase().contains(q)) ||
               (courseTitle != null && courseTitle.toLowerCase().contains(q)) ||
               (status != null && status.name().toLowerCase().contains(q));
    }

    @Override
    public String toFormattedReport() {
        return String.format("[%s] Staff: %s | Course: %s | Attended: %s | Score: %s | Status: %s",
                id, employeeName, courseTitle, attended ? "Yes" : "No",
                (score != null ? String.format("%.1f%%", score) : "N/A"), status.getLabel());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", employeeName, courseTitle, status.getLabel());
    }
}
