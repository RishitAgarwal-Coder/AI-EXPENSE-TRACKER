package com.smartspend.model;

import java.sql.Timestamp;

/**
 * Model class representing a User in SmartSpend.
 * Demonstrates:
 * 1. Classes and Objects
 * 2. Encapsulation (Private fields with public getters and setters)
 * 3. Constructors (Default and parameterized)
 * 4. Method Overriding (toString, equals)
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String password;
    private Timestamp createdAt;

    // Default Constructor
    public User() {
    }

    // Parameterized Constructor for registration
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Parameterized Constructor for full profile
    public User(int id, String name, String email, String password, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    // Getters and Setters (Encapsulation)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
