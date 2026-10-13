package com.stms.model;

/**
 * Soft skills & Leadership course (e.g. Communication, Agile Leadership, Conflict Resolution).
 * Demonstrates Inheritance and Polymorphic behavior.
 */
public class SoftSkillsCourse extends TrainingCourse {
    private static final long serialVersionUID = 1L;

    private String coreCompetency;
    private boolean workshopSessionRequired;

    public SoftSkillsCourse() {
        super();
        this.coreCompetency = "Communication & Teamwork";
        this.workshopSessionRequired = true;
    }

    public SoftSkillsCourse(String id, String title, String description, int durationHours,
                            CourseLevel level, int maxCapacity, double costPerSeat,
                            String coreCompetency, boolean workshopSessionRequired) {
        super(id, title, description, durationHours, level, maxCapacity, costPerSeat);
        this.coreCompetency = (coreCompetency != null) ? coreCompetency.trim() : "General Soft Skills";
        this.workshopSessionRequired = workshopSessionRequired;
    }

    @Override
    public String getCategoryName() {
        return "Soft Skills & Leadership";
    }

    @Override
    public double getPassingScorePercentage() {
        return 60.0; // 60% passing threshold for soft skills
    }

    @Override
    public int getCertificateValidityMonths() {
        return 0; // Lifetime qualification (no expiry)
    }

    @Override
    public String getSpecialRequirements() {
        return "Competency: " + coreCompetency + (workshopSessionRequired ? " | Role-play Workshop" : "");
    }

    public String getCoreCompetency() {
        return coreCompetency;
    }

    public void setCoreCompetency(String coreCompetency) {
        this.coreCompetency = coreCompetency;
    }

    public boolean isWorkshopSessionRequired() {
        return workshopSessionRequired;
    }

    public void setWorkshopSessionRequired(boolean workshopSessionRequired) {
        this.workshopSessionRequired = workshopSessionRequired;
    }

    @Override
    public boolean matchesQuery(String query) {
        if (super.matchesQuery(query)) return true;
        if (query == null || query.trim().isEmpty()) return true;
        return coreCompetency != null && coreCompetency.toLowerCase().contains(query.toLowerCase().trim());
    }
}
