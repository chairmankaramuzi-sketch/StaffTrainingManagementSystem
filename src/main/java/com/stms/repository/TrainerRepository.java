package com.stms.repository;

import com.stms.model.Trainer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialized repository for Trainers / Instructors.
 */
public class TrainerRepository extends InMemoryRepository<Trainer> {

    public List<Trainer> findBySpecialization(String spec) {
        if (spec == null || spec.trim().isEmpty()) return findAll();
        String s = spec.toLowerCase().trim();
        return findAll().stream()
                .filter(t -> t.getSpecialization().toLowerCase().contains(s))
                .collect(Collectors.toList());
    }

    public List<Trainer> search(String query) {
        if (query == null || query.trim().isEmpty()) return findAll();
        return findAll().stream()
                .filter(t -> t.matchesQuery(query))
                .collect(Collectors.toList());
    }
}
