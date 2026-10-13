package com.stms.model;

/**
 * Status lifecycle of a Training Session.
 */
public enum SessionStatus {
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    SessionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static SessionStatus fromString(String text) {
        for (SessionStatus s : SessionStatus.values()) {
            if (s.label.equalsIgnoreCase(text) || s.name().equalsIgnoreCase(text)) {
                return s;
            }
        }
        return SCHEDULED;
    }
}
