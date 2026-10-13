package com.stms.model;

import com.stms.model.interfaces.Identifiable;
import com.stms.model.interfaces.Searchable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract base class representing a Person in the organization.
 * Demonstrates Abstraction, Encapsulation, and Polymorphism.
 */
public abstract class Person implements Identifiable, Searchable, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate registrationDate;

    // Default constructor for serialization
    public Person() {
        this.registrationDate = LocalDate.now();
    }

    public Person(String id, String firstName, String lastName, String email, String phone) {
        if (id != null && !id.trim().isEmpty()) {
            this.id = id.trim();
        } else {
            this.id = "EMP-" + (System.currentTimeMillis() % 100000);
        }
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhone(phone);
        this.registrationDate = LocalDate.now();
    }

    // Abstract method to be implemented polymorphically by subclasses
    public abstract String getRoleTitle();

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        this.lastName = lastName.trim();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = (phone != null) ? phone.trim() : "";
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public boolean matchesQuery(String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.toLowerCase().trim();
        return (id != null && id.toLowerCase().contains(q)) ||
               (firstName != null && firstName.toLowerCase().contains(q)) ||
               (lastName != null && lastName.toLowerCase().contains(q)) ||
               (email != null && email.toLowerCase().contains(q)) ||
               (getFullName().toLowerCase().contains(q));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", id, getFullName(), getRoleTitle());
    }
}
