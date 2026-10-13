package com.stms.model;

/**
 * Regulatory and Mandatory Compliance Course (e.g. Data Privacy / GDPR, Safety, Workplace Ethics).
 * Demonstrates strict compliance policy polymorphism (higher passing threshold, annual renewal).
 */
public class ComplianceCourse extends TrainingCourse {
    private static final long serialVersionUID = 1L;

    private String regulatoryStandard;
    private boolean mandatoryForEntireCompany;

    public ComplianceCourse() {
        super();
        this.regulatoryStandard = "General Compliance";
        this.mandatoryForEntireCompany = true;
    }

    public ComplianceCourse(String id, String title, String description, int durationHours,
                            CourseLevel level, int maxCapacity, double costPerSeat,
                            String regulatoryStandard, boolean mandatoryForEntireCompany) {
        super(id, title, description, durationHours, level, maxCapacity, costPerSeat);
        this.regulatoryStandard = (regulatoryStandard != null) ? regulatoryStandard.trim() : "Corporate Policy";
        this.mandatoryForEntireCompany = mandatoryForEntireCompany;
    }

    @Override
    public String getCategoryName() {
        return "Mandatory Compliance";
    }

    @Override
    public double getPassingScorePercentage() {
        return 85.0; // Higher regulatory passing bar (85%)
    }

    @Override
    public int getCertificateValidityMonths() {
        return 12; // Annual renewal mandated by corporate compliance standards
    }

    @Override
    public String getSpecialRequirements() {
        return "Standard: " + regulatoryStandard + (mandatoryForEntireCompany ? " | Mandatory for all staff" : "");
    }

    public String getRegulatoryStandard() {
        return regulatoryStandard;
    }

    public void setRegulatoryStandard(String regulatoryStandard) {
        this.regulatoryStandard = regulatoryStandard;
    }

    public boolean isMandatoryForEntireCompany() {
        return mandatoryForEntireCompany;
    }

    public void setMandatoryForEntireCompany(boolean mandatoryForEntireCompany) {
        this.mandatoryForEntireCompany = mandatoryForEntireCompany;
    }

    @Override
    public boolean matchesQuery(String query) {
        if (super.matchesQuery(query)) return true;
        if (query == null || query.trim().isEmpty()) return true;
        return regulatoryStandard != null && regulatoryStandard.toLowerCase().contains(query.toLowerCase().trim());
    }
}
