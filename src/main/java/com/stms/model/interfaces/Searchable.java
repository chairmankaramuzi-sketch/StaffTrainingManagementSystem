package com.stms.model.interfaces;

/**
 * Interface allowing polymorphic searching across different domain entities.
 */
public interface Searchable {
    boolean matchesQuery(String query);
}
