package com.stms.model;

/**
 * Standard business departments in an organization.
 */
public enum Department {
    ENGINEERING("Engineering"),
    HUMAN_RESOURCES("Human Resources"),
    FINANCE("Finance"),
    MARKETING("Marketing"),
    OPERATIONS("Operations"),
    SALES("Sales"),
    LEGAL_COMPLIANCE("Legal & Compliance");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Department fromString(String text) {
        for (Department d : Department.values()) {
            if (d.displayName.equalsIgnoreCase(text) || d.name().equalsIgnoreCase(text)) {
                return d;
            }
        }
        return ENGINEERING;
    }
}
