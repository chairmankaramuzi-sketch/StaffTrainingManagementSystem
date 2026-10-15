package com.stms.dao;

import com.stms.model.Department;
import com.stms.model.Employee;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for Employee entities using SQLite JDBC.
 */
public class EmployeeDAO {
    private final DatabaseConnectionManager dbManager;

    public EmployeeDAO() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    public void insert(Employee e) throws SQLException {
        String sql = "INSERT INTO employees (id, first_name, last_name, email, phone, department, job_title, hire_date, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, e.getId());
            pstmt.setString(2, e.getFirstName());
            pstmt.setString(3, e.getLastName());
            pstmt.setString(4, e.getEmail());
            pstmt.setString(5, e.getPhone());
            pstmt.setString(6, e.getDepartment().name());
            pstmt.setString(7, e.getJobTitle());
            pstmt.setDate(8, Date.valueOf(e.getHireDate()));
            pstmt.setBoolean(9, e.isActive());
            pstmt.executeUpdate();
        }
    }

    public void update(Employee e) throws SQLException {
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
                     "department = ?, job_title = ?, hire_date = ?, active = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, e.getFirstName());
            pstmt.setString(2, e.getLastName());
            pstmt.setString(3, e.getEmail());
            pstmt.setString(4, e.getPhone());
            pstmt.setString(5, e.getDepartment().name());
            pstmt.setString(6, e.getJobTitle());
            pstmt.setDate(7, Date.valueOf(e.getHireDate()));
            pstmt.setBoolean(8, e.isActive());
            pstmt.setString(9, e.getId());
            pstmt.executeUpdate();
        }
    }

    public void setActiveStatus(String id, boolean active) throws SQLException {
        String sql = "UPDATE employees SET active = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, active);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        }
    }

    public boolean delete(String id) throws SQLException {
        // Check for active enrollments
        String checkSql = "SELECT COUNT(*) FROM enrollments WHERE employee_id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete employee " + id + ": existing enrollments exist. Please deactivate instead.");
                }
            }
        }

        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Optional<Employee> findById(String id) {
        String sql = "SELECT id, first_name, last_name, email, phone, department, job_title, hire_date, active FROM employees WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEmployee(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error finding employee by id: " + ex.getMessage());
        }
        return Optional.empty();
    }

    public List<Employee> findAll() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, phone, department, job_title, hire_date, active FROM employees ORDER BY id ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToEmployee(rs));
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching employees: " + ex.getMessage());
        }
        return list;
    }

    public List<Employee> search(String query, String department, Boolean activeOnly) {
        List<Employee> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, first_name, last_name, email, phone, department, job_title, hire_date, active FROM employees WHERE 1=1 ");

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(id) LIKE ? OR LOWER(job_title) LIKE ?) ");
        }
        if (department != null && !department.equalsIgnoreCase("ALL")) {
            sql.append("AND department = ? ");
        }
        if (activeOnly != null) {
            sql.append("AND active = ? ");
        }
        sql.append("ORDER BY id ASC");

        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (query != null && !query.trim().isEmpty()) {
                String q = "%" + query.trim().toLowerCase() + "%";
                pstmt.setString(idx++, q);
                pstmt.setString(idx++, q);
                pstmt.setString(idx++, q);
                pstmt.setString(idx++, q);
            }
            if (department != null && !department.equalsIgnoreCase("ALL")) {
                pstmt.setString(idx++, department);
            }
            if (activeOnly != null) {
                pstmt.setBoolean(idx++, activeOnly);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToEmployee(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error searching employees: " + ex.getMessage());
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM employees";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            System.err.println("Error counting employees: " + ex.getMessage());
        }
        return 0;
    }

    private Employee mapRowToEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee(
            rs.getString("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("phone"),
            Department.valueOf(rs.getString("department")),
            rs.getString("job_title"),
            rs.getDate("hire_date").toLocalDate()
        );
        emp.setActive(rs.getBoolean("active"));
        return emp;
    }
}
