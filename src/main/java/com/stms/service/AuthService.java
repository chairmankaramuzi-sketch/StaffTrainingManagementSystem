package com.stms.service;

import com.stms.database.DatabaseManager;
import com.stms.model.User;
import com.stms.model.UserRole;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Handles user accounts and login authentication against the JDBC database.
 * Passwords are stored as salted PBKDF2 hashes, never in plain text.
 */
public class AuthService {
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;

    private final DatabaseManager db;

    public AuthService() {
        this.db = DatabaseManager.getInstance();
        initializeUsersTable();
    }

    private void initializeUsersTable() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users ("
                + "username VARCHAR(50) PRIMARY KEY, "
                + "password_hash VARCHAR(255) NOT NULL, "
                + "salt VARCHAR(100) NOT NULL, "
                + "full_name VARCHAR(150) NOT NULL, "
                + "role VARCHAR(50) NOT NULL, "
                + "active BOOLEAN NOT NULL DEFAULT 1)";
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
        } catch (SQLException e) {
            System.err.println("JDBC Error creating users table: " + e.getMessage());
            return;
        }

        if (countUsers() == 0) {
            // Default accounts - change these passwords after first login.
            createUser("admin", "admin123", "HR Administrator", UserRole.HR_ADMINISTRATOR);
            createUser("trainer", "trainer123", "Training Instructor", UserRole.TRAINER);
            createUser("employee", "employee123", "Staff Member", UserRole.EMPLOYEE);
            System.out.println("Auth: Default user accounts seeded (admin / trainer / employee).");
        }
    }

    private int countUsers() {
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("JDBC Error counting users: " + e.getMessage());
            return 0;
        }
    }

    public boolean createUser(String username, String password, String fullName, UserRole role) {
        String salt = generateSalt();
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, role, active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            ps.setString(2, hash(password, salt));
            ps.setString(3, salt);
            ps.setString(4, fullName);
            ps.setString(5, role.name());
            ps.setBoolean(6, true);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("JDBC Error creating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifies credentials. Returns the user if valid and active, otherwise empty.
     */
    public Optional<User> authenticate(String username, char[] password) {
        if (username == null || username.isBlank() || password == null || password.length == 0) {
            return Optional.empty();
        }
        String sql = "SELECT username, password_hash, salt, full_name, role, active FROM users WHERE username = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean("active")) {
                    return Optional.empty();
                }
                String expected = rs.getString("password_hash");
                String actual = hash(new String(password), rs.getString("salt"));
                if (!constantTimeEquals(expected, actual)) {
                    return Optional.empty();
                }
                return Optional.of(new User(
                        rs.getString("username"),
                        rs.getString("full_name"),
                        UserRole.valueOf(rs.getString("role"))));
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error during login: " + e.getMessage());
            return Optional.empty();
        }
    }

    // =========================================================================
    // USER ACCOUNT MANAGEMENT
    // =========================================================================

    /** Read-only view of a stored account for the management screen. */
    public record UserAccount(String username, String fullName, UserRole role, boolean active) { }

    public List<UserAccount> listUsers() {
        List<UserAccount> list = new ArrayList<>();
        String sql = "SELECT username, full_name, role, active FROM users ORDER BY username";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new UserAccount(rs.getString("username"), rs.getString("full_name"),
                        UserRole.valueOf(rs.getString("role")), rs.getBoolean("active")));
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error listing users: " + e.getMessage());
        }
        return list;
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error checking username: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(String username, String fullName, UserRole role) {
        return executeUpdate("UPDATE users SET full_name = ?, role = ? WHERE username = ?",
                fullName, role.name(), username);
    }

    /** Admin reset: sets a new password without needing the old one. */
    public boolean resetPassword(String username, String newPassword) {
        String salt = generateSalt();
        return executeUpdate("UPDATE users SET password_hash = ?, salt = ? WHERE username = ?",
                hash(newPassword, salt), salt, username);
    }

    /** Self-service change: requires the current password. */
    public boolean changePassword(String username, char[] currentPassword, String newPassword) {
        if (authenticate(username, currentPassword).isEmpty()) {
            return false;
        }
        return resetPassword(username, newPassword);
    }

    public boolean setActive(String username, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE username = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("JDBC Error updating user status: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(String username) {
        return executeUpdate("DELETE FROM users WHERE username = ?", username);
    }

    /** Number of active HR Administrator accounts (used to prevent admin lock-out). */
    public int countActiveAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ? AND active = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UserRole.HR_ADMINISTRATOR.name());
            ps.setBoolean(2, true);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error counting admins: " + e.getMessage());
            return 0;
        }
    }

    /** True while the seeded admin account still uses its default password. */
    public boolean isDefaultAdminPasswordInUse() {
        return authenticate("admin", "admin123".toCharArray()).isPresent();
    }

    private boolean executeUpdate(String sql, String... params) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("JDBC Error updating users: " + e.getMessage());
            return false;
        }
    }

    private static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private static String hash(String password, String salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), ITERATIONS, KEY_LENGTH);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
