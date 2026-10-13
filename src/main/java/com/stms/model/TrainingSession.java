package com.stms.model;

import com.stms.model.interfaces.Identifiable;
import com.stms.model.interfaces.Reportable;
import com.stms.model.interfaces.Searchable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a scheduled execution of a training course.
 * Demonstrates Composition/Association (Course, Trainer).
 */
public class TrainingSession implements Identifiable, Searchable, Reportable, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String courseId;
    private String courseTitle;
    private String trainerId;
    private String trainerName;
    private LocalDate sessionDate;
    private String timeSlot;
    private String locationOrUrl;
    private int maxSeats;
    private SessionStatus status;

    public TrainingSession() {
        this.sessionDate = LocalDate.now().plusDays(7);
        this.timeSlot = "09:00 - 17:00";
        this.maxSeats = 20;
        this.status = SessionStatus.SCHEDULED;
    }

    public TrainingSession(String id, String courseId, String courseTitle,
                           String trainerId, String trainerName,
                           LocalDate sessionDate, String timeSlot,
                           String locationOrUrl, int maxSeats, SessionStatus status) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be empty.");
        }
        this.id = id.trim();
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.trainerId = trainerId;
        this.trainerName = trainerName;
        this.sessionDate = (sessionDate != null) ? sessionDate : LocalDate.now().plusDays(7);
        this.timeSlot = (timeSlot != null) ? timeSlot.trim() : "09:00 - 17:00";
        this.locationOrUrl = (locationOrUrl != null) ? locationOrUrl.trim() : "Virtual Classroom";
        this.maxSeats = Math.max(1, maxSeats);
        this.status = (status != null) ? status : SessionStatus.SCHEDULED;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getLocationOrUrl() {
        return locationOrUrl;
    }

    public void setLocationOrUrl(String locationOrUrl) {
        this.locationOrUrl = locationOrUrl;
    }

    public int getMaxSeats() {
        return maxSeats;
    }

    public void setMaxSeats(int maxSeats) {
        this.maxSeats = Math.max(1, maxSeats);
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    @Override
    public boolean matchesQuery(String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.toLowerCase().trim();
        return (id != null && id.toLowerCase().contains(q)) ||
               (courseTitle != null && courseTitle.toLowerCase().contains(q)) ||
               (trainerName != null && trainerName.toLowerCase().contains(q)) ||
               (locationOrUrl != null && locationOrUrl.toLowerCase().contains(q)) ||
               (status != null && status.name().toLowerCase().contains(q));
    }

    @Override
    public String toFormattedReport() {
        return String.format("[%s] Course: %s | Trainer: %s | Date: %s | Location: %s | Status: %s",
                id, courseTitle, trainerName, sessionDate, locationOrUrl, status.getLabel());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainingSession that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", id, courseTitle, sessionDate);
    }
}
