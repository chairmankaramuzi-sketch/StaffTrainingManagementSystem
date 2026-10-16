package com.stms.ui;

import com.stms.model.User;
import com.stms.model.UserRole;
import com.stms.service.AuthService;
import com.stms.service.TrainingManagerService;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Main application window for the Staff Training Management System.
 */
public class MainApplicationFrame extends JFrame {
    private final TrainingManagerService service;
    private final AuthService authService;
    private final User currentUser;
    private final Runnable onLogout;

    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private StaffManagementPanel staffPanel;
    private TrainerManagementPanel trainerPanel;
    private CourseCatalogPanel coursePanel;
    private SessionSchedulePanel sessionPanel;
    private EnrollmentGradingPanel enrollmentPanel;
    private ReportsPanel reportsPanel;
    private UserManagementPanel userPanel;

    /** All panels in their original tab order (index used by switchToTab). */
    private Component[] allPanels;

    private JLabel lblStatus;

    public MainApplicationFrame(TrainingManagerService service, AuthService authService, User currentUser, Runnable onLogout) {
        this.service = service;
        this.authService = authService;
        this.currentUser = currentUser;
        this.onLogout = onLogout;

        setTitle("Staff Training Management System (STMS) - " + currentUser.getFullName()
                + " [" + currentUser.getRole().getDisplayName() + "]");
        setSize(1180, 760);
        setMinimumSize(new Dimension(960, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. HEADER BAR
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BG_DARK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JPanel titleBox = new JPanel(new BorderLayout(4, 4));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("🎓 Staff Training Management System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Enterprise Object-Oriented Training, Competency & Certification Engine");
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(new Color(0x94, 0xA3, 0xB8));

        titleBox.add(lblTitle, BorderLayout.NORTH);
        titleBox.add(lblSubtitle, BorderLayout.SOUTH);

        JPanel userBadgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userBadgePanel.setOpaque(false);

        JButton btnDbStatus = new JButton("🗄️ JDBC Database (SQLite)");
        btnDbStatus.setFont(UITheme.FONT_BOLD);
        btnDbStatus.setBackground(new Color(0x0F, 0x76, 0x6E));
        btnDbStatus.setForeground(Color.WHITE);
        btnDbStatus.setFocusPainted(false);
        btnDbStatus.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDbStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2D, 0xD4, 0xBF), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnDbStatus.setToolTipText("Inspect JDBC Connection, Relational Tables, and SQL Schema");
        btnDbStatus.addActionListener(e -> new DatabaseDiagnosticsDialog(this, service).setVisible(true));

        JLabel lblBadge = new JLabel(currentUser.getFullName() + "  |  Role: " + currentUser.getRole().getDisplayName());
        lblBadge.setOpaque(true);
        lblBadge.setBackground(new Color(0x1E, 0x29, 0x3B));
        lblBadge.setForeground(new Color(0x38, 0xBD, 0xF8));
        lblBadge.setFont(UITheme.FONT_BOLD);
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x38, 0xBD, 0xF8), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        lblBadge.setToolTipText("Signed in as " + currentUser.getUsername());

        JButton btnChangePassword = new JButton("🔑 Change Password");
        btnChangePassword.setFont(UITheme.FONT_BOLD);
        btnChangePassword.setBackground(new Color(0x33, 0x41, 0x55));
        btnChangePassword.setForeground(Color.WHITE);
        btnChangePassword.setFocusPainted(false);
        btnChangePassword.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnChangePassword.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btnChangePassword.addActionListener(e -> UserManagementPanel.showChangePasswordDialog(this, authService, currentUser));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UITheme.FONT_BOLD);
        btnLogout.setBackground(UITheme.DANGER);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btnLogout.addActionListener(e -> logout());

        // Database diagnostics is an administrator tool
        if (currentUser.getRole() == UserRole.HR_ADMINISTRATOR) {
            userBadgePanel.add(btnDbStatus);
        }
        userBadgePanel.add(lblBadge);
        userBadgePanel.add(btnChangePassword);
        userBadgePanel.add(btnLogout);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(userBadgePanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. TABBED PANE
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        dashboardPanel = new DashboardPanel(service, this);
        staffPanel = new StaffManagementPanel(service, this);
        trainerPanel = new TrainerManagementPanel(service, this);
        coursePanel = new CourseCatalogPanel(service, this);
        sessionPanel = new SessionSchedulePanel(service, this);
        enrollmentPanel = new EnrollmentGradingPanel(service, this);
        reportsPanel = new ReportsPanel(service, this);
        userPanel = new UserManagementPanel(service, authService, currentUser, this);

        allPanels = new Component[] {
            dashboardPanel, staffPanel, trainerPanel, coursePanel, sessionPanel, enrollmentPanel, reportsPanel, userPanel
        };
        String[] titles = {
            "📊 Dashboard", "👥 Staff Directory", "👨‍🏫 Trainers", "📚 Course Catalog",
            "📅 Training Sessions", "📝 Enroll & Grade", "📄 Reports & Transcripts", "🔐 User Accounts"
        };
        for (int i = 0; i < allPanels.length; i++) {
            if (canAccessTab(i)) {
                tabbedPane.addTab(titles[i], allPanels[i]);
            }
        }

        tabbedPane.addChangeListener(e -> refreshAllViews());

        add(tabbedPane, BorderLayout.CENTER);

        // 3. STATUS BAR
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(0xF1, 0xF5, 0xF9));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 20, 6, 20)
        ));

        lblStatus = new JLabel();
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.TEXT_MUTED);
        updateStatusBar();

        JLabel lblEnv = new JLabel("Apache NetBeans | Java SE 17+ | Java Swing GUI | JDBC Relational DB");
        lblEnv.setFont(UITheme.FONT_SMALL);
        lblEnv.setForeground(UITheme.TEXT_MUTED);

        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblEnv, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }

    public void updateStatusBar() {
        lblStatus.setText(String.format("🟢 Storage: JDBC SQLite Active (data/stms.db) | Staff: %d | Trainers: %d | Courses: %d | Sessions: %d | Certificates: %d",
                service.getTotalStaffCount(), service.getAllTrainers().size(),
                service.getTotalCoursesCount(), service.getTotalSessionsCount(), service.getTotalCertificatesCount()));
    }

    /**
     * Switches to a tab using its original index (0=Dashboard ... 6=Reports).
     * Does nothing if the current user's role cannot access that tab.
     */
    public void switchToTab(int index) {
        if (index >= 0 && index < allPanels.length) {
            int pos = tabbedPane.indexOfComponent(allPanels[index]);
            if (pos >= 0) {
                tabbedPane.setSelectedIndex(pos);
            }
        }
    }

    /**
     * Role-based access: which tabs (by original index) each role may see.
     */
    private boolean canAccessTab(int index) {
        switch (currentUser.getRole()) {
            case HR_ADMINISTRATOR:
                return true;
            case TRAINER:
                // Dashboard, Trainers, Course Catalog, Sessions, Enroll & Grade, Reports
                return index != 1 && index != 7;
            case EMPLOYEE:
                // Dashboard, Course Catalog, Reports
                return index == 0 || index == 3 || index == 6;
            default:
                return false;
        }
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.HR_ADMINISTRATOR;
    }

    public boolean isTrainerOrAdmin() {
        if (currentUser == null) return false;
        return currentUser.getRole() == UserRole.HR_ADMINISTRATOR || currentUser.getRole() == UserRole.TRAINER;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public AuthService getAuthService() {
        return authService;
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            if (onLogout != null) {
                onLogout.run();
            }
        }
    }

    public void showEmployeeTranscript(String employeeId) {
        reportsPanel.selectEmployee(employeeId);
        reportsPanel.generateSelectedTranscript();
        switchToTab(6); // Go to Reports tab (index 6)
    }

    public void showAddStaffDialog() {
        if (staffPanel != null) {
            staffPanel.showAddStaffDialog();
        }
    }

    public StaffManagementPanel getStaffPanel() {
        return staffPanel;
    }

    public void refreshAllViews() {
        dashboardPanel.refreshData();
        staffPanel.loadStaffTable();
        trainerPanel.loadTrainersTable();
        coursePanel.loadCoursesTable();
        sessionPanel.loadSessionsTable();
        enrollmentPanel.loadEnrollmentsTable();
        reportsPanel.loadEmployeesDropdown();
        if (userPanel != null) {
            userPanel.loadUsers();
        }
        updateStatusBar();
    }
}
