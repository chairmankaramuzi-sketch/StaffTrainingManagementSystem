package com.stms.dao;

import com.stms.model.Enrollment;
import com.stms.model.EnrollmentStatus;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for Enrollment entities using SQLite JDBC.
 */
public class EnrollmentDAO {
    private final DatabaseConnectionManager dbManager;

    public EnrollmentDAO() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    public void insert(Enrollment en) throws SQLException {
        // Enforce duplicate enrollment check
        if (isAlreadyEnrolled(en.getSessionId(), en.getEmployeeId())) {
            throw new SQLException("Duplicate Enrollment Error: Employee " + en.getEmployeeName() +
                    " (" + en.getEmployeeId() + ") is already enrolled in session " + en.getSessionId());
        }

        String sql = "INSERT INTO enrollments (id, session_id, employee_id, employee_name, " +
                     "course_id, course_title, enrollment_date, attended, score, status, certificate_id, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, en.getId());
            pstmt.setString(2, en.getSessionId());
            pstmt.setString(3, en.getEmployeeId());
            pstmt.setString(4, en.getEmployeeName());
            pstmt.setString(5, en.getCourseId());
            pstmt.setString(6, en.getCourseTitle());
            pstmt.setDate(7, Date.valueOf(en.getEnrollmentDate()));
            pstmt.setBoolean(8, en.isAttended());
            if (en.getScore() != null) {
                pstmt.setDouble(9, en.getScore());
            } else {
                pstmt.setNull(9, Types.DOUBLE);
            }
            pstmt.setString(10, en.getStatus().name());
            pstmt.setString(11, en.getCertificateId());
            pstmt.setString(12, en.getNotes());
            pstmt.executeUpdate();
        }
    }

    public void update(Enrollment en) throws SQLException {
        String sql = "UPDATE enrollments SET attended = ?, score = ?, status = ?, certificate_id = ?, notes = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, en.isAttended());
            if (en.getScore() != null) {
                pstmt.setDouble(2, en.getScore());
            } else {
                pstmt.setNull(2, Types.DOUBLE);
            }
            pstmt.setString(3, en.getStatus().name());
            pstmt.setString(4, en.getCertificateId());
            pstmt.setString(5, en.getNotes());
            pstmt.setString(6, en.getId());
            pstmt.executeUpdate();
        }
    }

    public void updateInConnection(Enrollment en, Connection conn) throws SQLException {
        String sql = "UPDATE enrollments SET attended = ?, score = ?, status = ?, certificate_id = ?, notes = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, en.isAttended());
            if (en.getScore() != null) {
                pstmt.setDouble(2, en.getScore());
            } else {
                pstmt.setNull(2, Types.DOUBLE);
            }
            pstmt.setString(3, en.getStatus().name());
            pstmt.setString(4, en.getCertificateId());
            pstmt.setString(5, en.getNotes());
            pstmt.setString(6, en.getId());
            pstmt.executeUpdate();
        }
    }

    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean isAlreadyEnrolled(String sessionId, String employeeId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE session_id = ? AND employee_id = ? AND status != 'CANCELLED'";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            pstmt.setString(2, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println("Error checking existing enrollment: " + ex.getMessage());
            return false;
        }
    }

    public int countEnrolledInSession(String sessionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE session_id = ? AND status != 'CANCELLED'";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.err.println("Error counting session enrollments: " + ex.getMessage());
        }
        return 0;
    }

    public Optional<Enrollment> findById(String id) {
        String sql = "SELECT id, session_id, employee_id, employee_name, course_id, course_title, " +
                     "enrollment_date, attended, score, status, certificate_id, notes FROM enrollments WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEnrollment(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error finding enrollment: " + ex.getMessage());
        }
        return Optional.empty();
    }

    public List<Enrollment> findAll() {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT id, session_id, employee_id, employee_name, course_id, course_title, " +
                     "enrollment_date, attended, score, status, certificate_id, notes FROM enrollments ORDER BY enrollment_date DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToEnrollment(rs));
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching enrollments: " + ex.getMessage());
        }
        return list;
    }

    public List<Enrollment> findBySessionId(String sessionId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT id, session_id, employee_id, employee_name, course_id, course_title, " +
                     "enrollment_date, attended, score, status, certificate_id, notes FROM enrollments WHERE session_id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToEnrollment(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching session attendees: " + ex.getMessage());
        }
        return list;
    }

    public List<Enrollment> findByEmployeeId(String employeeId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT id, session_id, employee_id, employee_name, course_id, course_title, " +
                     "enrollment_date, attended, score, status, certificate_id, notes FROM enrollments WHERE employee_id = ? ORDER BY enrollment_date DESC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToEnrollment(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching employee enrollments: " + ex.getMessage());
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM enrollments";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            System.err.println("Error counting enrollments: " + ex.getMessage());
        }
        return 0;
    }

    public double calculateOverallPassRate() {
        String sql = "SELECT (COUNT(CASE WHEN status = 'PASSED' THEN 1 END) * 100.0) / " +
                     "NULLIF(COUNT(CASE WHEN status IN ('PASSED', 'FAILED') THEN 1 END), 0) FROM enrollments";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                double val = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : val;
            }
        } catch (SQLException ex) {
            System.err.println("Error calculating pass rate: " + ex.getMessage());
        }
        return 0.0;
    }

    private Enrollment mapRowToEnrollment(ResultSet rs) throws SQLException {
        Enrollment en = new Enrollment(
            rs.getString("id"),
            rs.getString("session_id"),
            rs.getString("employee_id"),
            rs.getString("employee_name"),
            rs.getString("course_id"),
            rs.getString("course_title"),
            rs.getDate("enrollment_date").toLocalDate()
        );
        en.setAttended(rs.getBoolean("attended"));
        double score = rs.getDouble("score");
        if (!rs.wasNull()) {
            en.setScore(score);
        }
        en.setStatus(EnrollmentStatus.valueOf(rs.getString("status")));
        en.setCertificateId(rs.getString("certificate_id"));
        en.setNotes(rs.getString("notes"));
        return en;
    }
}
