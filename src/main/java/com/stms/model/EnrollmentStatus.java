package com.stms.model;

/**
 * Status of an individual staff enrollment.
 */
public enum EnrollmentStatus {
    REGISTERED("Registered"),
    ATTENDED("Attended"),
    PASSED("Passed & Certified"),
    FAILED("Needs Retake"),
    DROPPED("Dropped"),
    CANCELLED("Cancelled");

    private final String label;

    EnrollmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static EnrollmentStatus fromString(String text) {
        for (EnrollmentStatus s : EnrollmentStatus.values()) {
            if (s.label.equalsIgnoreCase(text) || s.name().equalsIgnoreCase(text)) {
                return s;
            }
        }
        return REGISTERED;
    }
}
