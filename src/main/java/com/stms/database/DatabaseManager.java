package com.stms.database;

import com.stms.model.Certificate;
import com.stms.model.ComplianceCourse;
import com.stms.model.CourseLevel;
import com.stms.model.Department;
import com.stms.model.Employee;
import com.stms.model.Enrollment;
import com.stms.model.EnrollmentStatus;
import com.stms.model.SessionStatus;
import com.stms.model.SoftSkillsCourse;
import com.stms.model.TechnicalCourse;
import com.stms.model.Trainer;
import com.stms.model.TrainingCourse;
import com.stms.model.TrainingSession;
import com.stms.service.CourseFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Universal JDBC Database Manager for the Staff Training Management System (STMS).
 * Provides standard JDBC connectivity (Connection, Statement, PreparedStatement, ResultSet)
 * supporting both embedded SQLite (default out-of-the-box for NetBeans) and MySQL.
 */
public class DatabaseManager {
    private static DatabaseManager instance;

    private static final String DEFAULT_DB_PATH = "data/stms.db";
    private static final String DEFAULT_SQLITE_URL = "jdbc:sqlite:" + DEFAULT_DB_PATH;

    private String dbUrl;
    private String dbUser = "";
    private String dbPassword = "";
    private String dbEngine = "SQLite (Embedded JDBC)";

    private DatabaseManager() {
        this.dbUrl = DEFAULT_SQLITE_URL;
        ensureDataDirectoryExists();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
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
     * Establishes a standard JDBC Connection.
     */
    public Connection getConnection() throws SQLException {
        try {
            if (dbUrl.startsWith("jdbc:sqlite")) {
                Class.forName("org.sqlite.JDBC");
            } else if (dbUrl.startsWith("jdbc:mysql")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver class not found: " + e.getMessage());
        }

        if (dbUser.isEmpty() && dbPassword.isEmpty()) {
            return DriverManager.getConnection(dbUrl);
        } else {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        }
    }

    /**
     * Initializes relational database schema with tables and foreign key constraints.
     */
    public void initializeSchema() {
        ensureDataDirectoryExists();

        String createEmployeesTable = """
            CREATE TABLE IF NOT EXISTS employees (
                id VARCHAR(50) PRIMARY KEY,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                email VARCHAR(150) NOT NULL,
                phone VARCHAR(50),
                department VARCHAR(50) NOT NULL,
                job_title VARCHAR(100) NOT NULL,
                hire_date DATE NOT NULL,
                active BOOLEAN NOT NULL DEFAULT 1
            );
        """;

        String createTrainersTable = """
            CREATE TABLE IF NOT EXISTS trainers (
                id VARCHAR(50) PRIMARY KEY,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                email VARCHAR(150) NOT NULL,
                phone VARCHAR(50),
                specialization VARCHAR(150) NOT NULL,
                years_of_experience INTEGER NOT NULL,
                hourly_rate DOUBLE NOT NULL,
                is_external BOOLEAN NOT NULL DEFAULT 1
            );
        """;

        String createCoursesTable = """
            CREATE TABLE IF NOT EXISTS courses (
                id VARCHAR(50) PRIMARY KEY,
                course_type VARCHAR(50) NOT NULL,
                title VARCHAR(150) NOT NULL,
                description TEXT,
                duration_hours INTEGER NOT NULL,
                course_level VARCHAR(50) NOT NULL,
                max_capacity INTEGER NOT NULL,
                cost_per_seat DOUBLE NOT NULL,
                specific_attribute VARCHAR(200),
                flag_attribute BOOLEAN NOT NULL DEFAULT 0
            );
        """;

        String createSessionsTable = """
            CREATE TABLE IF NOT EXISTS sessions (
                id VARCHAR(50) PRIMARY KEY,
                course_id VARCHAR(50) NOT NULL,
                course_title VARCHAR(150) NOT NULL,
                trainer_id VARCHAR(50) NOT NULL,
                trainer_name VARCHAR(150) NOT NULL,
                session_date DATE NOT NULL,
                time_slot VARCHAR(50) NOT NULL,
                location VARCHAR(150) NOT NULL,
                max_seats INTEGER NOT NULL,
                status VARCHAR(50) NOT NULL,
                FOREIGN KEY (course_id) REFERENCES courses(id),
                FOREIGN KEY (trainer_id) REFERENCES trainers(id)
            );
        """;

        String createEnrollmentsTable = """
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
                FOREIGN KEY (session_id) REFERENCES sessions(id),
                FOREIGN KEY (employee_id) REFERENCES employees(id),
                FOREIGN KEY (course_id) REFERENCES courses(id)
            );
        """;

        String createCertificatesTable = """
            CREATE TABLE IF NOT EXISTS certificates (
                id VARCHAR(50) PRIMARY KEY,
                enrollment_id VARCHAR(50) NOT NULL,
                employee_id VARCHAR(50) NOT NULL,
                employee_name VARCHAR(150) NOT NULL,
                course_id VARCHAR(50) NOT NULL,
                course_title VARCHAR(150) NOT NULL,
                course_category VARCHAR(100) NOT NULL,
                issue_date DATE NOT NULL,
                expiry_date DATE,
                verification_code VARCHAR(100) NOT NULL,
                FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
                FOREIGN KEY (employee_id) REFERENCES employees(id),
                FOREIGN KEY (course_id) REFERENCES courses(id)
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createEmployeesTable);
            stmt.execute(createTrainersTable);
            stmt.execute(createCoursesTable);
            stmt.execute(createSessionsTable);
            stmt.execute(createEnrollmentsTable);
            stmt.execute(createCertificatesTable);
            System.out.println("JDBC Database: Schema verified and tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("JDBC Database Error during schema initialization: " + e.getMessage());
        }
    }

    /**
     * Checks if the database is currently empty (needs seeding).
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
            System.err.println("JDBC Error checking empty database: " + e.getMessage());
        }
        return true;
    }

    // =========================================================================
    // JDBC CRUD - EMPLOYEES
    // =========================================================================

    public void insertEmployee(Employee emp) {
        String sql = "INSERT OR REPLACE INTO employees (id, first_name, last_name, email, phone, department, job_title, hire_date, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, emp.getId());
            pstmt.setString(2, emp.getFirstName());
            pstmt.setString(3, emp.getLastName());
            pstmt.setString(4, emp.getEmail());
            pstmt.setString(5, emp.getPhone());
            pstmt.setString(6, emp.getDepartment().name());
            pstmt.setString(7, emp.getJobTitle());
            pstmt.setDate(8, Date.valueOf(emp.getHireDate()));
            pstmt.setBoolean(9, emp.isActive());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("JDBC Error inserting employee: " + e.getMessage());
        }
    }

    public void updateEmployee(Employee emp) {
        insertEmployee(emp); // SQLite INSERT OR REPLACE handles both
    }

    public boolean deleteEmployee(String id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("JDBC Error deleting employee: " + e.getMessage());
            return false;
        }
    }

    public List<Employee> loadAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, phone, department, job_title, hire_date, active FROM employees ORDER BY id ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
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
                list.add(emp);
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error loading employees: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // JDBC CRUD - TRAINERS
    // =========================================================================

    public void insertTrainer(Trainer trn) {
        String sql = "INSERT OR REPLACE INTO trainers (id, first_name, last_name, email, phone, specialization, years_of_experience, hourly_rate, is_external) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trn.getId());
            pstmt.setString(2, trn.getFirstName());
            pstmt.setString(3, trn.getLastName());
            pstmt.setString(4, trn.getEmail());
            pstmt.setString(5, trn.getPhone());
            pstmt.setString(6, trn.getSpecialization());
            pstmt.setInt(7, trn.getYearsOfExperience());
            pstmt.setDouble(8, trn.getHourlyRate());
            pstmt.setBoolean(9, trn.isExternal());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("JDBC Error inserting trainer: " + e.getMessage());
        }
    }

    public List<Trainer> loadAllTrainers() {
        List<Trainer> list = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, phone, specialization, years_of_experience, hourly_rate, is_external FROM trainers ORDER BY id ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Trainer trn = new Trainer(
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
                list.add(trn);
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error loading trainers: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // JDBC CRUD - COURSES
    // =========================================================================

    public void insertCourse(TrainingCourse crs) {
        String sql = "INSERT OR REPLACE INTO courses (id, course_type, title, description, duration_hours, course_level, max_capacity, cost_per_seat, specific_attribute, flag_attribute) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, crs.getId());

            String type = "TECHNICAL";
            String specificAttr = "";
            boolean flagAttr = false;

            if (crs instanceof TechnicalCourse tc) {
                type = "TECHNICAL";
                specificAttr = tc.getPrimaryTechStack();
                flagAttr = tc.isLabAssessmentRequired();
            } else if (crs instanceof SoftSkillsCourse sc) {
                type = "SOFT_SKILLS";
                specificAttr = sc.getCoreCompetency();
                flagAttr = sc.isWorkshopSessionRequired();
            } else if (crs instanceof ComplianceCourse cc) {
                type = "COMPLIANCE";
                specificAttr = cc.getRegulatoryStandard();
                flagAttr = cc.isMandatoryForEntireCompany();
            }

            pstmt.setString(2, type);
            pstmt.setString(3, crs.getTitle());
            pstmt.setString(4, crs.getDescription());
            pstmt.setInt(5, crs.getDurationHours());
            pstmt.setString(6, crs.getLevel().name());
            pstmt.setInt(7, crs.getMaxCapacity());
            pstmt.setDouble(8, crs.getCostPerSeat());
            pstmt.setString(9, specificAttr);
            pstmt.setBoolean(10, flagAttr);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("JDBC Error inserting course: " + e.getMessage());
        }
    }

    public boolean deleteCourse(String id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("JDBC Error deleting course: " + e.getMessage());
            return false;
        }
    }

    public List<TrainingCourse> loadAllCourses() {
        List<TrainingCourse> list = new ArrayList<>();
        String sql = "SELECT id, course_type, title, description, duration_hours, course_level, max_capacity, cost_per_seat, specific_attribute, flag_attribute FROM courses ORDER BY id ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String typeStr = rs.getString("course_type");
                CourseFactory.CourseType type = CourseFactory.CourseType.valueOf(typeStr);
                TrainingCourse c = CourseFactory.createCourse(
                    type,
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("duration_hours"),
                    CourseLevel.valueOf(rs.getString("course_level")),
                    rs.getInt("max_capacity"),
                    rs.getDouble("cost_per_seat"),
                    rs.getString("specific_attribute"),
                    rs.getBoolean("flag_attribute")
                );
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error loading courses: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // JDBC CRUD - SESSIONS
    // =========================================================================

    public void insertSession(TrainingSession sess) {
        String sql = "INSERT OR REPLACE INTO sessions (id, course_id, course_title, trainer_id, trainer_name, session_date, time_slot, location, max_seats, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sess.getId());
            pstmt.setString(2, sess.getCourseId());
            pstmt.setString(3, sess.getCourseTitle());
            pstmt.setString(4, sess.getTrainerId());
            pstmt.setString(5, sess.getTrainerName());
            pstmt.setDate(6, Date.valueOf(sess.getSessionDate()));
            pstmt.setString(7, sess.getTimeSlot());
            pstmt.setString(8, sess.getLocationOrUrl());
            pstmt.setInt(9, sess.getMaxSeats());
            pstmt.setString(10, sess.getStatus().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("JDBC Error inserting session: " + e.getMessage());
        }
    }

    public List<TrainingSession> loadAllSessions() {
        List<TrainingSession> list = new ArrayList<>();
        String sql = "SELECT id, course_id, course_title, trainer_id, trainer_name, session_date, time_slot, location, max_seats, status FROM sessions ORDER BY session_date DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TrainingSession s = new TrainingSession(
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
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error loading sessions: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // JDBC CRUD - ENROLLMENTS
    // =========================================================================

    public void insertEnrollment(Enrollment enr) {
        String sql = "INSERT OR REPLACE INTO enrollments (id, session_id, employee_id, employee_name, course_id, course_title, enrollment_date, attended, score, status, certificate_id, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, enr.getId());
            pstmt.setString(2, enr.getSessionId());
            pstmt.setString(3, enr.getEmployeeId());
            pstmt.setString(4, enr.getEmployeeName());
            pstmt.setString(5, enr.getCourseId());
            pstmt.setString(6, enr.getCourseTitle());
            pstmt.setDate(7, Date.valueOf(enr.getEnrollmentDate()));
            pstmt.setBoolean(8, enr.isAttended());
            if (enr.getScore() != null) {
                pstmt.setDouble(9, enr.getScore());
            } else {
                pstmt.setNull(9, java.sql.Types.DOUBLE);
            }
            pstmt.setString(10, enr.getStatus().name());
            pstmt.setString(11, enr.getCertificateId());
            pstmt.setString(12, enr.getNotes());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("JDBC Error inserting enrollment: " + e.getMessage());
        }
    }

    public List<Enrollment> loadAllEnrollments() {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT id, session_id, employee_id, employee_name, course_id, course_title, enrollment_date, attended, score, status, certificate_id, notes FROM enrollments ORDER BY id ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Enrollment enr = new Enrollment(
                    rs.getString("id"),
                    rs.getString("session_id"),
                    rs.getString("employee_id"),
                    rs.getString("employee_name"),
                    rs.getString("course_id"),
                    rs.getString("course_title"),
                    rs.getDate("enrollment_date").toLocalDate()
                );
                enr.setAttended(rs.getBoolean("attended"));
                double score = rs.getDouble("score");
                if (!rs.wasNull()) {
                    enr.setScore(score);
                }
                enr.setStatus(EnrollmentStatus.valueOf(rs.getString("status")));
                enr.setCertificateId(rs.getString("certificate_id"));
                enr.setNotes(rs.getString("notes"));
                list.add(enr);
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error loading enrollments: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // JDBC CRUD - CERTIFICATES
    // =========================================================================

    public void insertCertificate(Certificate cert) {
        String sql = "INSERT OR REPLACE INTO certificates (id, enrollment_id, employee_id, employee_name, course_id, course_title, course_category, issue_date, expiry_date, verification_code) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
                pstmt.setNull(9, java.sql.Types.DATE);
            }
            pstmt.setString(10, cert.getVerificationCode());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("JDBC Error inserting certificate: " + e.getMessage());
        }
    }

    public List<Certificate> loadAllCertificates() {
        List<Certificate> list = new ArrayList<>();
        String sql = "SELECT id, enrollment_id, employee_id, employee_name, course_id, course_title, course_category, issue_date, expiry_date, verification_code FROM certificates ORDER BY issue_date DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
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
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error loading certificates: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // DATABASE DIAGNOSTICS & METADATA
    // =========================================================================

    public boolean testConnection() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            return stmt.execute("SELECT 1");
        } catch (SQLException e) {
            return false;
        }
    }

    public int getTableRowCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error counting rows in " + tableName + ": " + e.getMessage());
        }
        return 0;
    }

    public String getDatabaseEngine() {
        return dbEngine;
    }

    public String getDatabaseUrl() {
        return dbUrl;
    }

    public void setDatabaseConfiguration(String url, String user, String password, String engineName) {
        this.dbUrl = url;
        this.dbUser = user;
        this.dbPassword = password;
        this.dbEngine = engineName;
        initializeSchema();
    }
}
