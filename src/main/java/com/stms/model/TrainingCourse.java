package com.stms.model;

import com.stms.model.interfaces.Certifiable;
import com.stms.model.interfaces.Identifiable;
import com.stms.model.interfaces.Reportable;
import com.stms.model.interfaces.Searchable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class for Training Courses.
 * Demonstrates Abstraction, Encapsulation, and Polymorphism.
 */
public abstract class TrainingCourse implements Identifiable, Certifiable, Searchable, Reportable, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private int durationHours;
    private CourseLevel level;
    private int maxCapacity;
    private double costPerSeat;

    public TrainingCourse() {
        this.level = CourseLevel.BEGINNER;
        this.durationHours = 8;
        this.maxCapacity = 20;
        this.costPerSeat = 0.0;
    }

    public TrainingCourse(String id, String title, String description, int durationHours,
                          CourseLevel level, int maxCapacity, double costPerSeat) {
        if (id != null && !id.trim().isEmpty()) {
            this.id = id.trim();
        } else {
            this.id = "CRS-" + (System.currentTimeMillis() % 100000);
        }
        setTitle(title);
        this.description = (description != null) ? description.trim() : "";
        setDurationHours(durationHours);
        this.level = (level != null) ? level : CourseLevel.BEGINNER;
        setMaxCapacity(maxCapacity);
        setCostPerSeat(costPerSeat);
    }

    // Abstract polymorphic methods to be implemented by specific course categories
    public abstract String getCategoryName();
    public abstract double getPassingScorePercentage();
    public abstract String getSpecialRequirements();

    @Override
    public boolean isEligibleForCertificate(double score, boolean attended) {
        // Business rule: Staff must attend AND achieve minimum polymorphic passing score
        return attended && score >= getPassingScorePercentage();
    }

    @Override
    public String getCertificateTitle() {
        return "Certificate of Completion: " + getTitle();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        if (durationHours <= 0) {
            throw new IllegalArgumentException("Duration hours must be greater than zero.");
        }
        this.durationHours = durationHours;
    }

    public CourseLevel getLevel() {
        return level;
    }

    public void setLevel(CourseLevel level) {
        this.level = level;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be greater than zero.");
        }
        this.maxCapacity = maxCapacity;
    }

    public double getCostPerSeat() {
        return costPerSeat;
    }

    public void setCostPerSeat(double costPerSeat) {
        this.costPerSeat = Math.max(0.0, costPerSeat);
    }

    @Override
    public boolean matchesQuery(String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.toLowerCase().trim();
        return (id != null && id.toLowerCase().contains(q)) ||
               (title != null && title.toLowerCase().contains(q)) ||
               (description != null && description.toLowerCase().contains(q)) ||
               (getCategoryName().toLowerCase().contains(q)) ||
               (level != null && level.name().toLowerCase().contains(q));
    }

    @Override
    public String toFormattedReport() {
        return String.format("[%s] %s | Type: %s | Level: %s | %d hrs | Pass req: %.1f%%",
                id, title, getCategoryName(), level.getLabel(), durationHours, getPassingScorePercentage());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainingCourse that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", id, title, getCategoryName());
    }
}
