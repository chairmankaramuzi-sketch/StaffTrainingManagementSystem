package com.stms.repository;

import com.stms.model.interfaces.Identifiable;
import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface defining standard CRUD operations.
 * Demonstrates Generics, Abstraction, and Data Access decoupling.
 *
 * @param <T> Entity type extending Identifiable
 */
public interface Repository<T extends Identifiable> {
    void save(T entity);
    Optional<T> findById(String id);
    List<T> findAll();
    boolean deleteById(String id);
    boolean existsById(String id);
    int count();
    void clear();
}
