package com.stms.repository;

import com.stms.model.SessionStatus;
import com.stms.model.TrainingSession;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialized repository for Training Sessions.
 */
public class SessionRepository extends InMemoryRepository<TrainingSession> {

    public List<TrainingSession> findByStatus(SessionStatus status) {
        if (status == null) return findAll();
        return findAll().stream()
                .filter(s -> s.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<TrainingSession> findByCourseId(String courseId) {
        if (courseId == null) return findAll();
        return findAll().stream()
                .filter(s -> courseId.equalsIgnoreCase(s.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<TrainingSession> findByTrainerId(String trainerId) {
        if (trainerId == null) return findAll();
        return findAll().stream()
                .filter(s -> trainerId.equalsIgnoreCase(s.getTrainerId()))
                .collect(Collectors.toList());
    }

    public List<TrainingSession> search(String query) {
        if (query == null || query.trim().isEmpty()) return findAll();
        return findAll().stream()
                .filter(s -> s.matchesQuery(query))
                .collect(Collectors.toList());
    }
}
