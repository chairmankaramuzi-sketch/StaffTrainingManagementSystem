package com.stms.model;

/**
 * Concrete class representing an Instructor / Trainer.
 * Demonstrates Inheritance from Person and Encapsulation.
 */
public class Trainer extends Person {
    private static final long serialVersionUID = 1L;

    private String specialization;
    private int yearsOfExperience;
    private double hourlyRate;
    private boolean external;
    private boolean active;

    public Trainer() {
        super();
        this.specialization = "General Training";
        this.yearsOfExperience = 3;
        this.hourlyRate = 50.0;
        this.external = false;
        this.active = true;
    }

    public Trainer(String id, String firstName, String lastName, String email, String phone,
                   String specialization, int yearsOfExperience, double hourlyRate, boolean external) {
        super(id, firstName, lastName, email, phone);
        setSpecialization(specialization);
        setYearsOfExperience(yearsOfExperience);
        setHourlyRate(hourlyRate);
        this.external = external;
        this.active = true;
    }

    @Override
    public String getRoleTitle() {
        return (external ? "External Consultant" : "In-House Trainer") + " (" + specialization + ")";
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = (specialization != null) ? specialization.trim() : "General";
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = Math.max(0, yearsOfExperience);
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = Math.max(0.0, hourlyRate);
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
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
        return (specialization != null && specialization.toLowerCase().contains(q));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s (%d yrs exp)", getId(), getFullName(), specialization, yearsOfExperience);
    }
}
