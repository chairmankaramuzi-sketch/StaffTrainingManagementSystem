package com.stms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for persistent audit trail logging in SQLite.
 */
public class AuditLogDAO {
    private final DatabaseConnectionManager dbManager;

    public AuditLogDAO() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    public void logEvent(String message) {
        String sql = "INSERT INTO audit_logs (event_message) VALUES (?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error saving audit log: " + ex.getMessage());
        }
    }

    public List<String> getRecentLogs(int limit) {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT timestamp, event_message FROM audit_logs ORDER BY id DESC LIMIT ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String ts = rs.getString("timestamp");
                    String msg = rs.getString("event_message");
                    logs.add("[" + ts + "] " + msg);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error reading audit logs: " + ex.getMessage());
        }
        // Return in chronological order
        java.util.Collections.reverse(logs);
        return logs;
    }

    public void clear() {
        String sql = "DELETE FROM audit_logs";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            System.err.println("Error clearing audit logs: " + ex.getMessage());
        }
    }
}
