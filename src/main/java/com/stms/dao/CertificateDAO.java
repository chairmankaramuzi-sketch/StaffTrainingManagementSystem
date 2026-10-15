package com.stms.dao;

import com.stms.model.Certificate;
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
 * Data Access Object (DAO) for Certificate entities using SQLite JDBC.
 */
public class CertificateDAO {
    private final DatabaseConnectionManager dbManager;

    public CertificateDAO() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    public void insert(Certificate cert) throws SQLException {
        try (Connection conn = dbManager.getConnection()) {
            insertInConnection(cert, conn);
        }
    }

    public void insertInConnection(Certificate cert, Connection conn) throws SQLException {
        // Enforce unique certificate per enrollment
        String checkSql = "SELECT COUNT(*) FROM certificates WHERE enrollment_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, cert.getEnrollmentId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Certificate already issued for enrollment: " + cert.getEnrollmentId());
                }
            }
        }

        String sql = "INSERT INTO certificates (id, enrollment_id, employee_id, employee_name, " +
                     "course_id, course_title, course_category, issue_date, expiry_date, verification_code) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cert.getId());
            pstmt.setString(2, cert.getEnrollmentId());
            pstmt.setString(3, cert.getEmployeeId());
            pstmt.setString(4, cert.getEmployeeName());
            pstmt.setString(5, cert.getCourseId());
            pstmt.setString(6, cert.getCourseTitle());
            pstmt.setString(7, cert.getCourseCategory());
            pstmt.setDate(8, Date.valueOf(cert.getIssueDate()));
            if (cert.getExpiryDate() != null) {
                pstmt.setDate(9, Date.valueOf(cert.getExpiryDate()));
            } else {
                pstmt.setNull(9, Types.DATE);
            }
            pstmt.setString(10, cert.getVerificationCode());
            pstmt.executeUpdate();
        }
    }

    public Optional<Certificate> findById(String id) {
        String sql = "SELECT id, enrollment_id, employee_id, employee_name, course_id, course_title, " +
                     "course_category, issue_date, expiry_date, verification_code FROM certificates WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCertificate(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error finding certificate by id: " + ex.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Certificate> findByEnrollmentId(String enrollmentId) {
        String sql = "SELECT id, enrollment_id, employee_id, employee_name, course_id, course_title, " +
                     "course_category, issue_date, expiry_date, verification_code FROM certificates WHERE enrollment_id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, enrollmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCertificate(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error finding certificate by enrollment: " + ex.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Certificate> findByVerificationCode(String code) {
        String sql = "SELECT id, enrollment_id, employee_id, employee_name, course_id, course_title, " +
                     "course_category, issue_date, expiry_date, verification_code FROM certificates " +
                     "WHERE verification_code = ? COLLATE NOCASE OR id = ? COLLATE NOCASE";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String trimmed = code.trim();
            pstmt.setString(1, trimmed);
            pstmt.setString(2, trimmed);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCertificate(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error verifying certificate: " + ex.getMessage());
        }
        return Optional.empty();
    }

    public List<Certificate> findByEmployeeId(String employeeId) {
        List<Certificate> list = new ArrayList<>();
        String sql = "SELECT id, enrollment_id, employee_id, employee_name, course_id, course_title, " +
                     "course_category, issue_date, expiry_date, verification_code FROM certificates WHERE employee_id = ? ORDER BY issue_date DESC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToCertificate(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching employee certificates: " + ex.getMessage());
        }
        return list;
    }

    public List<Certificate> findAll() {
        List<Certificate> list = new ArrayList<>();
        String sql = "SELECT id, enrollment_id, employee_id, employee_name, course_id, course_title, " +
                     "course_category, issue_date, expiry_date, verification_code FROM certificates ORDER BY issue_date DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToCertificate(rs));
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching certificates: " + ex.getMessage());
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM certificates";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            System.err.println("Error counting certificates: " + ex.getMessage());
        }
        return 0;
    }

    private Certificate mapRowToCertificate(ResultSet rs) throws SQLException {
        Certificate c = new Certificate();
        c.setId(rs.getString("id"));
        c.setEnrollmentId(rs.getString("enrollment_id"));
        c.setEmployeeId(rs.getString("employee_id"));
        c.setEmployeeName(rs.getString("employee_name"));
        c.setCourseId(rs.getString("course_id"));
        c.setCourseTitle(rs.getString("course_title"));
        c.setCourseCategory(rs.getString("course_category"));
        c.setIssueDate(rs.getDate("issue_date").toLocalDate());
        Date expDate = rs.getDate("expiry_date");
        if (expDate != null) {
            c.setExpiryDate(expDate.toLocalDate());
        }
        c.setVerificationCode(rs.getString("verification_code"));
        return c;
    }
}
