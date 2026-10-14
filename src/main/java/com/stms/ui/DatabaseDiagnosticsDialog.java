package com.stms.ui;

import com.stms.database.DatabaseManager;
import com.stms.service.TrainingManagerService;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Modern JDBC Database Diagnostics and Connection Inspector Dialog.
 * Demonstrates JDBC connectivity, live SQL querying, and database schema status for coursework evaluation.
 */
public class DatabaseDiagnosticsDialog extends JDialog {
    private final TrainingManagerService service;
    private final DatabaseManager dbManager;

    private JLabel lblStatusBadge;
    private JLabel lblDriver;
    private JLabel lblUrl;
    private JLabel lblEmpCount;
    private JLabel lblTrnCount;
    private JLabel lblCrsCount;
    private JLabel lblSessCount;
    private JLabel lblEnrCount;
    private JLabel lblCertCount;

    public DatabaseDiagnosticsDialog(Frame parent, TrainingManagerService service) {
        super(parent, "JDBC Database Inspector & Diagnostics", true);
        this.service = service;
        this.dbManager = DatabaseManager.getInstance();

        setSize(650, 520);
        setMinimumSize(new Dimension(580, 460));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        refreshStatus();
    }

    private void initComponents() {
        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BG_DARK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("🗄️ JDBC Database Connectivity Monitor");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Java Database Connectivity (JDBC) Architecture & Live Table Inspection");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(new Color(0x94, 0xA3, 0xB8));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Body Panel
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Connection Status
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblStatusTitle = new JLabel("Connection Status:");
        lblStatusTitle.setFont(UITheme.FONT_BOLD);
        bodyPanel.add(lblStatusTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        lblStatusBadge = new JLabel("🟢 Connected & Healthy");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatusBadge.setForeground(new Color(0x16, 0xA3, 0x4A));
        bodyPanel.add(lblStatusBadge, gbc);

        // Database Engine
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblEngTitle = new JLabel("Database Engine:");
        lblEngTitle.setFont(UITheme.FONT_BOLD);
        bodyPanel.add(lblEngTitle, gbc);

        gbc.gridx = 1;
        lblDriver = new JLabel(dbManager.getDatabaseEngine());
        lblDriver.setFont(UITheme.FONT_REGULAR);
        bodyPanel.add(lblDriver, gbc);

        // JDBC URL
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblUrlTitle = new JLabel("JDBC Connection URL:");
        lblUrlTitle.setFont(UITheme.FONT_BOLD);
        bodyPanel.add(lblUrlTitle, gbc);

        gbc.gridx = 1;
        lblUrl = new JLabel(dbManager.getDatabaseUrl());
        lblUrl.setFont(new Font("Consolas", Font.PLAIN, 12));
        lblUrl.setForeground(UITheme.PRIMARY);
        bodyPanel.add(lblUrl, gbc);

        // Separator Header: Database Tables & Records
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 6, 8, 6);
        JLabel lblTablesHeading = new JLabel("📊 Relational Database Tables & Live Row Counts");
        lblTablesHeading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTablesHeading.setForeground(UITheme.BG_DARK);
        bodyPanel.add(lblTablesHeading, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Table 1: Employees
        gbc.gridx = 0; gbc.gridy = 4;
        bodyPanel.add(new JLabel("• employees table:"), gbc);
        gbc.gridx = 1;
        lblEmpCount = new JLabel();
        lblEmpCount.setFont(UITheme.FONT_BOLD);
        bodyPanel.add(lblEmpCount, gbc);

        // Table 2: Trainers
        gbc.gridx = 0; gbc.gridy = 5;
        bodyPanel.add(new JLabel("• trainers table:"), gbc);
        gbc.gridx = 1;
        lblTrnCount = new JLabel();
        lblTrnCount.setFont(UITheme.FONT_BOLD);
        bodyPanel.add(lblTrnCount, gbc);

        // Table 3: Courses
        gbc.gridx = 0; gbc.gridy = 6;
        bodyPanel.add(new JLabel("• courses table:"), gbc);
        gbc.gridx = 1;
        lblCrsCount = new JLabel();
        lblCrsCount.setFont(UITheme.FONT_BOLD);
        bodyPanel.add(lblCrsCount, gbc);

        // Table 4: Sessions
        gbc.gridx = 0; gbc.gridy = 7;
        bodyPanel.add(new JLabel("• sessions table:"), gbc);
        gbc.gridx = 1;
        lblSessCount = new JLabel();
        lblSessCount.setFont(UITheme.FONT_BOLD);
        bodyPanel.add(lblSessCount, gbc);

        // Table 5: Enrollments
        gbc.gridx = 0; gbc.gridy = 8;
        bodyPanel.add(new JLabel("• enrollments table:"), gbc);
        gbc.gridx = 1;
        lblEnrCount = new JLabel();
        lblEnrCount.setFont(UITheme.FONT_BOLD);
        bodyPanel.add(lblEnrCount, gbc);

        // Table 6: Certificates
        gbc.gridx = 0; gbc.gridy = 9;
        bodyPanel.add(new JLabel("• certificates table:"), gbc);
        gbc.gridx = 1;
        lblCertCount = new JLabel();
        lblCertCount.setFont(UITheme.FONT_BOLD);
        bodyPanel.add(lblCertCount, gbc);

        add(bodyPanel, BorderLayout.CENTER);

        // 3. Footer Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footerPanel.setBackground(new Color(0xF8, 0xFA, 0xFC));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton btnTestConn = new JButton("⚡ Test JDBC Query");
        btnTestConn.setFont(UITheme.FONT_BOLD);
        btnTestConn.addActionListener(e -> testJdbcQuery());

        JButton btnViewSchema = new JButton("📄 View SQL Schema");
        btnViewSchema.setFont(UITheme.FONT_BOLD);
        btnViewSchema.addActionListener(e -> showSchemaDialog());

        JButton btnSync = new JButton("🔄 Re-sync All to DB");
        btnSync.setFont(UITheme.FONT_BOLD);
        btnSync.addActionListener(e -> {
            service.syncToStorage();
            refreshStatus();
            JOptionPane.showMessageDialog(this,
                "All in-memory entities have been successfully re-persisted via JDBC PreparedStatements!",
                "Database Synchronized", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnClose = new JButton("Close");
        btnClose.setFont(UITheme.FONT_BOLD);
        btnClose.addActionListener(e -> dispose());

        footerPanel.add(btnTestConn);
        footerPanel.add(btnViewSchema);
        footerPanel.add(btnSync);
        footerPanel.add(btnClose);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void refreshStatus() {
        boolean healthy = dbManager.testConnection();
        if (healthy) {
            lblStatusBadge.setText("🟢 Connected & Healthy (JDBC Active)");
            lblStatusBadge.setForeground(new Color(0x16, 0xA3, 0x4A));
        } else {
            lblStatusBadge.setText("🔴 Connection Failed");
            lblStatusBadge.setForeground(UITheme.DANGER);
        }

        lblDriver.setText(dbManager.getDatabaseEngine());
        lblUrl.setText(dbManager.getDatabaseUrl());

        lblEmpCount.setText(dbManager.getTableRowCount("employees") + " records");
        lblTrnCount.setText(dbManager.getTableRowCount("trainers") + " records");
        lblCrsCount.setText(dbManager.getTableRowCount("courses") + " records");
        lblSessCount.setText(dbManager.getTableRowCount("sessions") + " records");
        lblEnrCount.setText(dbManager.getTableRowCount("enrollments") + " records");
        lblCertCount.setText(dbManager.getTableRowCount("certificates") + " records");
    }

    private void testJdbcQuery() {
        long startTime = System.nanoTime();
        boolean success = dbManager.testConnection();
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

        if (success) {
            JOptionPane.showMessageDialog(this,
                "JDBC Query: 'SELECT 1' executed successfully.\nResponse time: " + elapsedMs + " ms\nDatabase URL: " + dbManager.getDatabaseUrl(),
                "JDBC Connection Test Passed",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "JDBC Connection Test Failed! Please check database configuration and JDBC driver.",
                "Connection Failed",
                JOptionPane.ERROR_MESSAGE);
        }
        refreshStatus();
    }

    private void showSchemaDialog() {
        JDialog schemaDialog = new JDialog(this, "SQL DDL Schema Definition", true);
        schemaDialog.setSize(600, 480);
        schemaDialog.setLocationRelativeTo(this);
        schemaDialog.setLayout(new BorderLayout());

        JTextArea txtSchema = new JTextArea();
        txtSchema.setEditable(false);
        txtSchema.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtSchema.setBackground(new Color(0x0F, 0x17, 0x2A));
        txtSchema.setForeground(new Color(0x38, 0xBD, 0xF8));
        txtSchema.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String schemaText = """
        -- ==========================================================
        -- STMS Relational Schema (ANSI SQL / SQLite / MySQL)
        -- ==========================================================
        
        1. TABLE employees (
            id VARCHAR(50) PRIMARY KEY,
            first_name VARCHAR(100), last_name VARCHAR(100),
            email VARCHAR(150), phone VARCHAR(50),
            department VARCHAR(50), job_title VARCHAR(100),
            hire_date DATE, active BOOLEAN
        );

        2. TABLE trainers (
            id VARCHAR(50) PRIMARY KEY,
            first_name VARCHAR(100), last_name VARCHAR(100),
            email VARCHAR(150), phone VARCHAR(50),
            specialization VARCHAR(150), years_of_experience INT,
            hourly_rate DOUBLE, is_external BOOLEAN
        );

        3. TABLE courses (
            id VARCHAR(50) PRIMARY KEY,
            course_type VARCHAR(50), title VARCHAR(150),
            description TEXT, duration_hours INT,
            course_level VARCHAR(50), max_capacity INT,
            cost_per_seat DOUBLE, specific_attribute VARCHAR(200),
            flag_attribute BOOLEAN
        );

        4. TABLE sessions (
            id VARCHAR(50) PRIMARY KEY,
            course_id VARCHAR(50) REFERENCES courses(id),
            course_title VARCHAR(150),
            trainer_id VARCHAR(50) REFERENCES trainers(id),
            trainer_name VARCHAR(150), session_date DATE,
            time_slot VARCHAR(50), location VARCHAR(150),
            max_seats INT, status VARCHAR(50)
        );

        5. TABLE enrollments (
            id VARCHAR(50) PRIMARY KEY,
            session_id VARCHAR(50) REFERENCES sessions(id),
            employee_id VARCHAR(50) REFERENCES employees(id),
            employee_name VARCHAR(150), course_id VARCHAR(50),
            course_title VARCHAR(150), enrollment_date DATE,
            attended BOOLEAN, score DOUBLE, status VARCHAR(50),
            certificate_id VARCHAR(50), notes TEXT
        );

        6. TABLE certificates (
            id VARCHAR(50) PRIMARY KEY,
            enrollment_id VARCHAR(50) REFERENCES enrollments(id),
            employee_id VARCHAR(50) REFERENCES employees(id),
            employee_name VARCHAR(150), course_id VARCHAR(50),
            course_title VARCHAR(150), course_category VARCHAR(100),
            issue_date DATE, expiry_date DATE, verification_code VARCHAR(100)
        );
        """;
        txtSchema.setText(schemaText);
        txtSchema.setCaretPosition(0);

        schemaDialog.add(new JScrollPane(txtSchema), BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.setFont(UITheme.FONT_BOLD);
        btnClose.addActionListener(e -> schemaDialog.dispose());

        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnl.add(btnClose);
        schemaDialog.add(pnl, BorderLayout.SOUTH);

        schemaDialog.setVisible(true);
    }
}
