package com.stms.model;

/**
 * Technical skills training course (e.g. Java OOP, Cloud Computing, Cybersecurity).
 * Demonstrates Inheritance and Polymorphic behavior.
 */
public class TechnicalCourse extends TrainingCourse {
    private static final long serialVersionUID = 1L;

    private String primaryTechStack;
    private boolean labAssessmentRequired;

    public TechnicalCourse() {
        super();
        this.primaryTechStack = "Java";
        this.labAssessmentRequired = true;
    }

    public TechnicalCourse(String id, String title, String description, int durationHours,
                           CourseLevel level, int maxCapacity, double costPerSeat,
                           String primaryTechStack, boolean labAssessmentRequired) {
        super(id, title, description, durationHours, level, maxCapacity, costPerSeat);
        this.primaryTechStack = (primaryTechStack != null) ? primaryTechStack.trim() : "General Tech";
        this.labAssessmentRequired = labAssessmentRequired;
    }

    @Override
    public String getCategoryName() {
        return "Technical / Engineering";
    }

    @Override
    public double getPassingScorePercentage() {
        return 70.0; // 70% passing threshold for technical modules
    }

    @Override
    public int getCertificateValidityMonths() {
        return 36; // Valid for 3 years
    }

    @Override
    public String getSpecialRequirements() {
        return "Tech Stack: " + primaryTechStack + (labAssessmentRequired ? " | Hands-on Lab Required" : " | Theory Only");
    }

    public String getPrimaryTechStack() {
        return primaryTechStack;
    }

    public void setPrimaryTechStack(String primaryTechStack) {
        this.primaryTechStack = primaryTechStack;
    }

    public boolean isLabAssessmentRequired() {
        return labAssessmentRequired;
    }

    public void setLabAssessmentRequired(boolean labAssessmentRequired) {
        this.labAssessmentRequired = labAssessmentRequired;
    }

    @Override
    public boolean matchesQuery(String query) {
        if (super.matchesQuery(query)) return true;
        if (query == null || query.trim().isEmpty()) return true;
        return primaryTechStack != null && primaryTechStack.toLowerCase().contains(query.toLowerCase().trim());
    }
}
