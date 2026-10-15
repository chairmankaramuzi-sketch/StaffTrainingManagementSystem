package com.stms.ui;

import com.stms.model.Employee;
import com.stms.model.Enrollment;
import com.stms.model.EnrollmentStatus;
import com.stms.model.SessionStatus;
import com.stms.model.TrainingCourse;
import com.stms.model.TrainingSession;
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
 * Enrollment & Polymorphic Grading Panel.
 * Demonstrates business workflow, polymorphic grade threshold evaluation, certificate issuance,
 * score corrections, retake tracking, and cancellation actions.
 */
public class EnrollmentGradingPanel extends JPanel {
    private final TrainingManagerService service;
    private final MainApplicationFrame mainFrame;

    private JTable enrollmentTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;

    public EnrollmentGradingPanel(TrainingManagerService service, MainApplicationFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadEnrollmentsTable();
    }

    private void initComponents() {
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        txtSearch = UITheme.createTextField(12);
        txtSearch.addActionListener(e -> loadEnrollmentsTable());

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        cbStatusFilter = UITheme.createComboBox(new String[]{
                "All Statuses", "Registered", "Attended", "Passed & Certified", "Needs Retake", "Cancelled"
        });
        cbStatusFilter.addActionListener(e -> loadEnrollmentsTable());

        JButton btnFilter = UITheme.createButton("Filter", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnFilter.addActionListener(e -> loadEnrollmentsTable());

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblStatus);
        filterPanel.add(cbStatusFilter);
        filterPanel.add(btnFilter);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnEnroll = UITheme.createButton("➕ Enroll Staff", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnEnroll.addActionListener(e -> showEnrollStaffDialog());

        JButton btnGrade = UITheme.createButton("📝 Record / Correct Score", UITheme.SUCCESS, UITheme.TEXT_LIGHT);
        btnGrade.addActionListener(e -> showGradeDialog());

        JButton btnCancel = UITheme.createButton("❌ Cancel Enrollment", UITheme.DANGER, UITheme.TEXT_LIGHT);
        btnCancel.addActionListener(e -> cancelSelectedEnrollment());

        actionPanel.add(btnEnroll);
        actionPanel.add(btnGrade);
        actionPanel.add(btnCancel);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] columns = {"Enrollment ID", "Staff Name", "Staff ID", "Session ID", "Course Title", "Attended", "Score %", "Status", "Certificate ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        enrollmentTable = new JTable(tableModel);
        UITheme.styleTable(enrollmentTable);

        JScrollPane scrollPane = new JScrollPane(enrollmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadEnrollmentsTable() {
        tableModel.setRowCount(0);
        String query = txtSearch.getText().trim().toLowerCase();
        String statusFilter = (String) cbStatusFilter.getSelectedItem();

        List<Enrollment> list = service.getAllEnrollments();
        for (Enrollment en : list) {
            if (statusFilter != null && !statusFilter.equals("All Statuses")) {
                if (!en.getStatus().getLabel().equalsIgnoreCase(statusFilter) && !en.getStatus().name().equalsIgnoreCase(statusFilter)) {
                    continue;
                }
            }

            if (!query.isEmpty()) {
                boolean matchId = en.getId().toLowerCase().contains(query);
                boolean matchName = en.getEmployeeName().toLowerCase().contains(query);
                boolean matchEmpId = en.getEmployeeId().toLowerCase().contains(query);
                boolean matchSession = en.getSessionId().toLowerCase().contains(query);
                boolean matchCourse = en.getCourseTitle().toLowerCase().contains(query);
                if (!matchId && !matchName && !matchEmpId && !matchSession && !matchCourse) {
                    continue;
                }
            }

            String certId = en.getCertificateId() != null ? en.getCertificateId() : "—";
            String scoreStr = en.getScore() != null ? String.format("%.1f%%", en.getScore()) : "Pending";

            tableModel.addRow(new Object[]{
                    en.getId(),
                    en.getEmployeeName(),
                    en.getEmployeeId(),
                    en.getSessionId(),
                    en.getCourseTitle(),
                    en.isAttended() ? "✅ Yes" : "❌ No",
                    scoreStr,
                    en.getStatus().getLabel(),
                    certId
            });
        }
    }

    private void showEnrollStaffDialog() {
        List<Employee> employees = service.getAllEmployees().stream().filter(Employee::isActive).toList();
        List<TrainingSession> openSessions = service.getAllSessions().stream()
                .filter(s -> s.getStatus() == SessionStatus.SCHEDULED)
                .toList();

        if (employees.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No active staff members available for enrollment.", "Prerequisite Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (openSessions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No open scheduled sessions available for enrollment.", "No Open Sessions", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Enroll Staff in Training Session", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(480, 260);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 15));
        form.setBorder(BorderFactory.createEmptyBorder(25, 20, 15, 20));

        JComboBox<Employee> cbEmp = UITheme.createComboBox(employees.toArray(new Employee[0]));
        JComboBox<TrainingSession> cbSess = UITheme.createComboBox(openSessions.toArray(new TrainingSession[0]));

        form.add(new JLabel("Select Staff Member:"));
        form.add(cbEmp);
        form.add(new JLabel("Select Training Session:"));
        form.add(cbSess);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnConfirm = UITheme.createButton("Confirm Enrollment", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        JButton btnCancel = UITheme.createButton("Cancel", UITheme.SECONDARY, UITheme.TEXT_LIGHT);

        btnConfirm.addActionListener(e -> {
            try {
                Employee selectedEmp = (Employee) cbEmp.getSelectedItem();
                TrainingSession selectedSess = (TrainingSession) cbSess.getSelectedItem();

                if (selectedEmp == null || selectedSess == null) return;

                service.enrollStaff(selectedEmp.getId(), selectedSess.getId());
                JOptionPane.showMessageDialog(dialog,
                        "Staff " + selectedEmp.getFullName() + " successfully enrolled in " + selectedSess.getCourseTitle() + "!",
                        "Enrollment Successful", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
                loadEnrollmentsTable();
                mainFrame.refreshAllViews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Enrollment Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showGradeDialog() {
        int selected = enrollmentTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select an enrollment record to grade or correct.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String enrId = (String) tableModel.getValueAt(selected, 0);
        String staffName = (String) tableModel.getValueAt(selected, 1);
        String courseTitle = (String) tableModel.getValueAt(selected, 4);

        Enrollment current = service.getAllEnrollments().stream()
                .filter(e -> e.getId().equals(enrId))
                .findFirst()
                .orElse(null);

        if (current == null) return;

        TrainingCourse course = service.findCourseById(current.getCourseId()).orElse(null);
        double passingReq = course != null ? course.getPassingScorePercentage() : 70.0;
        String catName = course != null ? course.getCategoryName() : "Course";

        JDialog dialog = new JDialog(mainFrame, "Record / Correct Evaluation: " + staffName, true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JCheckBox chkAttended = new JCheckBox("Attended Training Session", current.isAttended());
        JTextField tfScore = UITheme.createTextField(10);
        tfScore.setText(current.getScore() != null ? current.getScore().toString() : "85.0");
        JTextField tfNotes = UITheme.createTextField(15);
        tfNotes.setText(current.getNotes() != null ? current.getNotes() : "");

        JLabel lblReq = new JLabel(String.format("<html><b>%.1f%%</b> (Category: %s)</html>", passingReq, catName));

        form.add(new JLabel("Course:"));
        form.add(new JLabel("<html><b>" + courseTitle + "</b></html>"));
        form.add(new JLabel("Passing Requirement:"));
        form.add(lblReq);
        form.add(new JLabel("Attendance:"));
        form.add(chkAttended);
        form.add(new JLabel("Assessment Score (0 - 100%):"));
        form.add(tfScore);
        form.add(new JLabel("Evaluator Notes / Retake:"));
        form.add(tfNotes);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnEvaluate = UITheme.createButton("Evaluate & Submit", UITheme.SUCCESS, UITheme.TEXT_LIGHT);
        JButton btnCancel = UITheme.createButton("Cancel", UITheme.SECONDARY, UITheme.TEXT_LIGHT);

        btnEvaluate.addActionListener(e -> {
            try {
                boolean attended = chkAttended.isSelected();
                Double score = Double.parseDouble(tfScore.getText().trim());
                String notes = tfNotes.getText().trim();

                if (score < 0 || score > 100) {
                    JOptionPane.showMessageDialog(dialog, "Score must be between 0 and 100.", "Invalid Score", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                service.recordAttendanceAndScore(enrId, attended, score, notes);

                Enrollment updated = service.getAllEnrollments().stream()
                        .filter(en -> en.getId().equals(enrId)).findFirst().orElse(null);

                String msg = "Evaluation completed!\nResult: " + (updated != null ? updated.getStatus().getLabel() : "");
                if (updated != null && updated.getCertificateId() != null) {
                    msg += "\n🎉 Official Certificate Issued ID: " + updated.getCertificateId();
                } else if (!attended) {
                    msg += "\n⚠️ Marked as NOT attended. No certificate issued.";
                } else if (score < passingReq) {
                    msg += "\n⚠️ Score below passing requirement (" + passingReq + "%). Marked for Retake.";
                }

                JOptionPane.showMessageDialog(dialog, msg, "Evaluation Completed", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
                loadEnrollmentsTable();
                mainFrame.refreshAllViews();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric score (e.g., 85.5).", "Number Format Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnEvaluate);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void cancelSelectedEnrollment() {
        int selected = enrollmentTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select an enrollment record to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String enrId = (String) tableModel.getValueAt(selected, 0);
        String staffName = (String) tableModel.getValueAt(selected, 1);
        String courseTitle = (String) tableModel.getValueAt(selected, 4);

        String reason = JOptionPane.showInputDialog(this,
                "Enter reason for cancelling enrollment for " + staffName + " in " + courseTitle + ":",
                "Cancel Enrollment", JOptionPane.QUESTION_MESSAGE);

        if (reason != null && !reason.trim().isEmpty()) {
            try {
                service.cancelEnrollment(enrId, reason.trim());
                JOptionPane.showMessageDialog(this, "Enrollment cancelled successfully.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadEnrollmentsTable();
                mainFrame.refreshAllViews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling enrollment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
