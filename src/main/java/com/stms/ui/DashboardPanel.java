package com.stms.ui;

import com.stms.service.TrainingManagerService;
import com.stms.ui.components.ModernCard;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Overview Dashboard Panel presenting key performance indicators and system logs.
 */
public class DashboardPanel extends JPanel {
    private final TrainingManagerService service;
    private final MainApplicationFrame mainFrame;

    private ModernCard cardStaff;
    private ModernCard cardCourses;
    private ModernCard cardSessions;
    private ModernCard cardEnrollments;
    private ModernCard cardPassRate;
    private ModernCard cardCertificates;

    private JTextArea txtLogs;

    public DashboardPanel(TrainingManagerService service, MainApplicationFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // TOP: Stats Cards Grid
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 16, 16));
        cardsPanel.setOpaque(false);

        cardStaff = new ModernCard("Total Staff", "0", "Across 7 Departments", UITheme.PRIMARY);
        cardCourses = new ModernCard("Training Courses", "0", "Technical & Compliance", UITheme.PURPLE);
        cardSessions = new ModernCard("Training Sessions", "0", "Scheduled & Active", UITheme.SUCCESS);
        cardEnrollments = new ModernCard("Total Enrollments", "0", "Staff Participations", UITheme.WARNING);
        cardPassRate = new ModernCard("Overall Pass Rate", "0%", "Polymorphic Benchmark", UITheme.SUCCESS);
        cardCertificates = new ModernCard("Issued Certificates", "0", "Official Qualifications", UITheme.PRIMARY);

        cardsPanel.add(cardStaff);
        cardsPanel.add(cardCourses);
        cardsPanel.add(cardSessions);
        cardsPanel.add(cardEnrollments);
        cardsPanel.add(cardPassRate);
        cardsPanel.add(cardCertificates);

        add(cardsPanel, BorderLayout.NORTH);

        // CENTER: Live Audit & Activity Logs
        JPanel logsPanel = new JPanel(new BorderLayout(8, 8));
        logsPanel.setOpaque(false);
        logsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                " Live Activity & Audit Stream ",
                0, 0, UITheme.FONT_SUBTITLE, UITheme.TEXT_PRIMARY
        ));

        txtLogs = new JTextArea();
        txtLogs.setFont(UITheme.FONT_MONO);
        txtLogs.setEditable(false);
        txtLogs.setBackground(UITheme.BG_SURFACE);
        txtLogs.setForeground(UITheme.TEXT_PRIMARY);
        txtLogs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollLogs = new JScrollPane(txtLogs);
        scrollLogs.setBorder(BorderFactory.createEmptyBorder());
        logsPanel.add(scrollLogs, BorderLayout.CENTER);

        // Quick Actions Toolbar at bottom
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsBar.setOpaque(false);

        JButton btnRefresh = UITheme.createButton("🔄 Refresh Dashboard", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnRefresh.addActionListener(e -> refreshData());

        JButton btnClearLogs = UITheme.createButton("🧹 Clear Stream", UITheme.SECONDARY, UITheme.TEXT_PRIMARY);
        btnClearLogs.addActionListener(e -> {
            service.clearAuditLogs();
            refreshData();
        });

        JButton btnGoEnroll = UITheme.createButton("📝 Quick Enroll Staff", UITheme.SUCCESS, UITheme.TEXT_LIGHT);
        btnGoEnroll.addActionListener(e -> mainFrame.switchToTab(5)); // tab 5: Enrollments

        JButton btnGoReports = UITheme.createButton("📄 View Transcripts", UITheme.PURPLE, UITheme.TEXT_LIGHT);
        btnGoReports.addActionListener(e -> mainFrame.switchToTab(6)); // tab 6: Reports

        actionsBar.add(btnRefresh);
        if (mainFrame.isAdmin()) {
            actionsBar.add(btnClearLogs);
        }
        if (mainFrame.isTrainerOrAdmin()) {
            actionsBar.add(btnGoEnroll);
        }
        actionsBar.add(btnGoReports);

        logsPanel.add(actionsBar, BorderLayout.SOUTH);

        add(logsPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        cardStaff.setValue(String.valueOf(service.getTotalStaffCount()));
        cardCourses.setValue(String.valueOf(service.getTotalCoursesCount()));
        cardSessions.setValue(String.valueOf(service.getTotalSessionsCount()));
        cardEnrollments.setValue(String.valueOf(service.getTotalEnrollmentsCount()));
        cardPassRate.setValue(String.format("%.1f%%", service.calculateOverallPassRate()));
        cardCertificates.setValue(String.valueOf(service.getTotalCertificatesCount()));

        List<String> logs = service.getRecentAuditLogs(30);
        if (logs == null || logs.isEmpty()) {
            logs = service.getNotificationService().getRecentLogs(30);
        }
        StringBuilder sb = new StringBuilder();
        for (String log : logs) {
            sb.append(log).append("\n");
        }
        txtLogs.setText(sb.toString());
        txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
    }
}
