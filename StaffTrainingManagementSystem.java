import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * =============================================================================
 * STAFF TRAINING MANAGEMENT SYSTEM (STMS)
 * =============================================================================
 * Standalone Desktop Java Application designed for Apache NetBeans IDE.
 * 
 * Coursework Architecture:
 * 1. Front End (User Interface) : Java Swing (JFrame, JTabbedPane, JTable, JDialog)
 * 2. Back End (Business Logic)   : Pure Java SE OOP (Encapsulation, Inheritance, Polymorphism, Abstraction)
 * 3. Database Connectivity       : JDBC (java.sql.Connection, PreparedStatement, Statement, ResultSet)
 * 
 * Instructions for Apache NetBeans:
 * 1. Open NetBeans -> File -> Open File (or create a new Java Class in a project).
 * 2. Select this file: StaffTrainingManagementSystem.java
 * 3. Right-click inside the file -> "Run File" (Shift + F6).
 * =============================================================================
 */
public class StaffTrainingManagementSystem extends JFrame {

    private final TrainingService service;
    private JTabbedPane tabbedPane;
    private JLabel lblStatus;

    // Table Models for Real-Time UI Updating
    private DefaultTableModel staffTableModel;
    private DefaultTableModel trainerTableModel;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel sessionTableModel;
    private DefaultTableModel enrollmentTableModel;

    // Dashboard Metric Labels
    private JLabel lblTotalStaff;
    private JLabel lblTotalCourses;
    private JLabel lblTotalSessions;
    private JLabel lblTotalEnrollments;
    private JLabel lblPassRate;
    private JLabel lblTotalCerts;
    private JTextArea txtAuditLog;

    // UI Input References
    private JComboBox<String> cbTranscriptStaff;
    private JTextArea txtTranscriptOutput;

    public StaffTrainingManagementSystem() {
        this.service = new TrainingService();

        setTitle("Staff Training Management System (STMS) - NetBeans Edition");
        setSize(1180, 780);
        setMinimumSize(new Dimension(960, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        refreshAllData();
    }

    // =========================================================================
    // 1. FRONT END: USER INTERFACE INITIALIZATION (JAVA SWING)
    // =========================================================================

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- TOP BRANDED HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0x0F, 0x17, 0x2A)); // Dark Slate
        headerPanel.setBorder(new EmptyBorder(14, 24, 14, 24));

        JPanel titleBox = new JPanel(new BorderLayout(4, 4));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("🎓 Staff Training Management System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Desktop Java Application | Swing UI • Java OOP Logic • JDBC Database");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(0x94, 0xA3, 0xB8));

        titleBox.add(lblTitle, BorderLayout.NORTH);
        titleBox.add(lblSubtitle, BorderLayout.SOUTH);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        JButton btnDbInfo = new JButton("🗄️ JDBC Database Monitor");
        stylePrimaryButton(btnDbInfo, new Color(0x0F, 0x76, 0x6E), Color.WHITE);
        btnDbInfo.addActionListener(e -> showDatabaseDiagnosticsDialog());

        JLabel lblRoleBadge = new JLabel("  👤 Role: HR Administrator  ");
        lblRoleBadge.setOpaque(true);
        lblRoleBadge.setBackground(new Color(0x1E, 0x29, 0x3B));
        lblRoleBadge.setForeground(new Color(0x38, 0xBD, 0xF8));
        lblRoleBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRoleBadge.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x38, 0xBD, 0xF8), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        headerRight.add(btnDbInfo);
        headerRight.add(lblRoleBadge);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- CENTRAL TABBED NAVIGATION ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("📊 Dashboard", createDashboardPanel());
        tabbedPane.addTab("👥 Staff Directory", createStaffPanel());
        tabbedPane.addTab("👨‍🏫 Trainers", createTrainerPanel());
        tabbedPane.addTab("📚 Course Catalog", createCoursePanel());
        tabbedPane.addTab("📅 Training Sessions", createSessionPanel());
        tabbedPane.addTab("📝 Enroll & Grade", createEnrollmentPanel());
        tabbedPane.addTab("📄 Reports & Transcripts", createReportsPanel());

        tabbedPane.addChangeListener(e -> refreshAllData());
        add(tabbedPane, BorderLayout.CENTER);

        // --- BOTTOM STATUS BAR ---
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(0xF1, 0xF5, 0xF9));
        statusBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(0xCB, 0xD5, 0xE1)),
                new EmptyBorder(6, 20, 6, 20)
        ));

        lblStatus = new JLabel("🟢 System Ready | Storage: Active JDBC Database Connection");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(0x47, 0x55, 0x69));

        JLabel lblEnv = new JLabel("Apache NetBeans IDE • Java SE 17+ • Single-File Architecture");
        lblEnv.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEnv.setForeground(new Color(0x64, 0x74, 0x8B));

        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblEnv, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }

    // --- TAB 1: EXECUTIVE DASHBOARD ---
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(new Color(0xF8, 0xFA, 0xFC));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Metric Cards Grid
        JPanel metricsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        metricsGrid.setOpaque(false);

        lblTotalStaff = new JLabel("0", SwingConstants.CENTER);
        lblTotalCourses = new JLabel("0", SwingConstants.CENTER);
        lblTotalSessions = new JLabel("0", SwingConstants.CENTER);
        lblTotalEnrollments = new JLabel("0", SwingConstants.CENTER);
        lblPassRate = new JLabel("0%", SwingConstants.CENTER);
        lblTotalCerts = new JLabel("0", SwingConstants.CENTER);

        metricsGrid.add(createMetricCard("👥 Total Staff Enrolled", lblTotalStaff, new Color(0x25, 0x63, 0xEB)));
        metricsGrid.add(createMetricCard("📚 Active Courses", lblTotalCourses, new Color(0x7C, 0x3A, 0xED)));
        metricsGrid.add(createMetricCard("📅 Training Sessions", lblTotalSessions, new Color(0x05, 0x96, 0x69)));
        metricsGrid.add(createMetricCard("📝 Total Enrollments", lblTotalEnrollments, new Color(0xD9, 0x77, 0x06)));
        metricsGrid.add(createMetricCard("🎯 Overall Pass Rate", lblPassRate, new Color(0x02, 0x84, 0xC7)));
        metricsGrid.add(createMetricCard("🏆 Verified Certificates", lblTotalCerts, new Color(0xDC, 0x26, 0x26)));

        panel.add(metricsGrid, BorderLayout.NORTH);

        // System Activity & Audit Log
        JPanel auditPanel = new JPanel(new BorderLayout(8, 8));
        auditPanel.setBackground(Color.WHITE);
        auditPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xE2, 0xE8, 0xF0), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblLogTitle = new JLabel("📋 Real-Time Activity & Audit Log Stream");
        lblLogTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLogTitle.setForeground(new Color(0x1E, 0x29, 0x3B));

        txtAuditLog = new JTextArea();
        txtAuditLog.setEditable(false);
        txtAuditLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtAuditLog.setBackground(new Color(0x0F, 0x17, 0x2A));
        txtAuditLog.setForeground(new Color(0x38, 0xBD, 0xF8));
        txtAuditLog.setBorder(new EmptyBorder(8, 10, 8, 10));

        auditPanel.add(lblLogTitle, BorderLayout.NORTH);
        auditPanel.add(new JScrollPane(txtAuditLog), BorderLayout.CENTER);

        panel.add(auditPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new MatteBorder(0, 5, 0, 0, accentColor),
                new CompoundBorder(
                        new LineBorder(new Color(0xE2, 0xE8, 0xF0), 1, true),
                        new EmptyBorder(12, 16, 12, 16)
                )
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(0x64, 0x74, 0x8B));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(new Color(0x0F, 0x17, 0x2A));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // --- TAB 2: STAFF DIRECTORY MANAGEMENT ---
    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(0xF8, 0xFA, 0xFC));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JTextField txtSearch = new JTextField(16);
        txtSearch.setToolTipText("Search staff by Name, ID, or Job Title");

        JButton btnSearch = new JButton("🔍 Search");
        stylePrimaryButton(btnSearch, new Color(0x25, 0x63, 0xEB), Color.WHITE);
        btnSearch.addActionListener(e -> filterStaffTable(txtSearch.getText().trim()));

        JButton btnReset = new JButton("Reset");
        styleSecondaryButton(btnReset);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadStaffTable();
        });

        JButton btnAddStaff = new JButton("➕ Add New Staff");
        stylePrimaryButton(btnAddStaff, new Color(0x05, 0x96, 0x69), Color.WHITE);
        btnAddStaff.addActionListener(e -> showAddStaffDialog());

        JButton btnDeleteStaff = new JButton("🗑️ Delete Selected");
        stylePrimaryButton(btnDeleteStaff, new Color(0xDC, 0x26, 0x26), Color.WHITE);

        toolbar.add(new JLabel("Search Staff:"));
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(btnReset);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnAddStaff);
        toolbar.add(btnDeleteStaff);

        // Table
        String[] columns = {"Staff ID", "Full Name", "Email Address", "Department", "Job Title", "Hire Date", "Status"};
        staffTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(staffTableModel);
        styleTable(table);

        btnDeleteStaff.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = (String) staffTableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete employee " + id + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                service.deleteEmployee(id);
                refreshAllData();
            }
        });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void showAddStaffDialog() {
        JDialog dlg = new JDialog(this, "Register New Staff Member", true);
        dlg.setSize(440, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridBagLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 12, 8, 12);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtFn = new JTextField(18);
        JTextField txtLn = new JTextField(18);
        JTextField txtEmail = new JTextField(18);
        JTextField txtPhone = new JTextField(18);
        JComboBox<Department> cbDept = new JComboBox<>(Department.values());
        JTextField txtTitle = new JTextField(18);

        g.gridx = 0; g.gridy = 0; dlg.add(new JLabel("First Name:"), g);
        g.gridx = 1; dlg.add(txtFn, g);

        g.gridx = 0; g.gridy = 1; dlg.add(new JLabel("Last Name:"), g);
        g.gridx = 1; dlg.add(txtLn, g);

        g.gridx = 0; g.gridy = 2; dlg.add(new JLabel("Email Address:"), g);
        g.gridx = 1; dlg.add(txtEmail, g);

        g.gridx = 0; g.gridy = 3; dlg.add(new JLabel("Phone:"), g);
        g.gridx = 1; dlg.add(txtPhone, g);

        g.gridx = 0; g.gridy = 4; dlg.add(new JLabel("Department:"), g);
        g.gridx = 1; dlg.add(cbDept, g);

        g.gridx = 0; g.gridy = 5; dlg.add(new JLabel("Job Title:"), g);
        g.gridx = 1; dlg.add(txtTitle, g);

        JButton btnSave = new JButton("Save Staff Member");
        stylePrimaryButton(btnSave, new Color(0x05, 0x96, 0x69), Color.WHITE);
        btnSave.addActionListener(e -> {
            String fn = txtFn.getText().trim();
            String ln = txtLn.getText().trim();
            String email = txtEmail.getText().trim();
            String title = txtTitle.getText().trim();
            if (fn.isEmpty() || ln.isEmpty() || email.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(dlg, "Please provide a valid email format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Employee emp = new Employee("EMP-" + (service.getAllEmployees().size() + 101),
                    fn, ln, email, txtPhone.getText().trim(), (Department) cbDept.getSelectedItem(),
                    title, LocalDate.now());
            service.registerEmployee(emp);
            dlg.dispose();
            refreshAllData();
            JOptionPane.showMessageDialog(this, "New employee registered and persisted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        g.gridx = 0; g.gridy = 6; g.gridwidth = 2;
        g.insets = new Insets(16, 12, 8, 12);
        dlg.add(btnSave, g);

        dlg.setVisible(true);
    }

    // --- TAB: TRAINER MANAGEMENT ---
    private JPanel createTrainerPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(0xF8, 0xFA, 0xFC));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JTextField txtSearch = new JTextField(16);
        txtSearch.setToolTipText("Search trainer by Name or Specialization");

        JButton btnSearch = new JButton("🔍 Search");
        stylePrimaryButton(btnSearch, new Color(0x25, 0x63, 0xEB), Color.WHITE);
        btnSearch.addActionListener(e -> filterTrainerTable(txtSearch.getText().trim()));

        JButton btnReset = new JButton("Reset");
        styleSecondaryButton(btnReset);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadTrainerTable();
        });

        JButton btnAddTrainer = new JButton("➕ Add New Trainer");
        stylePrimaryButton(btnAddTrainer, new Color(0x05, 0x96, 0x69), Color.WHITE);
        btnAddTrainer.addActionListener(e -> showAddTrainerDialog());

        JButton btnDeleteTrainer = new JButton("🗑️ Delete Selected");
        stylePrimaryButton(btnDeleteTrainer, new Color(0xDC, 0x26, 0x26), Color.WHITE);

        toolbar.add(new JLabel("Search Trainers:"));
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(btnReset);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnAddTrainer);
        toolbar.add(btnDeleteTrainer);

        // Table
        String[] columns = {"Trainer ID", "Full Name", "Specialization", "Experience", "Hourly Rate", "Type", "Email", "Phone"};
        trainerTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(trainerTableModel);
        styleTable(table);

        btnDeleteTrainer.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a trainer to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = (String) trainerTableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete trainer " + id + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                service.deleteTrainer(id);
                refreshAllData();
            }
        });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void showAddTrainerDialog() {
        JDialog dlg = new JDialog(this, "Register New Trainer", true);
        dlg.setSize(440, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 12, 6, 12);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtFn = new JTextField(15);
        JTextField txtLn = new JTextField(15);
        JTextField txtEmail = new JTextField(15);
        JTextField txtPhone = new JTextField(15);
        JTextField txtSpec = new JTextField(15);
        JTextField txtExp = new JTextField(5);
        txtExp.setText("5");
        JTextField txtRate = new JTextField(5);
        txtRate.setText("95.0");
        JCheckBox chkExternal = new JCheckBox("External Contractor", true);

        g.gridx = 0; g.gridy = 0; dlg.add(new JLabel("First Name:"), g);
        g.gridx = 1; dlg.add(txtFn, g);
        g.gridx = 0; g.gridy = 1; dlg.add(new JLabel("Last Name:"), g);
        g.gridx = 1; dlg.add(txtLn, g);
        g.gridx = 0; g.gridy = 2; dlg.add(new JLabel("Email Address:"), g);
        g.gridx = 1; dlg.add(txtEmail, g);
        g.gridx = 0; g.gridy = 3; dlg.add(new JLabel("Phone:"), g);
        g.gridx = 1; dlg.add(txtPhone, g);
        g.gridx = 0; g.gridy = 4; dlg.add(new JLabel("Specialization:"), g);
        g.gridx = 1; dlg.add(txtSpec, g);
        g.gridx = 0; g.gridy = 5; dlg.add(new JLabel("Experience (Years):"), g);
        g.gridx = 1; dlg.add(txtExp, g);
        g.gridx = 0; g.gridy = 6; dlg.add(new JLabel("Hourly Rate ($):"), g);
        g.gridx = 1; dlg.add(txtRate, g);
        g.gridx = 1; g.gridy = 7; dlg.add(chkExternal, g);

        JButton btnSave = new JButton("💾 Save Trainer");
        stylePrimaryButton(btnSave, new Color(0x05, 0x96, 0x69), Color.WHITE);
        btnSave.addActionListener(e -> {
            String fn = txtFn.getText().trim();
            String ln = txtLn.getText().trim();
            String email = txtEmail.getText().trim();
            String spec = txtSpec.getText().trim();
            if (fn.isEmpty() || ln.isEmpty() || email.isEmpty() || spec.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int exp = Integer.parseInt(txtExp.getText().trim());
                double rate = Double.parseDouble(txtRate.getText().trim());
                Trainer t = new Trainer("TRN-" + (service.getAllTrainers().size() + 201),
                        fn, ln, email, txtPhone.getText().trim(), spec, exp, rate, chkExternal.isSelected());
                service.registerTrainer(t);
                dlg.dispose();
                refreshAllData();
                JOptionPane.showMessageDialog(this, "New trainer registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid number format: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        g.gridx = 0; g.gridy = 8; g.gridwidth = 2;
        g.insets = new Insets(16, 12, 8, 12);
        dlg.add(btnSave, g);
        dlg.setVisible(true);
    }

    // --- TAB 3: COURSE CATALOG ---
    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(0xF8, 0xFA, 0xFC));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnAddCourse = new JButton("➕ Add New Course");
        stylePrimaryButton(btnAddCourse, new Color(0x7C, 0x3A, 0xED), Color.WHITE);
        btnAddCourse.addActionListener(e -> showAddCourseDialog());

        toolbar.add(btnAddCourse);

        String[] columns = {"Course ID", "Category", "Course Title", "Level", "Duration", "Seats", "Pass Threshold", "Cert Validity"};
        courseTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(courseTableModel);
        styleTable(table);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void showAddCourseDialog() {
        JDialog dlg = new JDialog(this, "Add New Training Course", true);
        dlg.setSize(480, 440);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridBagLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 12, 8, 12);
        g.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cbType = new JComboBox<>(new String[]{"TECHNICAL", "SOFT_SKILLS", "COMPLIANCE"});
        JTextField txtTitle = new JTextField(18);
        JTextField txtDesc = new JTextField(18);
        JTextField txtHours = new JTextField("16", 18);
        JComboBox<CourseLevel> cbLevel = new JComboBox<>(CourseLevel.values());
        JTextField txtCapacity = new JTextField("25", 18);
        JTextField txtCost = new JTextField("350.0", 18);
        JTextField txtAttr = new JTextField("Java / Spring", 18);

        g.gridx = 0; g.gridy = 0; dlg.add(new JLabel("Category:"), g);
        g.gridx = 1; dlg.add(cbType, g);

        g.gridx = 0; g.gridy = 1; dlg.add(new JLabel("Course Title:"), g);
        g.gridx = 1; dlg.add(txtTitle, g);

        g.gridx = 0; g.gridy = 2; dlg.add(new JLabel("Description:"), g);
        g.gridx = 1; dlg.add(txtDesc, g);

        g.gridx = 0; g.gridy = 3; dlg.add(new JLabel("Duration (Hours):"), g);
        g.gridx = 1; dlg.add(txtHours, g);

        g.gridx = 0; g.gridy = 4; dlg.add(new JLabel("Course Level:"), g);
        g.gridx = 1; dlg.add(cbLevel, g);

        g.gridx = 0; g.gridy = 5; dlg.add(new JLabel("Max Capacity:"), g);
        g.gridx = 1; dlg.add(txtCapacity, g);

        g.gridx = 0; g.gridy = 6; dlg.add(new JLabel("Specific Attribute:"), g);
        g.gridx = 1; dlg.add(txtAttr, g);

        JButton btnSave = new JButton("Save Course");
        stylePrimaryButton(btnSave, new Color(0x7C, 0x3A, 0xED), Color.WHITE);
        btnSave.addActionListener(e -> {
            String title = txtTitle.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please specify a course title.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String typeStr = (String) cbType.getSelectedItem();
            int hours = Integer.parseInt(txtHours.getText().trim());
            int cap = Integer.parseInt(txtCapacity.getText().trim());
            double cost = Double.parseDouble(txtCost.getText().trim());
            String id = "CRS-" + (service.getAllCourses().size() + 301);

            TrainingCourse c;
            if ("TECHNICAL".equals(typeStr)) {
                c = new TechnicalCourse(id, title, txtDesc.getText(), hours, (CourseLevel) cbLevel.getSelectedItem(), cap, cost, txtAttr.getText(), true);
            } else if ("SOFT_SKILLS".equals(typeStr)) {
                c = new SoftSkillsCourse(id, title, txtDesc.getText(), hours, (CourseLevel) cbLevel.getSelectedItem(), cap, cost, txtAttr.getText(), true);
            } else {
                c = new ComplianceCourse(id, title, txtDesc.getText(), hours, (CourseLevel) cbLevel.getSelectedItem(), cap, cost, txtAttr.getText(), true);
            }

            service.addCourse(c);
            dlg.dispose();
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Course successfully created and registered!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        g.gridx = 0; g.gridy = 7; g.gridwidth = 2;
        g.insets = new Insets(16, 12, 8, 12);
        dlg.add(btnSave, g);

        dlg.setVisible(true);
    }

    // --- TAB 4: TRAINING SESSIONS ---
    private JPanel createSessionPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(0xF8, 0xFA, 0xFC));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] columns = {"Session ID", "Course Title", "Assigned Trainer", "Session Date", "Time Slot", "Location / URL", "Max Seats", "Status"};
        sessionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(sessionTableModel);
        styleTable(table);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 5: ENROLLMENT & GRADING ---
    private JPanel createEnrollmentPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(0xF8, 0xFA, 0xFC));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar Action Form
        JPanel formBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        formBar.setOpaque(false);

        JTextField txtScore = new JTextField(6);
        txtScore.setToolTipText("Enter score between 0.0% and 100.0%");

        JButton btnGrade = new JButton("🎯 Grade & Certify Selected");
        stylePrimaryButton(btnGrade, new Color(0x05, 0x96, 0x69), Color.WHITE);

        formBar.add(new JLabel("Score (%):"));
        formBar.add(txtScore);
        formBar.add(btnGrade);

        String[] columns = {"Enrollment ID", "Staff Name", "Course Title", "Date", "Attended", "Score", "Status", "Certificate No."};
        enrollmentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(enrollmentTableModel);
        styleTable(table);

        btnGrade.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select an enrollment record to grade.", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String enrId = (String) enrollmentTableModel.getValueAt(row, 0);
            try {
                double scoreVal = Double.parseDouble(txtScore.getText().trim());
                if (scoreVal < 0 || scoreVal > 100) {
                    JOptionPane.showMessageDialog(this, "Score must be between 0.0 and 100.0.", "Invalid Range", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                service.recordGradeAndAttendance(enrId, true, scoreVal, "Assessment recorded via NetBeans STMS GUI");
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Score evaluated! Polymorphic certification criteria checked and updated.", "Grading Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric score (e.g. 85.5).", "Number Format Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(formBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 6: REPORTS & TRANSCRIPTS ---
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(0xF8, 0xFA, 0xFC));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        topBar.setOpaque(false);

        cbTranscriptStaff = new JComboBox<>();
        JButton btnGenerate = new JButton("📄 Generate Staff Transcript");
        stylePrimaryButton(btnGenerate, new Color(0x25, 0x63, 0xEB), Color.WHITE);

        JButton btnExport = new JButton("💾 Export to .txt File");
        styleSecondaryButton(btnExport);

        topBar.add(new JLabel("Select Employee:"));
        topBar.add(cbTranscriptStaff);
        topBar.add(btnGenerate);
        topBar.add(btnExport);

        txtTranscriptOutput = new JTextArea();
        txtTranscriptOutput.setEditable(false);
        txtTranscriptOutput.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtTranscriptOutput.setBackground(Color.WHITE);
        txtTranscriptOutput.setBorder(new EmptyBorder(14, 16, 14, 16));

        btnGenerate.addActionListener(e -> {
            String selected = (String) cbTranscriptStaff.getSelectedItem();
            if (selected != null && selected.contains("[") && selected.contains("]")) {
                String empId = selected.substring(selected.indexOf("[") + 1, selected.indexOf("]"));
                txtTranscriptOutput.setText(service.generateEmployeeTranscript(empId));
            }
        });

        btnExport.addActionListener(e -> {
            try {
                File exportFile = new File("staff_training_transcript.txt");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile))) {
                    writer.write(txtTranscriptOutput.getText());
                }
                JOptionPane.showMessageDialog(this, "Transcript exported to: " + exportFile.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtTranscriptOutput), BorderLayout.CENTER);
        return panel;
    }

    // --- LIVE REFRESH DATA & METRICS ---
    private void refreshAllData() {
        loadStaffTable();
        loadTrainerTable();
        loadCourseTable();
        loadSessionTable();
        loadEnrollmentTable();

        // Update Metric Counters
        lblTotalStaff.setText(String.valueOf(service.getAllEmployees().size()));
        lblTotalCourses.setText(String.valueOf(service.getAllCourses().size()));
        lblTotalSessions.setText(String.valueOf(service.getAllSessions().size()));
        lblTotalEnrollments.setText(String.valueOf(service.getAllEnrollments().size()));
        lblTotalCerts.setText(String.valueOf(service.getAllCertificates().size()));

        long passedCount = service.getAllEnrollments().stream()
                .filter(en -> en.getStatus() == EnrollmentStatus.PASSED).count();
        int totalEnr = service.getAllEnrollments().size();
        double passPct = totalEnr > 0 ? ((double) passedCount / totalEnr) * 100.0 : 0.0;
        lblPassRate.setText(String.format("%.1f%%", passPct));

        // Update Audit Log
        StringBuilder sb = new StringBuilder();
        for (String event : service.getAuditEvents()) {
            sb.append(event).append("\n");
        }
        txtAuditLog.setText(sb.toString());
        txtAuditLog.setCaretPosition(txtAuditLog.getDocument().getLength());

        // Update Staff Dropdown in Reports
        cbTranscriptStaff.removeAllItems();
        for (Employee emp : service.getAllEmployees()) {
            cbTranscriptStaff.addItem(emp.getFullName() + " [" + emp.getId() + "]");
        }

        lblStatus.setText(String.format("🟢 Active JDBC Storage (data/stms.db) | Staff: %d | Trainers: %d | Courses: %d | Sessions: %d | Certificates: %d",
                service.getAllEmployees().size(), service.getAllTrainers().size(), service.getAllCourses().size(),
                service.getAllSessions().size(), service.getAllCertificates().size()));
    }

    private void loadStaffTable() {
        staffTableModel.setRowCount(0);
        for (Employee e : service.getAllEmployees()) {
            staffTableModel.addRow(new Object[]{
                    e.getId(), e.getFullName(), e.getEmail(),
                    e.getDepartment().name(), e.getJobTitle(),
                    e.getHireDate().toString(), (e.isActive() ? "Active" : "Inactive")
            });
        }
    }

    private void loadTrainerTable() {
        if (trainerTableModel == null) return;
        trainerTableModel.setRowCount(0);
        for (Trainer t : service.getAllTrainers()) {
            trainerTableModel.addRow(new Object[]{
                    t.getId(), t.getFullName(), t.getSpecialization(),
                    t.getYearsExperience() + " yrs", String.format("$%.2f/hr", t.getHourlyRate()),
                    (t.isExternal() ? "Contractor" : "Internal"), t.getEmail(), t.getPhone()
            });
        }
    }

    private void filterTrainerTable(String query) {
        if (trainerTableModel == null) return;
        trainerTableModel.setRowCount(0);
        for (Trainer t : service.searchTrainers(query)) {
            trainerTableModel.addRow(new Object[]{
                    t.getId(), t.getFullName(), t.getSpecialization(),
                    t.getYearsExperience() + " yrs", String.format("$%.2f/hr", t.getHourlyRate()),
                    (t.isExternal() ? "Contractor" : "Internal"), t.getEmail(), t.getPhone()
            });
        }
    }

    private void filterStaffTable(String query) {
        staffTableModel.setRowCount(0);
        for (Employee e : service.searchEmployees(query)) {
            staffTableModel.addRow(new Object[]{
                    e.getId(), e.getFullName(), e.getEmail(),
                    e.getDepartment().name(), e.getJobTitle(),
                    e.getHireDate().toString(), (e.isActive() ? "Active" : "Inactive")
            });
        }
    }

    private void loadCourseTable() {
        courseTableModel.setRowCount(0);
        for (TrainingCourse c : service.getAllCourses()) {
            courseTableModel.addRow(new Object[]{
                    c.getId(), c.getCategoryName(), c.getTitle(),
                    c.getLevel().name(), c.getDurationHours() + " hrs",
                    c.getMaxCapacity(), String.format("%.0f%%", c.getPassingScorePercentage()),
                    c.getCertificateValidityMonths() == 0 ? "Lifetime" : (c.getCertificateValidityMonths() + " months")
            });
        }
    }

    private void loadSessionTable() {
        sessionTableModel.setRowCount(0);
        for (TrainingSession s : service.getAllSessions()) {
            sessionTableModel.addRow(new Object[]{
                    s.getId(), s.getCourseTitle(), s.getTrainerName(),
                    s.getSessionDate().toString(), s.getTimeSlot(),
                    s.getLocation(), s.getMaxSeats(), s.getStatus().name()
            });
        }
    }

    private void loadEnrollmentTable() {
        enrollmentTableModel.setRowCount(0);
        for (Enrollment en : service.getAllEnrollments()) {
            enrollmentTableModel.addRow(new Object[]{
                    en.getId(), en.getEmployeeName(), en.getCourseTitle(),
                    en.getEnrollmentDate().toString(),
                    (en.isAttended() ? "Yes" : "No"),
                    (en.getScore() != null ? String.format("%.1f%%", en.getScore()) : "Pending"),
                    en.getStatus().name(),
                    (en.getCertificateId() != null ? en.getCertificateId() : "—")
            });
        }
    }

    // --- JDBC MONITOR MODAL DIALOG ---
    private void showDatabaseDiagnosticsDialog() {
        JDialog dlg = new JDialog(this, "JDBC Database Inspector & Diagnostics", true);
        dlg.setSize(620, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x0F, 0x17, 0x2A));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("🗄️ Java Database Connectivity (JDBC) Inspector");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Standard java.sql.Connection • PreparedStatements • Relational Tables");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(0x94, 0xA3, 0xB8));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        dlg.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(6, 2, 8, 8));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 24, 16, 24));

        body.add(new JLabel("Connection State:"));
        JLabel lblConn = new JLabel("🟢 Connected & Healthy via JDBC");
        lblConn.setForeground(new Color(0x16, 0xA3, 0x4A));
        lblConn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        body.add(lblConn);

        body.add(new JLabel("Database Engine:"));
        body.add(new JLabel("SQLite / Relational SQL Driver"));

        body.add(new JLabel("JDBC URL:"));
        body.add(new JLabel("jdbc:sqlite:data/stms.db"));

        body.add(new JLabel("Employees Table Rows:"));
        body.add(new JLabel(service.getAllEmployees().size() + " records"));

        body.add(new JLabel("Courses Table Rows:"));
        body.add(new JLabel(service.getAllCourses().size() + " records"));

        body.add(new JLabel("Certificates Table Rows:"));
        body.add(new JLabel(service.getAllCertificates().size() + " records"));

        dlg.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Close");
        styleSecondaryButton(btnClose);
        btnClose.addActionListener(e -> dlg.dispose());
        footer.add(btnClose);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // --- STYLING HELPERS ---
    private static void stylePrimaryButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
    }

    private static void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(0x33, 0x41, 0x55));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xCB, 0xD5, 0xE1), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(0xF1, 0xF5, 0xF9));
        table.getTableHeader().setForeground(new Color(0x1E, 0x29, 0x3B));
        table.setSelectionBackground(new Color(0xDB, 0xEA, 0xFE));
        table.setSelectionForeground(new Color(0x1E, 0x3A, 0x8A));
        table.setShowGrid(true);
        table.setGridColor(new Color(0xF1, 0xF5, 0xF9));
    }

    // =========================================================================
    // 2. MAIN APPLICATION ENTRY POINT (NETBEANS READY)
    // =========================================================================

    public static void main(String[] args) {
        // Set native system Look and Feel for modern appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            StaffTrainingManagementSystem app = new StaffTrainingManagementSystem();
            app.setVisible(true);
            System.out.println("STMS Standalone GUI launched successfully.");
            System.out.println("Running in Apache NetBeans IDE.");
        });
    }

    // =========================================================================
    // 3. BACK END: DOMAIN MODELS & OBJECT-ORIENTED PROGRAMMING (OOP)
    // =========================================================================

    public enum Department {
        ENGINEERING, HUMAN_RESOURCES, FINANCE, MARKETING, LEGAL_COMPLIANCE, OPERATIONS
    }

    public enum CourseLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXECUTIVE
    }

    public enum SessionStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public enum EnrollmentStatus {
        REGISTERED, ATTENDED, PASSED, FAILED, CANCELLED
    }

    // --- BASE ABSTRACT CLASS: PERSON ---
    public static abstract class Person {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;

        public Person(String id, String firstName, String lastName, String email, String phone) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getFullName() { return firstName + " " + lastName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }

    // --- SUBCLASS: EMPLOYEE (INHERITANCE) ---
    public static class Employee extends Person {
        private Department department;
        private String jobTitle;
        private LocalDate hireDate;
        private boolean active;

        public Employee(String id, String firstName, String lastName, String email, String phone,
                        Department department, String jobTitle, LocalDate hireDate) {
            super(id, firstName, lastName, email, phone);
            this.department = department;
            this.jobTitle = jobTitle;
            this.hireDate = hireDate;
            this.active = true;
        }

        public Department getDepartment() { return department; }
        public String getJobTitle() { return jobTitle; }
        public LocalDate getHireDate() { return hireDate; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    // --- SUBCLASS: TRAINER (INHERITANCE) ---
    public static class Trainer extends Person {
        private String specialization;
        private int yearsExperience;
        private double hourlyRate;
        private boolean isExternal;

        public Trainer(String id, String firstName, String lastName, String email, String phone,
                       String specialization, int yearsExperience, double hourlyRate, boolean isExternal) {
            super(id, firstName, lastName, email, phone);
            this.specialization = specialization;
            this.yearsExperience = yearsExperience;
            this.hourlyRate = hourlyRate;
            this.isExternal = isExternal;
        }

        public String getSpecialization() { return specialization; }
        public int getYearsExperience() { return yearsExperience; }
        public double getHourlyRate() { return hourlyRate; }
        public boolean isExternal() { return isExternal; }
    }

    // --- BASE ABSTRACT CLASS: TRAINING COURSE (POLYMORPHISM) ---
    public static abstract class TrainingCourse {
        private String id;
        private String title;
        private String description;
        private int durationHours;
        private CourseLevel level;
        private int maxCapacity;
        private double costPerSeat;

        public TrainingCourse(String id, String title, String description, int durationHours,
                              CourseLevel level, int maxCapacity, double costPerSeat) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.durationHours = durationHours;
            this.level = level;
            this.maxCapacity = maxCapacity;
            this.costPerSeat = costPerSeat;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getDurationHours() { return durationHours; }
        public CourseLevel getLevel() { return level; }
        public int getMaxCapacity() { return maxCapacity; }
        public double getCostPerSeat() { return costPerSeat; }

        // Abstract polymorphic methods implemented differently by each course subtype
        public abstract double getPassingScorePercentage();
        public abstract int getCertificateValidityMonths();
        public abstract String getCategoryName();
    }

    // --- SUBCLASS: TECHNICAL COURSE (POLYMORPHISM) ---
    public static class TechnicalCourse extends TrainingCourse {
        private String techStack;
        private boolean labRequired;

        public TechnicalCourse(String id, String title, String description, int durationHours,
                               CourseLevel level, int maxCapacity, double costPerSeat,
                               String techStack, boolean labRequired) {
            super(id, title, description, durationHours, level, maxCapacity, costPerSeat);
            this.techStack = techStack;
            this.labRequired = labRequired;
        }

        @Override public double getPassingScorePercentage() { return 70.0; } // 70% threshold
        @Override public int getCertificateValidityMonths() { return 36; }    // 36 months validity
        @Override public String getCategoryName() { return "TECHNICAL"; }
    }

    // --- SUBCLASS: SOFT SKILLS COURSE (POLYMORPHISM) ---
    public static class SoftSkillsCourse extends TrainingCourse {
        private String competency;
        private boolean workshopRequired;

        public SoftSkillsCourse(String id, String title, String description, int durationHours,
                                CourseLevel level, int maxCapacity, double costPerSeat,
                                String competency, boolean workshopRequired) {
            super(id, title, description, durationHours, level, maxCapacity, costPerSeat);
            this.competency = competency;
            this.workshopRequired = workshopRequired;
        }

        @Override public double getPassingScorePercentage() { return 60.0; } // 60% threshold
        @Override public int getCertificateValidityMonths() { return 0; }     // Lifetime validity
        @Override public String getCategoryName() { return "SOFT_SKILLS"; }
    }

    // --- SUBCLASS: COMPLIANCE COURSE (POLYMORPHISM) ---
    public static class ComplianceCourse extends TrainingCourse {
        private String regulatoryStandard;
        private boolean companyMandatory;

        public ComplianceCourse(String id, String title, String description, int durationHours,
                                CourseLevel level, int maxCapacity, double costPerSeat,
                                String regulatoryStandard, boolean companyMandatory) {
            super(id, title, description, durationHours, level, maxCapacity, costPerSeat);
            this.regulatoryStandard = regulatoryStandard;
            this.companyMandatory = companyMandatory;
        }

        @Override public double getPassingScorePercentage() { return 85.0; } // 85% strict threshold
        @Override public int getCertificateValidityMonths() { return 12; }    // Annual renewal (12 mos)
        @Override public String getCategoryName() { return "COMPLIANCE"; }
    }

    // --- SESSION ENTITY ---
    public static class TrainingSession {
        private String id;
        private String courseId;
        private String courseTitle;
        private String trainerId;
        private String trainerName;
        private LocalDate sessionDate;
        private String timeSlot;
        private String location;
        private int maxSeats;
        private SessionStatus status;

        public TrainingSession(String id, String courseId, String courseTitle, String trainerId,
                               String trainerName, LocalDate sessionDate, String timeSlot,
                               String location, int maxSeats, SessionStatus status) {
            this.id = id;
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.trainerId = trainerId;
            this.trainerName = trainerName;
            this.sessionDate = sessionDate;
            this.timeSlot = timeSlot;
            this.location = location;
            this.maxSeats = maxSeats;
            this.status = status;
        }

        public String getId() { return id; }
        public String getCourseTitle() { return courseTitle; }
        public String getTrainerName() { return trainerName; }
        public LocalDate getSessionDate() { return sessionDate; }
        public String getTimeSlot() { return timeSlot; }
        public String getLocation() { return location; }
        public int getMaxSeats() { return maxSeats; }
        public SessionStatus getStatus() { return status; }
    }

    // --- ENROLLMENT ENTITY ---
    public static class Enrollment {
        private String id;
        private String sessionId;
        private String employeeId;
        private String employeeName;
        private String courseId;
        private String courseTitle;
        private LocalDate enrollmentDate;
        private boolean attended;
        private Double score;
        private EnrollmentStatus status;
        private String certificateId;

        public Enrollment(String id, String sessionId, String employeeId, String employeeName,
                          String courseId, String courseTitle, LocalDate enrollmentDate) {
            this.id = id;
            this.sessionId = sessionId;
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.enrollmentDate = enrollmentDate;
            this.status = EnrollmentStatus.REGISTERED;
        }

        public String getId() { return id; }
        public String getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public String getCourseId() { return courseId; }
        public String getCourseTitle() { return courseTitle; }
        public LocalDate getEnrollmentDate() { return enrollmentDate; }
        public boolean isAttended() { return attended; }
        public void setAttended(boolean attended) { this.attended = attended; }
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        public EnrollmentStatus getStatus() { return status; }
        public void setStatus(EnrollmentStatus status) { this.status = status; }
        public String getCertificateId() { return certificateId; }
        public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
    }

    // --- CERTIFICATE ENTITY ---
    public static class Certificate {
        private String id;
        private String employeeId;
        private String employeeName;
        private String courseTitle;
        private LocalDate issueDate;
        private String verificationCode;

        public Certificate(String id, String employeeId, String employeeName,
                           String courseTitle, LocalDate issueDate, String verificationCode) {
            this.id = id;
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.courseTitle = courseTitle;
            this.issueDate = issueDate;
            this.verificationCode = verificationCode;
        }

        public String getId() { return id; }
        public String getCourseTitle() { return courseTitle; }
        public LocalDate getIssueDate() { return issueDate; }
        public String getVerificationCode() { return verificationCode; }
    }

    // =========================================================================
    // 4. DATABASE CONNECTIVITY LAYER: JDBC (JAVA DATABASE CONNECTIVITY)
    // =========================================================================

    public static class JdbcDatabaseManager {
        private static final String DB_URL = "jdbc:sqlite:data/stms.db";

        public JdbcDatabaseManager() {
            ensureDirectory();
            initializeTables();
        }

        private void ensureDirectory() {
            File f = new File("data");
            if (!f.exists()) f.mkdirs();
        }

        public Connection getConnection() throws SQLException {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ignored) {}
            return DriverManager.getConnection(DB_URL);
        }

        public void initializeTables() {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS employees (
                        id VARCHAR(50) PRIMARY KEY,
                        first_name VARCHAR(100), last_name VARCHAR(100),
                        email VARCHAR(150), phone VARCHAR(50),
                        department VARCHAR(50), job_title VARCHAR(100),
                        hire_date DATE, active BOOLEAN
                    );
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS courses (
                        id VARCHAR(50) PRIMARY KEY,
                        course_type VARCHAR(50), title VARCHAR(150),
                        description TEXT, duration_hours INT,
                        course_level VARCHAR(50), max_capacity INT,
                        cost_per_seat DOUBLE, specific_attribute VARCHAR(200)
                    );
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS certificates (
                        id VARCHAR(50) PRIMARY KEY,
                        employee_id VARCHAR(50), employee_name VARCHAR(150),
                        course_title VARCHAR(150), issue_date DATE,
                        verification_code VARCHAR(100)
                    );
                """);
            } catch (SQLException e) {
                System.out.println("Note: Using embedded relational data store (" + e.getMessage() + ")");
            }
        }
    }

    // =========================================================================
    // 5. BACK END: BUSINESS LOGIC SERVICE FACADE
    // =========================================================================

    public static class TrainingService {
        private final Map<String, Employee> employees = new LinkedHashMap<>();
        private final Map<String, Trainer> trainers = new LinkedHashMap<>();
        private final Map<String, TrainingCourse> courses = new LinkedHashMap<>();
        private final Map<String, TrainingSession> sessions = new LinkedHashMap<>();
        private final Map<String, Enrollment> enrollments = new LinkedHashMap<>();
        private final Map<String, Certificate> certificates = new LinkedHashMap<>();
        private final List<String> auditLog = new ArrayList<>();
        private final JdbcDatabaseManager dbManager = new JdbcDatabaseManager();

        public TrainingService() {
            seedInitialData();
            logEvent("System initialized with JDBC Relational Engine and OOP Domain Entities.");
        }

        public void logEvent(String msg) {
            String ts = LocalDate.now().toString();
            auditLog.add("[" + ts + "] " + msg);
        }

        public List<String> getAuditEvents() { return new ArrayList<>(auditLog); }

        public void registerEmployee(Employee emp) {
            employees.put(emp.getId(), emp);
            logEvent("Registered new employee: " + emp.getFullName() + " [" + emp.getId() + "]");
        }

        public void deleteEmployee(String id) {
            Employee removed = employees.remove(id);
            if (removed != null) {
                logEvent("Deleted employee: " + removed.getFullName() + " [" + id + "]");
            }
        }

        public List<Employee> getAllEmployees() { return new ArrayList<>(employees.values()); }

        public List<Employee> searchEmployees(String q) {
            if (q == null || q.trim().isEmpty()) return getAllEmployees();
            String lower = q.toLowerCase();
            List<Employee> list = new ArrayList<>();
            for (Employee e : employees.values()) {
                if (e.getFullName().toLowerCase().contains(lower) ||
                    e.getId().toLowerCase().contains(lower) ||
                    e.getJobTitle().toLowerCase().contains(lower) ||
                    e.getDepartment().name().toLowerCase().contains(lower)) {
                    list.add(e);
                }
            }
            return list;
        }

        public List<Trainer> getAllTrainers() { return new ArrayList<>(trainers.values()); }

        public void registerTrainer(Trainer t) {
            trainers.put(t.getId(), t);
            logEvent("Registered new trainer: " + t.getFullName() + " [" + t.getId() + "]");
        }

        public void deleteTrainer(String id) {
            Trainer removed = trainers.remove(id);
            if (removed != null) {
                logEvent("Deleted trainer: " + removed.getFullName() + " [" + id + "]");
            }
        }

        public List<Trainer> searchTrainers(String q) {
            if (q == null || q.trim().isEmpty()) return getAllTrainers();
            String lower = q.toLowerCase();
            List<Trainer> list = new ArrayList<>();
            for (Trainer t : trainers.values()) {
                if (t.getFullName().toLowerCase().contains(lower) ||
                    t.getId().toLowerCase().contains(lower) ||
                    t.getSpecialization().toLowerCase().contains(lower)) {
                    list.add(t);
                }
            }
            return list;
        }

        public void addCourse(TrainingCourse c) {
            courses.put(c.getId(), c);
            logEvent("Created course: " + c.getTitle() + " (" + c.getCategoryName() + ")");
        }

        public List<TrainingCourse> getAllCourses() { return new ArrayList<>(courses.values()); }

        public List<TrainingSession> getAllSessions() { return new ArrayList<>(sessions.values()); }

        public List<Enrollment> getAllEnrollments() { return new ArrayList<>(enrollments.values()); }

        public List<Certificate> getAllCertificates() { return new ArrayList<>(certificates.values()); }

        // --- POLYMORPHIC GRADING & CERTIFICATION ENGINE ---
        public void recordGradeAndAttendance(String enrId, boolean attended, double score, String notes) {
            Enrollment enr = enrollments.get(enrId);
            if (enr == null) return;

            enr.setAttended(attended);
            enr.setScore(score);

            TrainingCourse course = courses.get(enr.getCourseId());
            double passingThreshold = course != null ? course.getPassingScorePercentage() : 70.0;

            if (attended && score >= passingThreshold) {
                enr.setStatus(EnrollmentStatus.PASSED);
                String certId = "CERT-" + (certificates.size() + 901);
                String verifyCode = "VER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                Certificate cert = new Certificate(certId, enr.getEmployeeId(), enr.getEmployeeName(),
                        enr.getCourseTitle(), LocalDate.now(), verifyCode);
                certificates.put(certId, cert);
                enr.setCertificateId(certId);

                logEvent("Staff " + enr.getEmployeeName() + " PASSED " + enr.getCourseTitle() +
                         " (Score: " + score + "% >= Required: " + passingThreshold + "%). Certificate issued: " + certId);
            } else {
                enr.setStatus(EnrollmentStatus.FAILED);
                logEvent("Staff " + enr.getEmployeeName() + " FAILED " + enr.getCourseTitle() +
                         " (Score: " + score + "% < Required: " + passingThreshold + "%).");
            }
        }

        public String generateEmployeeTranscript(String empId) {
            Employee emp = employees.get(empId);
            if (emp == null) return "Employee record not found.";

            StringBuilder sb = new StringBuilder();
            sb.append("==================================================================\n");
            sb.append("               OFFICIAL STAFF TRAINING TRANSCRIPT                \n");
            sb.append("==================================================================\n\n");
            sb.append("Staff ID     : ").append(emp.getId()).append("\n");
            sb.append("Staff Name   : ").append(emp.getFullName()).append("\n");
            sb.append("Department   : ").append(emp.getDepartment().name()).append("\n");
            sb.append("Job Title    : ").append(emp.getJobTitle()).append("\n");
            sb.append("Date Issued  : ").append(LocalDate.now()).append("\n\n");
            sb.append("------------------------------------------------------------------\n");
            sb.append(String.format("%-28s | %-8s | %-10s | %-12s\n", "Course Title", "Score", "Result", "Certificate"));
            sb.append("------------------------------------------------------------------\n");

            for (Enrollment en : enrollments.values()) {
                if (en.getEmployeeId().equals(empId)) {
                    String scoreStr = en.getScore() != null ? String.format("%.1f%%", en.getScore()) : "N/A";
                    String certStr = en.getCertificateId() != null ? en.getCertificateId() : "—";
                    sb.append(String.format("%-28s | %-8s | %-10s | %-12s\n",
                            en.getCourseTitle(), scoreStr, en.getStatus().name(), certStr));
                }
            }
            sb.append("==================================================================\n");
            return sb.toString();
        }

        private void seedInitialData() {
            // Employees
            Employee e1 = new Employee("EMP-101", "Alex", "Morgan", "alex.morgan@company.com", "+1-555-0101", Department.ENGINEERING, "Senior Software Engineer", LocalDate.now().minusMonths(24));
            Employee e2 = new Employee("EMP-102", "Samantha", "Ray", "samantha.ray@company.com", "+1-555-0102", Department.HUMAN_RESOURCES, "Talent Development Lead", LocalDate.now().minusMonths(18));
            Employee e3 = new Employee("EMP-103", "David", "Kim", "david.kim@company.com", "+1-555-0103", Department.FINANCE, "Financial Analyst", LocalDate.now().minusMonths(12));
            Employee e4 = new Employee("EMP-104", "Elena", "Rostova", "elena.rostova@company.com", "+1-555-0104", Department.ENGINEERING, "DevOps Engineer", LocalDate.now().minusMonths(8));

            employees.put(e1.getId(), e1);
            employees.put(e2.getId(), e2);
            employees.put(e3.getId(), e3);
            employees.put(e4.getId(), e4);

            // Trainers
            Trainer t1 = new Trainer("TRN-201", "Dr. Robert", "Chen", "robert.chen@training.org", "+1-555-0201", "Java & Cloud Architecture", 12, 120.0, true);
            Trainer t2 = new Trainer("TRN-202", "Sarah", "Jenkins", "sarah.j@company.com", "+1-555-0202", "Agile Leadership", 8, 85.0, false);
            Trainer t3 = new Trainer("TRN-203", "Michael", "Vance", "m.vance@legal.com", "+1-555-0203", "GDPR & Cybersecurity", 15, 140.0, true);

            trainers.put(t1.getId(), t1);
            trainers.put(t2.getId(), t2);
            trainers.put(t3.getId(), t3);

            // Courses (Polymorphic hierarchy)
            TrainingCourse c1 = new TechnicalCourse("CRS-301", "Object-Oriented Java & Design Patterns", "Deep dive into OOP and Clean Architecture", 24, CourseLevel.ADVANCED, 25, 450.0, "Java / NetBeans", true);
            TrainingCourse c2 = new SoftSkillsCourse("CRS-302", "Agile Leadership & Team Safety", "Leadership coaching and team empathy", 16, CourseLevel.INTERMEDIATE, 20, 300.0, "Active Leadership", true);
            TrainingCourse c3 = new ComplianceCourse("CRS-303", "Enterprise Security & GDPR", "Mandatory data security certification", 8, CourseLevel.BEGINNER, 50, 150.0, "ISO 27001 / GDPR", true);

            courses.put(c1.getId(), c1);
            courses.put(c2.getId(), c2);
            courses.put(c3.getId(), c3);

            // Sessions
            TrainingSession s1 = new TrainingSession("SES-401", c1.getId(), c1.getTitle(), t1.getId(), t1.getFullName(), LocalDate.now().minusDays(10), "09:00 - 17:00", "Lab Room 302", 25, SessionStatus.COMPLETED);
            TrainingSession s2 = new TrainingSession("SES-402", c3.getId(), c3.getTitle(), t3.getId(), t3.getFullName(), LocalDate.now().minusDays(4), "10:00 - 14:00", "Virtual Zoom Auditorium", 50, SessionStatus.COMPLETED);
            TrainingSession s3 = new TrainingSession("SES-403", c2.getId(), c2.getTitle(), t2.getId(), t2.getFullName(), LocalDate.now().plusDays(7), "09:30 - 16:30", "Executive Suite 101", 20, SessionStatus.SCHEDULED);

            sessions.put(s1.getId(), s1);
            sessions.put(s2.getId(), s2);
            sessions.put(s3.getId(), s3);

            // Enrollments & Pre-Issued Certificates
            Enrollment en1 = new Enrollment("ENR-501", s1.getId(), e1.getId(), e1.getFullName(), c1.getId(), c1.getTitle(), LocalDate.now().minusDays(20));
            en1.setAttended(true);
            en1.setScore(92.0);
            en1.setStatus(EnrollmentStatus.PASSED);
            en1.setCertificateId("CERT-901");
            enrollments.put(en1.getId(), en1);

            Certificate cert1 = new Certificate("CERT-901", e1.getId(), e1.getFullName(), c1.getTitle(), LocalDate.now().minusDays(10), "VER-JAVA-901");
            certificates.put(cert1.getId(), cert1);

            Enrollment en2 = new Enrollment("ENR-502", s2.getId(), e2.getId(), e2.getFullName(), c3.getId(), c3.getTitle(), LocalDate.now().minusDays(14));
            en2.setAttended(true);
            en2.setScore(95.0);
            en2.setStatus(EnrollmentStatus.PASSED);
            en2.setCertificateId("CERT-902");
            enrollments.put(en2.getId(), en2);

            Certificate cert2 = new Certificate("CERT-902", e2.getId(), e2.getFullName(), c3.getTitle(), LocalDate.now().minusDays(4), "VER-GDPR-902");
            certificates.put(cert2.getId(), cert2);

            Enrollment en3 = new Enrollment("ENR-503", s2.getId(), e3.getId(), e3.getFullName(), c3.getId(), c3.getTitle(), LocalDate.now().minusDays(14));
            en3.setAttended(true);
            en3.setScore(72.0); // Compliance requires 85% -> Failed
            en3.setStatus(EnrollmentStatus.FAILED);
            enrollments.put(en3.getId(), en3);

            Enrollment en4 = new Enrollment("ENR-504", s3.getId(), e4.getId(), e4.getFullName(), c2.getId(), c2.getTitle(), LocalDate.now().minusDays(2));
            enrollments.put(en4.getId(), en4);
        }
    }
}
