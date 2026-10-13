package com.stms.ui;

import com.stms.service.TrainingManagerService;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Main application window for the Staff Training Management System.
 */
public class MainApplicationFrame extends JFrame {
    private final TrainingManagerService service;

    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private StaffManagementPanel staffPanel;
    private CourseCatalogPanel coursePanel;
    private SessionSchedulePanel sessionPanel;
    private EnrollmentGradingPanel enrollmentPanel;
    private ReportsPanel reportsPanel;

    private JLabel lblStatus;

    public MainApplicationFrame(TrainingManagerService service) {
        this.service = service;

        setTitle("Staff Training Management System (STMS) - NetBeans Edition");
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

        JLabel lblBadge = new JLabel("  👤 Role: HR Administrator  ");
        lblBadge.setOpaque(true);
        lblBadge.setBackground(new Color(0x1E, 0x29, 0x3B));
        lblBadge.setForeground(new Color(0x38, 0xBD, 0xF8));
        lblBadge.setFont(UITheme.FONT_BOLD);
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x38, 0xBD, 0xF8), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        userBadgePanel.add(lblBadge);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(userBadgePanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. TABBED PANE
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        dashboardPanel = new DashboardPanel(service, this);
        staffPanel = new StaffManagementPanel(service, this);
        coursePanel = new CourseCatalogPanel(service, this);
        sessionPanel = new SessionSchedulePanel(service, this);
        enrollmentPanel = new EnrollmentGradingPanel(service, this);
        reportsPanel = new ReportsPanel(service, this);

        tabbedPane.addTab("📊 Dashboard", dashboardPanel);
        tabbedPane.addTab("👥 Staff Directory", staffPanel);
        tabbedPane.addTab("📚 Course Catalog", coursePanel);
        tabbedPane.addTab("📅 Training Sessions", sessionPanel);
        tabbedPane.addTab("📝 Enroll & Grade", enrollmentPanel);
        tabbedPane.addTab("📄 Reports & Transcripts", reportsPanel);

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

        JLabel lblEnv = new JLabel("Apache NetBeans / Java SE 17+ Ready | OOP Architecture");
        lblEnv.setFont(UITheme.FONT_SMALL);
        lblEnv.setForeground(UITheme.TEXT_MUTED);

        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblEnv, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }

    public void updateStatusBar() {
        lblStatus.setText(String.format("🟢 Storage: Active CSV File Persistence | Staff: %d | Courses: %d | Sessions: %d | Certificates: %d",
                service.getTotalStaffCount(), service.getTotalCoursesCount(),
                service.getTotalSessionsCount(), service.getTotalCertificatesCount()));
    }

    public void switchToTab(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    public void showEmployeeTranscript(String employeeId) {
        reportsPanel.selectEmployee(employeeId);
        reportsPanel.generateSelectedTranscript();
        switchToTab(5); // Go to Reports tab
    }

    public void refreshAllViews() {
        dashboardPanel.refreshData();
        staffPanel.loadStaffTable();
        coursePanel.loadCoursesTable();
        sessionPanel.loadSessionsTable();
        enrollmentPanel.loadEnrollmentsTable();
        reportsPanel.loadEmployeesDropdown();
        updateStatusBar();
    }
}
