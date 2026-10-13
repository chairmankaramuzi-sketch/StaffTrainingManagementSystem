package com.stms.model;

/**
 * Difficulty level for training courses.
 */
public enum CourseLevel {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    EXPERT("Expert");

    private final String label;

    CourseLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static CourseLevel fromString(String text) {
        for (CourseLevel level : CourseLevel.values()) {
            if (level.label.equalsIgnoreCase(text) || level.name().equalsIgnoreCase(text)) {
                return level;
            }
        }
        return BEGINNER;
    }
}
