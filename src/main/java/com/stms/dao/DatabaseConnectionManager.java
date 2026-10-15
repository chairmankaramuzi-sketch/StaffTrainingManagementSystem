package com.stms.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Enterprise Database Connection Manager for SQLite JDBC.
 * Manages JDBC connections, connection diagnostics, transactions, schema initialization, and backups.
 */
public class DatabaseConnectionManager {
    private static DatabaseConnectionManager instance;

    private static final String DEFAULT_DB_PATH = "data/stms.db";
    private static final String DEFAULT_JDBC_URL = "jdbc:sqlite:" + DEFAULT_DB_PATH;

    private String dbPath = DEFAULT_DB_PATH;
    private String jdbcUrl = DEFAULT_JDBC_URL;

    private DatabaseConnectionManager() {
        ensureDataDirectoryExists();
    }

    public static synchronized DatabaseConnectionManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionManager();
        }
        return instance;
    }

    private void ensureDataDirectoryExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    /**
     * Obtains a standard JDBC Connection with foreign key constraints enabled.
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found on classpath. Ensure sqlite-jdbc is installed.", e);
        }

        Connection conn = DriverManager.getConnection(jdbcUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Tests the database connection by executing a verification query.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            return stmt.execute("SELECT 1;");
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Measures query latency in milliseconds.
     */
    public long getConnectionLatencyMs() {
        long start = System.nanoTime();
        boolean ok = testConnection();
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        return ok ? Math.max(1, elapsed) : -1;
    }

    /**
     * Functional interface for atomic transactional operations.
     */
    @FunctionalInterface
    public interface TransactionWork<T> {
        T execute(Connection conn) throws SQLException;
    }

    /**
     * Executes work atomically inside a transaction (commit on success, rollback on error).
     */
    public <T> T executeInTransaction(TransactionWork<T> work) throws SQLException {
        try (Connection conn = getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                T result = work.execute(conn);
                conn.commit();
                return result;
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    System.err.println("Rollback failed: " + rbEx.getMessage());
                }
                throw ex;
            } finally {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Initializes all relational database tables, constraints, and indexes.
     */
    public void initializeSchema() throws SQLException {
        ensureDataDirectoryExists();

        String createEmployees = """
            CREATE TABLE IF NOT EXISTS employees (
                id VARCHAR(50) PRIMARY KEY,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                email VARCHAR(150) NOT NULL UNIQUE,
                phone VARCHAR(50),
                department VARCHAR(50) NOT NULL,
                job_title VARCHAR(100) NOT NULL,
                hire_date DATE NOT NULL,
                active BOOLEAN NOT NULL DEFAULT 1
            );
        """;

        String createTrainers = """
            CREATE TABLE IF NOT EXISTS trainers (
                id VARCHAR(50) PRIMARY KEY,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                email VARCHAR(150) NOT NULL UNIQUE,
                phone VARCHAR(50),
                specialization VARCHAR(150) NOT NULL,
                years_of_experience INTEGER NOT NULL,
                hourly_rate DOUBLE NOT NULL,
                is_external BOOLEAN NOT NULL DEFAULT 1,
                active BOOLEAN NOT NULL DEFAULT 1
            );
        """;

        String createCourses = """
            CREATE TABLE IF NOT EXISTS courses (
                id VARCHAR(50) PRIMARY KEY,
                course_type VARCHAR(50) NOT NULL,
                title VARCHAR(150) NOT NULL,
                description TEXT,
                duration_hours INTEGER NOT NULL CHECK (duration_hours > 0),
                course_level VARCHAR(50) NOT NULL,
                max_capacity INTEGER NOT NULL CHECK (max_capacity > 0),
                cost_per_seat DOUBLE NOT NULL CHECK (cost_per_seat >= 0),
                specific_attribute VARCHAR(200),
                flag_attribute BOOLEAN NOT NULL DEFAULT 0,
                archived BOOLEAN NOT NULL DEFAULT 0
            );
        """;

        String createSessions = """
            CREATE TABLE IF NOT EXISTS sessions (
                id VARCHAR(50) PRIMARY KEY,
                course_id VARCHAR(50) NOT NULL,
                course_title VARCHAR(150) NOT NULL,
                trainer_id VARCHAR(50) NOT NULL,
                trainer_name VARCHAR(150) NOT NULL,
                session_date DATE NOT NULL,
                time_slot VARCHAR(50) NOT NULL,
                location VARCHAR(150) NOT NULL,
                max_seats INTEGER NOT NULL CHECK (max_seats > 0),
                status VARCHAR(50) NOT NULL,
                FOREIGN KEY (course_id) REFERENCES courses(id) ON UPDATE CASCADE,
                FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON UPDATE CASCADE
            );
        """;

        String createEnrollments = """
            CREATE TABLE IF NOT EXISTS enrollments (
                id VARCHAR(50) PRIMARY KEY,
                session_id VARCHAR(50) NOT NULL,
                employee_id VARCHAR(50) NOT NULL,
                employee_name VARCHAR(150) NOT NULL,
                course_id VARCHAR(50) NOT NULL,
                course_title VARCHAR(150) NOT NULL,
                enrollment_date DATE NOT NULL,
                attended BOOLEAN NOT NULL DEFAULT 0,
                score DOUBLE,
                status VARCHAR(50) NOT NULL,
                certificate_id VARCHAR(50),
                notes TEXT,
                UNIQUE (session_id, employee_id),
                FOREIGN KEY (session_id) REFERENCES sessions(id) ON UPDATE CASCADE,
                FOREIGN KEY (employee_id) REFERENCES employees(id) ON UPDATE CASCADE,
                FOREIGN KEY (course_id) REFERENCES courses(id) ON UPDATE CASCADE
            );
        """;

        String createCertificates = """
            CREATE TABLE IF NOT EXISTS certificates (
                id VARCHAR(50) PRIMARY KEY,
                enrollment_id VARCHAR(50) NOT NULL UNIQUE,
                employee_id VARCHAR(50) NOT NULL,
                employee_name VARCHAR(150) NOT NULL,
                course_id VARCHAR(50) NOT NULL,
                course_title VARCHAR(150) NOT NULL,
                course_category VARCHAR(100) NOT NULL,
                issue_date DATE NOT NULL,
                expiry_date DATE,
                verification_code VARCHAR(100) NOT NULL UNIQUE,
                FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON UPDATE CASCADE,
                FOREIGN KEY (employee_id) REFERENCES employees(id) ON UPDATE CASCADE,
                FOREIGN KEY (course_id) REFERENCES courses(id) ON UPDATE CASCADE
            );
        """;

        String createAuditLogs = """
            CREATE TABLE IF NOT EXISTS audit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                event_message TEXT NOT NULL
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createEmployees);
            stmt.execute(createTrainers);
            stmt.execute(createCourses);
            stmt.execute(createSessions);
            stmt.execute(createEnrollments);
            stmt.execute(createCertificates);
            stmt.execute(createAuditLogs);

            // Performance indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_emp_dept ON employees(department);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sess_course ON sessions(course_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sess_trainer ON sessions(trainer_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_enr_sess ON enrollments(session_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_enr_emp ON enrollments(employee_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cert_code ON certificates(verification_code);");

            // Safe schema migrations for existing databases
            try {
                stmt.execute("ALTER TABLE courses ADD COLUMN archived BOOLEAN NOT NULL DEFAULT 0;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE trainers ADD COLUMN active BOOLEAN NOT NULL DEFAULT 1;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE employees ADD COLUMN active BOOLEAN NOT NULL DEFAULT 1;");
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Checks whether the database is empty (no employees or courses).
     */
    public boolean isDatabaseEmpty() {
        String sql = "SELECT COUNT(*) FROM employees";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            return true;
        }
        return true;
    }

    /**
     * Creates a binary backup copy of the SQLite database.
     */
    public void backupDatabase(File destinationFile) throws IOException, SQLException {
        File currentDb = new File(dbPath);
        if (!currentDb.exists()) {
            throw new IOException("Current database file does not exist: " + dbPath);
        }

        // Checkpoint WAL and ensure flushed
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA wal_checkpoint(FULL);");
        }

        try (FileInputStream in = new FileInputStream(currentDb);
             FileOutputStream out = new FileOutputStream(destinationFile);
             FileChannel srcChannel = in.getChannel();
             FileChannel destChannel = out.getChannel()) {
            destChannel.transferFrom(srcChannel, 0, srcChannel.size());
        }
    }

    /**
     * Restores database from a backup file.
     */
    public void restoreDatabase(File backupFile) throws IOException, SQLException {
        if (!backupFile.exists() || backupFile.length() == 0) {
            throw new IOException("Backup file is empty or does not exist: " + backupFile.getAbsolutePath());
        }

        File currentDb = new File(dbPath);
        try (FileInputStream in = new FileInputStream(backupFile);
             FileOutputStream out = new FileOutputStream(currentDb);
             FileChannel srcChannel = in.getChannel();
             FileChannel destChannel = out.getChannel()) {
            destChannel.transferFrom(srcChannel, 0, srcChannel.size());
        }

        // Test connection to verify restored database
        if (!testConnection()) {
            throw new SQLException("Failed to connect to the restored database.");
        }
    }

    public String getDatabasePath() { return dbPath; }
    public String getJdbcUrl() { return jdbcUrl; }
}
