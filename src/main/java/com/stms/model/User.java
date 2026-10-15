package com.stms.model;

/**
 * An authenticated system user (login account).
 */
public class User {
    private final String username;
    private final String fullName;
    private final UserRole role;

    public User(String username, String fullName, UserRole role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }
}
