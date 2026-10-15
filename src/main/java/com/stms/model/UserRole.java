package com.stms.model;

/**
 * Application roles used for login and access control.
 */
public enum UserRole {
    HR_ADMINISTRATOR("HR Administrator"),
    TRAINER("Trainer"),
    EMPLOYEE("Employee");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
