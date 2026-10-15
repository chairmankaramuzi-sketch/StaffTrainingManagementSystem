package com.stms.dao;

import com.stms.model.ComplianceCourse;
import com.stms.model.CourseLevel;
import com.stms.model.SoftSkillsCourse;
import com.stms.model.TechnicalCourse;
import com.stms.model.TrainingCourse;
import com.stms.service.CourseFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for polymorphic TrainingCourse entities using SQLite JDBC.
 */
public class CourseDAO {
    private final DatabaseConnectionManager dbManager;

    public CourseDAO() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    public void insert(TrainingCourse c) throws SQLException {
        String sql = "INSERT INTO courses (id, course_type, title, description, duration_hours, " +
                     "course_level, max_capacity, cost_per_seat, specific_attribute, flag_attribute, archived) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            populatePreparedStatement(pstmt, c);
            pstmt.executeUpdate();
        }
    }

    public void update(TrainingCourse c) throws SQLException {
        String sql = "UPDATE courses SET course_type = ?, title = ?, description = ?, duration_hours = ?, " +
                     "course_level = ?, max_capacity = ?, cost_per_seat = ?, specific_attribute = ?, " +
                     "flag_attribute = ?, archived = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String type = "TECHNICAL";
            String specificAttr = "";
            boolean flagAttr = false;

            if (c instanceof TechnicalCourse tc) {
                type = "TECHNICAL";
                specificAttr = tc.getPrimaryTechStack();
                flagAttr = tc.isLabAssessmentRequired();
            } else if (c instanceof SoftSkillsCourse sc) {
                type = "SOFT_SKILLS";
                specificAttr = sc.getCoreCompetency();
                flagAttr = sc.isWorkshopSessionRequired();
            } else if (c instanceof ComplianceCourse cc) {
                type = "COMPLIANCE";
                specificAttr = cc.getRegulatoryStandard();
                flagAttr = cc.isMandatoryForEntireCompany();
            }

            pstmt.setString(1, type);
            pstmt.setString(2, c.getTitle());
            pstmt.setString(3, c.getDescription());
            pstmt.setInt(4, c.getDurationHours());
            pstmt.setString(5, c.getLevel().name());
            pstmt.setInt(6, c.getMaxCapacity());
            pstmt.setDouble(7, c.getCostPerSeat());
            pstmt.setString(8, specificAttr);
            pstmt.setBoolean(9, flagAttr);
            pstmt.setBoolean(10, c.isArchived());
            pstmt.setString(11, c.getId());

            pstmt.executeUpdate();
        }
    }

    public void setArchivedStatus(String id, boolean archived) throws SQLException {
        String sql = "UPDATE courses SET archived = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, archived);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        }
    }

    public boolean delete(String id) throws SQLException {
        // Check for active sessions
        String checkSql = "SELECT COUNT(*) FROM sessions WHERE course_id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete course " + id + ": existing sessions reference this course. Please archive instead.");
                }
            }
        }

        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Optional<TrainingCourse> findById(String id) {
        String sql = "SELECT id, course_type, title, description, duration_hours, course_level, " +
                     "max_capacity, cost_per_seat, specific_attribute, flag_attribute, archived FROM courses WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCourse(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error finding course by id: " + ex.getMessage());
        }
        return Optional.empty();
    }

    public List<TrainingCourse> findAll(boolean includeArchived) {
        List<TrainingCourse> list = new ArrayList<>();
        String sql = "SELECT id, course_type, title, description, duration_hours, course_level, " +
                     "max_capacity, cost_per_seat, specific_attribute, flag_attribute, archived FROM courses " +
                     (includeArchived ? "" : "WHERE archived = 0 ") + "ORDER BY id ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToCourse(rs));
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching courses: " + ex.getMessage());
        }
        return list;
    }

    public List<TrainingCourse> search(String query, String category, boolean includeArchived) {
        List<TrainingCourse> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, course_type, title, description, duration_hours, course_level, " +
                "max_capacity, cost_per_seat, specific_attribute, flag_attribute, archived FROM courses WHERE 1=1 ");

        if (!includeArchived) {
            sql.append("AND archived = 0 ");
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(title) LIKE ? OR LOWER(id) LIKE ? OR LOWER(description) LIKE ?) ");
        }
        if (category != null && !category.equalsIgnoreCase("ALL")) {
            sql.append("AND course_type = ? ");
        }
        sql.append("ORDER BY id ASC");

        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (query != null && !query.trim().isEmpty()) {
                String q = "%" + query.trim().toLowerCase() + "%";
                pstmt.setString(idx++, q);
                pstmt.setString(idx++, q);
                pstmt.setString(idx++, q);
            }
            if (category != null && !category.equalsIgnoreCase("ALL")) {
                pstmt.setString(idx++, category.toUpperCase());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToCourse(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error searching courses: " + ex.getMessage());
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM courses WHERE archived = 0";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            System.err.println("Error counting courses: " + ex.getMessage());
        }
        return 0;
    }

    private void populatePreparedStatement(PreparedStatement pstmt, TrainingCourse c) throws SQLException {
        pstmt.setString(1, c.getId());

        String type = "TECHNICAL";
        String specificAttr = "";
        boolean flagAttr = false;

        if (c instanceof TechnicalCourse tc) {
            type = "TECHNICAL";
            specificAttr = tc.getPrimaryTechStack();
            flagAttr = tc.isLabAssessmentRequired();
        } else if (c instanceof SoftSkillsCourse sc) {
            type = "SOFT_SKILLS";
            specificAttr = sc.getCoreCompetency();
            flagAttr = sc.isWorkshopSessionRequired();
        } else if (c instanceof ComplianceCourse cc) {
            type = "COMPLIANCE";
            specificAttr = cc.getRegulatoryStandard();
            flagAttr = cc.isMandatoryForEntireCompany();
        }

        pstmt.setString(2, type);
        pstmt.setString(3, c.getTitle());
        pstmt.setString(4, c.getDescription());
        pstmt.setInt(5, c.getDurationHours());
        pstmt.setString(6, c.getLevel().name());
        pstmt.setInt(7, c.getMaxCapacity());
        pstmt.setDouble(8, c.getCostPerSeat());
        pstmt.setString(9, specificAttr);
        pstmt.setBoolean(10, flagAttr);
        pstmt.setBoolean(11, c.isArchived());
    }

    private TrainingCourse mapRowToCourse(ResultSet rs) throws SQLException {
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
        c.setArchived(rs.getBoolean("archived"));
        return c;
    }
}
