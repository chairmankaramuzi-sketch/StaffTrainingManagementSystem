package com.stms.repository;

import com.stms.model.CourseLevel;
import com.stms.model.TrainingCourse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialized repository for Training Courses.
 */
public class CourseRepository extends InMemoryRepository<TrainingCourse> {

    public List<TrainingCourse> findByLevel(CourseLevel level) {
        if (level == null) return findAll();
        return findAll().stream()
                .filter(c -> c.getLevel() == level)
                .collect(Collectors.toList());
    }

    public List<TrainingCourse> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) return findAll();
        String cat = category.toLowerCase().trim();
        return findAll().stream()
                .filter(c -> c.getCategoryName().toLowerCase().contains(cat))
                .collect(Collectors.toList());
    }

    public List<TrainingCourse> search(String query) {
        if (query == null || query.trim().isEmpty()) return findAll();
        return findAll().stream()
                .filter(c -> c.matchesQuery(query))
                .collect(Collectors.toList());
    }
}
