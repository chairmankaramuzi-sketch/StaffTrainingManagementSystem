package com.stms.ui;

import com.stms.model.ComplianceCourse;
import com.stms.model.CourseLevel;
import com.stms.model.SoftSkillsCourse;
import com.stms.model.TechnicalCourse;
import com.stms.model.TrainingCourse;
import com.stms.service.CourseFactory;
import com.stms.service.TrainingManagerService;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Course Catalog Panel.
 * Demonstrates Abstraction, Polymorphism, and the Factory Pattern.
 * Supports Full CRUD, Editing, and Lifecycle Archiving.
 */
public class CourseCatalogPanel extends JPanel {
    private final TrainingManagerService service;
    private final MainApplicationFrame mainFrame;

    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbCategoryFilter;
    private JCheckBox chkIncludeArchived;

    public CourseCatalogPanel(TrainingManagerService service, MainApplicationFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadCoursesTable();
    }

    private void initComponents() {
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Search Course:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        txtSearch = UITheme.createTextField(14);
        txtSearch.addActionListener(e -> loadCoursesTable());

        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(UITheme.FONT_BOLD);
        cbCategoryFilter = UITheme.createComboBox(new String[]{"All Categories", "Technical", "Soft Skills", "Compliance"});
        cbCategoryFilter.addActionListener(e -> loadCoursesTable());

        chkIncludeArchived = new JCheckBox("Include Archived");
        chkIncludeArchived.setFont(UITheme.FONT_REGULAR);
        chkIncludeArchived.setOpaque(false);
        chkIncludeArchived.addActionListener(e -> loadCoursesTable());

        JButton btnApply = UITheme.createButton("Filter", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnApply.addActionListener(e -> loadCoursesTable());

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblCat);
        filterPanel.add(cbCategoryFilter);
        filterPanel.add(chkIncludeArchived);
        filterPanel.add(btnApply);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd = UITheme.createButton("➕ Add Course", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnAdd.addActionListener(e -> showAddCourseDialog());

        JButton btnEdit = UITheme.createButton("✏️ Edit Course", UITheme.PURPLE, UITheme.TEXT_LIGHT);
        btnEdit.addActionListener(e -> showEditCourseDialog());

        JButton btnArchive = UITheme.createButton("📦 Archive / Restore", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnArchive.addActionListener(e -> toggleArchiveSelectedCourse());

        JButton btnDelete = UITheme.createButton("🗑️ Delete", UITheme.DANGER, UITheme.TEXT_LIGHT);
        btnDelete.addActionListener(e -> deleteSelectedCourse());

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnArchive);
        actionPanel.add(btnDelete);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] columns = {"ID", "Course Title", "Category", "Level", "Hours", "Capacity", "Cost", "Passing Req %", "Details / Requirements", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        UITheme.styleTable(courseTable);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadCoursesTable() {
        tableModel.setRowCount(0);
        String query = txtSearch.getText().trim().toLowerCase();
        String catFilter = (String) cbCategoryFilter.getSelectedItem();
        boolean includeArchived = chkIncludeArchived != null && chkIncludeArchived.isSelected();

        List<TrainingCourse> list = service.getAllCourses(includeArchived);
        for (TrainingCourse c : list) {
            if (catFilter != null && !catFilter.equals("All Categories")) {
                if (!c.getCategoryName().toLowerCase().contains(catFilter.toLowerCase())) {
                    continue;
                }
            }
            if (!query.isEmpty()) {
                boolean matchTitle = c.getTitle() != null && c.getTitle().toLowerCase().contains(query);
                boolean matchId = c.getId() != null && c.getId().toLowerCase().contains(query);
                boolean matchDesc = c.getDescription() != null && c.getDescription().toLowerCase().contains(query);
                boolean matchReq = c.getSpecialRequirements() != null && c.getSpecialRequirements().toLowerCase().contains(query);
                if (!matchTitle && !matchId && !matchDesc && !matchReq) {
                    continue;
                }
            }

            String statusStr = c.isArchived() ? "📦 Archived" : "🟢 Active";

            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getTitle(),
                    c.getCategoryName(),
                    c.getLevel().getLabel(),
                    c.getDurationHours() + " hrs",
                    c.getMaxCapacity(),
                    String.format("$%.2f", c.getCostPerSeat()),
                    String.format("%.1f%%", c.getPassingScorePercentage()),
                    c.getSpecialRequirements(),
                    statusStr
            });
        }
    }

    private void showAddCourseDialog() {
        JDialog dialog = new JDialog(mainFrame, "Create New Course via Factory", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(500, 540);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JComboBox<CourseFactory.CourseType> cbType = UITheme.createComboBox(CourseFactory.CourseType.values());
        JTextField tfTitle = UITheme.createTextField(15);
        JTextField tfDesc = UITheme.createTextField(15);
        JTextField tfHours = UITheme.createTextField(15);
        tfHours.setText("16");
        JComboBox<CourseLevel> cbLevel = UITheme.createComboBox(CourseLevel.values());
        JTextField tfCapacity = UITheme.createTextField(15);
        tfCapacity.setText("25");
        JTextField tfCost = UITheme.createTextField(15);
        tfCost.setText("350.0");
        JTextField tfSpecialAttr = UITheme.createTextField(15);
        tfSpecialAttr.setText("Java 21 / Spring");
        JCheckBox chkFlag = new JCheckBox("Practical Lab / Workshop Mandate", true);

        cbType.addActionListener(e -> {
            CourseFactory.CourseType selected = (CourseFactory.CourseType) cbType.getSelectedItem();
            if (selected == CourseFactory.CourseType.TECHNICAL) {
                tfSpecialAttr.setText("Java / Cloud");
                chkFlag.setText("Hands-on Lab Assessment");
            } else if (selected == CourseFactory.CourseType.SOFT_SKILLS) {
                tfSpecialAttr.setText("Leadership / Collaboration");
                chkFlag.setText("Interactive Group Workshop");
            } else {
                tfSpecialAttr.setText("ISO 27001 / GDPR");
                chkFlag.setText("Mandatory for all Employees");
            }
        });

        form.add(new JLabel("Course Type:"));
        form.add(cbType);
        form.add(new JLabel("Title:"));
        form.add(tfTitle);
        form.add(new JLabel("Description:"));
        form.add(tfDesc);
        form.add(new JLabel("Duration (Hours):"));
        form.add(tfHours);
        form.add(new JLabel("Difficulty Level:"));
        form.add(cbLevel);
        form.add(new JLabel("Max Capacity:"));
        form.add(tfCapacity);
        form.add(new JLabel("Cost per Seat ($):"));
        form.add(tfCost);
        form.add(new JLabel("Stack / Standard / Area:"));
        form.add(tfSpecialAttr);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
        bottom.add(chkFlag, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCreate = UITheme.createButton("Create Course", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        JButton btnCancel = UITheme.createButton("Cancel", UITheme.SECONDARY, UITheme.TEXT_LIGHT);

        btnCreate.addActionListener(e -> {
            try {
                String title = tfTitle.getText().trim();
                String desc = tfDesc.getText().trim();
                int hours = Integer.parseInt(tfHours.getText().trim());
                int capacity = Integer.parseInt(tfCapacity.getText().trim());
                double cost = Double.parseDouble(tfCost.getText().trim());
                String attr = tfSpecialAttr.getText().trim();
                boolean flag = chkFlag.isSelected();
                CourseFactory.CourseType type = (CourseFactory.CourseType) cbType.getSelectedItem();
                CourseLevel level = (CourseLevel) cbLevel.getSelectedItem();

                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Course title cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String courseId = service.getNextCourseId();
                TrainingCourse course = CourseFactory.createCourse(type, courseId, title, desc, hours, level, capacity, cost, attr, flag);
                service.registerCourse(course);

                dialog.dispose();
                loadCoursesTable();
                mainFrame.refreshAllViews();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Hours, capacity, and cost must be valid numbers.", "Format Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnCreate);
        bottom.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditCourseDialog() {
        int selected = courseTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = (String) tableModel.getValueAt(selected, 0);
        TrainingCourse course = service.findCourseById(courseId).orElse(null);
        if (course == null) {
            JOptionPane.showMessageDialog(this, "Course not found: " + courseId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Edit Course [" + course.getId() + "]", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(500, 520);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JTextField tfTitle = UITheme.createTextField(15);
        tfTitle.setText(course.getTitle());
        JTextField tfDesc = UITheme.createTextField(15);
        tfDesc.setText(course.getDescription());
        JTextField tfHours = UITheme.createTextField(15);
        tfHours.setText(String.valueOf(course.getDurationHours()));
        JComboBox<CourseLevel> cbLevel = UITheme.createComboBox(CourseLevel.values());
        cbLevel.setSelectedItem(course.getLevel());
        JTextField tfCapacity = UITheme.createTextField(15);
        tfCapacity.setText(String.valueOf(course.getMaxCapacity()));
        JTextField tfCost = UITheme.createTextField(15);
        tfCost.setText(String.valueOf(course.getCostPerSeat()));
        JTextField tfSpecialAttr = UITheme.createTextField(15);

        boolean initialFlag = false;
        String flagLabel = "Special Requirement Flag";
        if (course instanceof TechnicalCourse tc) {
            tfSpecialAttr.setText(tc.getPrimaryTechStack());
            initialFlag = tc.isLabAssessmentRequired();
            flagLabel = "Hands-on Lab Assessment Required";
        } else if (course instanceof SoftSkillsCourse sc) {
            tfSpecialAttr.setText(sc.getCoreCompetency());
            initialFlag = sc.isWorkshopSessionRequired();
            flagLabel = "Interactive Group Workshop Required";
        } else if (course instanceof ComplianceCourse cc) {
            tfSpecialAttr.setText(cc.getRegulatoryStandard());
            initialFlag = cc.isMandatoryForEntireCompany();
            flagLabel = "Mandatory for all Employees";
        }

        JCheckBox chkFlag = new JCheckBox(flagLabel, initialFlag);

        form.add(new JLabel("Title:"));
        form.add(tfTitle);
        form.add(new JLabel("Description:"));
        form.add(tfDesc);
        form.add(new JLabel("Duration (Hours):"));
        form.add(tfHours);
        form.add(new JLabel("Difficulty Level:"));
        form.add(cbLevel);
        form.add(new JLabel("Max Capacity:"));
        form.add(tfCapacity);
        form.add(new JLabel("Cost per Seat ($):"));
        form.add(tfCost);
        form.add(new JLabel("Special Attribute:"));
        form.add(tfSpecialAttr);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
        bottom.add(chkFlag, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnSave = UITheme.createButton("Save Changes", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        JButton btnCancel = UITheme.createButton("Cancel", UITheme.SECONDARY, UITheme.TEXT_LIGHT);

        btnSave.addActionListener(e -> {
            try {
                String title = tfTitle.getText().trim();
                String desc = tfDesc.getText().trim();
                int hours = Integer.parseInt(tfHours.getText().trim());
                int capacity = Integer.parseInt(tfCapacity.getText().trim());
                double cost = Double.parseDouble(tfCost.getText().trim());
                String attr = tfSpecialAttr.getText().trim();
                boolean flag = chkFlag.isSelected();
                CourseLevel level = (CourseLevel) cbLevel.getSelectedItem();

                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Course title cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                course.setTitle(title);
                course.setDescription(desc);
                course.setDurationHours(hours);
                course.setLevel(level);
                course.setMaxCapacity(capacity);
                course.setCostPerSeat(cost);

                if (course instanceof TechnicalCourse tc) {
                    tc.setPrimaryTechStack(attr);
                    tc.setLabAssessmentRequired(flag);
                } else if (course instanceof SoftSkillsCourse sc) {
                    sc.setCoreCompetency(attr);
                    sc.setWorkshopSessionRequired(flag);
                } else if (course instanceof ComplianceCourse cc) {
                    cc.setRegulatoryStandard(attr);
                    cc.setMandatoryForEntireCompany(flag);
                }

                service.updateCourse(course);
                JOptionPane.showMessageDialog(dialog, "Course details updated successfully in SQLite!", "Success", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
                loadCoursesTable();
                mainFrame.refreshAllViews();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Hours, capacity, and cost must be valid numbers.", "Format Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        bottom.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void toggleArchiveSelectedCourse() {
        int selected = courseTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to archive or restore.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String) tableModel.getValueAt(selected, 0);
        String title = (String) tableModel.getValueAt(selected, 1);
        TrainingCourse course = service.findCourseById(id).orElse(null);
        if (course == null) return;

        boolean newArchiveState = !course.isArchived();
        String prompt = newArchiveState
                ? "Archive course '" + title + "'?\n\nArchived courses are hidden from session scheduling but preserve all historical training records."
                : "Restore course '" + title + "' to active status?";

        int confirm = JOptionPane.showConfirmDialog(this, prompt, "Confirm Status Change", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.setCourseArchived(id, newArchiveState);
            loadCoursesTable();
            mainFrame.refreshAllViews();
        }
    }

    private void deleteSelectedCourse() {
        int selected = courseTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(selected, 0);
        String title = (String) tableModel.getValueAt(selected, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete course: " + title + " [" + id + "]?\n\n(Note: Courses with scheduled or past sessions cannot be permanently deleted; please use 'Archive' instead).",
                "Confirm Permanent Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteCourse(id);
                loadCoursesTable();
                mainFrame.refreshAllViews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Deletion prevented: " + ex.getMessage(),
                        "Integrity Protection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
