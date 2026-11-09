package com.smartspend.dao;

import java.util.List;

/**
 * Generic Data Access Object (DAO) Interface.
 * Demonstrates:
 * 1. Java Interfaces
 * 2. Generics (<T>)
 * 3. Standard CRUD Contract
 *
 * @param <T> Model type handled by this DAO
 */
public interface CrudDao<T> {

    /**
     * Inserts a new record into the database.
     * @param entity The object to persist
     * @return true if successful, false otherwise
     */
    boolean save(T entity);

    /**
     * Finds a single record by its primary key ID.
     * @param id The primary key
     * @return The entity if found, null otherwise
     */
    T findById(int id);

    /**
     * Retrieves all records associated with a specific user.
     * @param userId The ID of the authenticated user
     * @return List of entities
     */
    List<T> findAllByUserId(int userId);

    /**
     * Updates an existing database record.
     * @param entity The entity with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean update(T entity);

    /**
     * Deletes a record by its primary key ID.
     * @param id The primary key
     * @return true if deleted, false otherwise
     */
    boolean delete(int id);
}
