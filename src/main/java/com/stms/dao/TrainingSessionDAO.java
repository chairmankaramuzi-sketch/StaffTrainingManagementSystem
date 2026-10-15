package com.stms.dao;

import com.stms.model.SessionStatus;
import com.stms.model.TrainingSession;
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
 * Data Access Object (DAO) for TrainingSession entities using SQLite JDBC.
 */
public class TrainingSessionDAO {
    private final DatabaseConnectionManager dbManager;

    public TrainingSessionDAO() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    public void insert(TrainingSession s) throws SQLException {
        // Enforce trainer schedule conflict rule
        if (hasTrainerConflict(s.getTrainerId(), s.getSessionDate(), s.getTimeSlot(), null)) {
            throw new SQLException("Schedule Conflict: Trainer is already assigned to another session on " +
                    s.getSessionDate() + " during slot " + s.getTimeSlot());
        }

        String sql = "INSERT INTO sessions (id, course_id, course_title, trainer_id, trainer_name, " +
                     "session_date, time_slot, location, max_seats, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getId());
            pstmt.setString(2, s.getCourseId());
            pstmt.setString(3, s.getCourseTitle());
            pstmt.setString(4, s.getTrainerId());
            pstmt.setString(5, s.getTrainerName());
            pstmt.setDate(6, Date.valueOf(s.getSessionDate()));
            pstmt.setString(7, s.getTimeSlot());
            pstmt.setString(8, s.getLocationOrUrl());
            pstmt.setInt(9, s.getMaxSeats());
            pstmt.setString(10, s.getStatus().name());
            pstmt.executeUpdate();
        }
    }

    public void update(TrainingSession s) throws SQLException {
        if (hasTrainerConflict(s.getTrainerId(), s.getSessionDate(), s.getTimeSlot(), s.getId())) {
            throw new SQLException("Schedule Conflict: Trainer is already assigned to another session on " +
                    s.getSessionDate() + " during slot " + s.getTimeSlot());
        }

        String sql = "UPDATE sessions SET course_id = ?, course_title = ?, trainer_id = ?, trainer_name = ?, " +
                     "session_date = ?, time_slot = ?, location = ?, max_seats = ?, status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getCourseId());
            pstmt.setString(2, s.getCourseTitle());
            pstmt.setString(3, s.getTrainerId());
            pstmt.setString(4, s.getTrainerName());
            pstmt.setDate(5, Date.valueOf(s.getSessionDate()));
            pstmt.setString(6, s.getTimeSlot());
            pstmt.setString(7, s.getLocationOrUrl());
            pstmt.setInt(8, s.getMaxSeats());
            pstmt.setString(9, s.getStatus().name());
            pstmt.setString(10, s.getId());
            pstmt.executeUpdate();
        }
    }

    public void updateStatus(String sessionId, SessionStatus status) throws SQLException {
        String sql = "UPDATE sessions SET status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, sessionId);
            pstmt.executeUpdate();
        }
    }

    public boolean delete(String id) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM enrollments WHERE session_id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete session " + id + ": employees are already enrolled. Cancel the session instead.");
                }
            }
        }

        String sql = "DELETE FROM sessions WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean hasTrainerConflict(String trainerId, LocalDate sessionDate, String timeSlot, String excludeSessionId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sessions WHERE trainer_id = ? AND session_date = ? AND time_slot = ? AND status != 'CANCELLED' ");
        if (excludeSessionId != null) {
            sql.append("AND id != ? ");
        }

        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, trainerId);
            pstmt.setDate(2, Date.valueOf(sessionDate));
            pstmt.setString(3, timeSlot);
            if (excludeSessionId != null) {
                pstmt.setString(4, excludeSessionId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println("Error checking trainer schedule conflict: " + ex.getMessage());
            return false;
        }
    }

    public Optional<TrainingSession> findById(String id) {
        String sql = "SELECT id, course_id, course_title, trainer_id, trainer_name, session_date, " +
                     "time_slot, location, max_seats, status FROM sessions WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSession(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error finding session by id: " + ex.getMessage());
        }
        return Optional.empty();
    }

    public List<TrainingSession> findAll() {
        List<TrainingSession> list = new ArrayList<>();
        String sql = "SELECT id, course_id, course_title, trainer_id, trainer_name, session_date, " +
                     "time_slot, location, max_seats, status FROM sessions ORDER BY session_date DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToSession(rs));
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching sessions: " + ex.getMessage());
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM sessions";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            System.err.println("Error counting sessions: " + ex.getMessage());
        }
        return 0;
    }

    private TrainingSession mapRowToSession(ResultSet rs) throws SQLException {
        return new TrainingSession(
            rs.getString("id"),
            rs.getString("course_id"),
            rs.getString("course_title"),
            rs.getString("trainer_id"),
            rs.getString("trainer_name"),
            rs.getDate("session_date").toLocalDate(),
            rs.getString("time_slot"),
            rs.getString("location"),
            rs.getInt("max_seats"),
            SessionStatus.valueOf(rs.getString("status"))
        );
    }
}
