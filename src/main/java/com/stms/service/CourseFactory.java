package com.stms.service;

import com.stms.model.ComplianceCourse;
import com.stms.model.CourseLevel;
import com.stms.model.SoftSkillsCourse;
import com.stms.model.TechnicalCourse;
import com.stms.model.TrainingCourse;

/**
 * Factory class for creating specific course types.
 * Demonstrates the Factory Method design pattern.
 */
public class CourseFactory {

    public enum CourseType {
        TECHNICAL,
        SOFT_SKILLS,
        COMPLIANCE
    }

    public static TrainingCourse createCourse(CourseType type, String id, String title, String description,
                                              int durationHours, CourseLevel level, int maxCapacity,
                                              double costPerSeat, String specializedAttribute,
                                              boolean flagAttribute) {
        return switch (type) {
            case TECHNICAL -> new TechnicalCourse(
                    id, title, description, durationHours, level, maxCapacity, costPerSeat,
                    specializedAttribute, flagAttribute);
            case SOFT_SKILLS -> new SoftSkillsCourse(
                    id, title, description, durationHours, level, maxCapacity, costPerSeat,
                    specializedAttribute, flagAttribute);
            case COMPLIANCE -> new ComplianceCourse(
                    id, title, description, durationHours, level, maxCapacity, costPerSeat,
                    specializedAttribute, flagAttribute);
        };
    }
}
