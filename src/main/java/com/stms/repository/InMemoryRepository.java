package com.stms.repository;

import com.stms.model.interfaces.Identifiable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic In-Memory implementation of Repository using a LinkedHashMap for deterministic order.
 * Demonstrates Generics, Polymorphism, and Encapsulation.
 */
public class InMemoryRepository<T extends Identifiable> implements Repository<T> {
    protected final Map<String, T> storage = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public void save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null.");
        }
        if (entity.getId() == null || entity.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Entity ID cannot be null or empty.");
        }
        storage.put(entity.getId(), entity);
    }

    @Override
    public Optional<T> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        synchronized (storage) {
            return new ArrayList<>(storage.values());
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) return false;
        return storage.remove(id) != null;
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) return false;
        return storage.containsKey(id);
    }

    @Override
    public int count() {
        return storage.size();
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
