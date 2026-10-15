package com.stms.dao;

import com.stms.model.Trainer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for Trainer entities using SQLite JDBC.
 */
public class TrainerDAO {
    private final DatabaseConnectionManager dbManager;

    public TrainerDAO() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    public void insert(Trainer t) throws SQLException {
        String sql = "INSERT INTO trainers (id, first_name, last_name, email, phone, specialization, years_of_experience, hourly_rate, is_external, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getId());
            pstmt.setString(2, t.getFirstName());
            pstmt.setString(3, t.getLastName());
            pstmt.setString(4, t.getEmail());
            pstmt.setString(5, t.getPhone());
            pstmt.setString(6, t.getSpecialization());
            pstmt.setInt(7, t.getYearsOfExperience());
            pstmt.setDouble(8, t.getHourlyRate());
            pstmt.setBoolean(9, t.isExternal());
            pstmt.setBoolean(10, t.isActive());
            pstmt.executeUpdate();
        }
    }

    public void update(Trainer t) throws SQLException {
        String sql = "UPDATE trainers SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
                     "specialization = ?, years_of_experience = ?, hourly_rate = ?, is_external = ?, active = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getFirstName());
            pstmt.setString(2, t.getLastName());
            pstmt.setString(3, t.getEmail());
            pstmt.setString(4, t.getPhone());
            pstmt.setString(5, t.getSpecialization());
            pstmt.setInt(6, t.getYearsOfExperience());
            pstmt.setDouble(7, t.getHourlyRate());
            pstmt.setBoolean(8, t.isExternal());
            pstmt.setBoolean(9, t.isActive());
            pstmt.setString(10, t.getId());
            pstmt.executeUpdate();
        }
    }

    public void setActiveStatus(String id, boolean active) throws SQLException {
        String sql = "UPDATE trainers SET active = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, active);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        }
    }

    public boolean delete(String id) throws SQLException {
        // Check if trainer is assigned to scheduled/in-progress sessions
        String checkSql = "SELECT COUNT(*) FROM sessions WHERE trainer_id = ? AND status IN ('SCHEDULED', 'IN_PROGRESS')";
        try (Connection conn = dbManager.getConnection(); PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete trainer " + id + ": assigned to active training sessions. Please deactivate instead.");
                }
            }
        }

        String sql = "DELETE FROM trainers WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Optional<Trainer> findById(String id) {
        String sql = "SELECT id, first_name, last_name, email, phone, specialization, years_of_experience, hourly_rate, is_external, active FROM trainers WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTrainer(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error finding trainer by id: " + ex.getMessage());
        }
        return Optional.empty();
    }

    public List<Trainer> findAll() {
        List<Trainer> list = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, phone, specialization, years_of_experience, hourly_rate, is_external, active FROM trainers ORDER BY id ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToTrainer(rs));
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching trainers: " + ex.getMessage());
        }
        return list;
    }

    public List<Trainer> search(String query, Boolean activeOnly) {
        List<Trainer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, first_name, last_name, email, phone, specialization, years_of_experience, hourly_rate, is_external, active FROM trainers WHERE 1=1 ");

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(id) LIKE ? OR LOWER(specialization) LIKE ?) ");
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
            if (activeOnly != null) {
                pstmt.setBoolean(idx++, activeOnly);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTrainer(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error searching trainers: " + ex.getMessage());
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM trainers";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            System.err.println("Error counting trainers: " + ex.getMessage());
        }
        return 0;
    }

    private Trainer mapRowToTrainer(ResultSet rs) throws SQLException {
        Trainer t = new Trainer(
            rs.getString("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("specialization"),
            rs.getInt("years_of_experience"),
            rs.getDouble("hourly_rate"),
            rs.getBoolean("is_external")
        );
        t.setActive(rs.getBoolean("active"));
        return t;
    }
}
