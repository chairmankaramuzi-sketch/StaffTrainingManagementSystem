package com.stms.ui;

import com.stms.model.Employee;
import com.stms.model.Enrollment;
import com.stms.model.EnrollmentStatus;
import com.stms.model.SessionStatus;
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
 * Demonstrates business workflow, polymorphic grade threshold evaluation, and certificate generation.
 */
public class EnrollmentGradingPanel extends JPanel {
    private final TrainingManagerService service;
    private final MainApplicationFrame mainFrame;

    private JTable enrollmentTable;
    private DefaultTableModel tableModel;
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

        JLabel lblStatus = new JLabel("Status Filter:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        cbStatusFilter = UITheme.createComboBox(new String[]{"All Statuses", "Registered", "Attended", "Passed & Certified", "Needs Retake"});
        cbStatusFilter.addActionListener(e -> loadEnrollmentsTable());

        filterPanel.add(lblStatus);
        filterPanel.add(cbStatusFilter);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnEnroll = UITheme.createButton("➕ Enroll Staff", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnEnroll.addActionListener(e -> showEnrollStaffDialog());

        JButton btnGrade = UITheme.createButton("📝 Record Attendance & Score", UITheme.SUCCESS, UITheme.TEXT_LIGHT);
        btnGrade.addActionListener(e -> showGradeDialog());

        actionPanel.add(btnEnroll);
        actionPanel.add(btnGrade);

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
        String statusFilter = (String) cbStatusFilter.getSelectedItem();

        List<Enrollment> list = service.getAllEnrollments();
        for (Enrollment en : list) {
            if (statusFilter != null && !statusFilter.equals("All Statuses")) {
                if (!en.getStatus().getLabel().equalsIgnoreCase(statusFilter)) {
                    continue;
                }
            }
            tableModel.addRow(new Object[]{
                    en.getId(),
                    en.getEmployeeName(),
                    en.getEmployeeId(),
                    en.getSessionId(),
                    en.getCourseTitle(),
                    en.isAttended() ? "YES" : "NO",
                    (en.getScore() != null ? String.format("%.1f%%", en.getScore()) : "-"),
                    en.getStatus().getLabel(),
                    (en.getCertificateId() != null ? en.getCertificateId() : "-")
            });
        }
    }

    private void showEnrollStaffDialog() {
        List<Employee> employees = service.getAllEmployees();
        List<TrainingSession> openSessions = service.getAllSessions().stream()
                .filter(s -> s.getStatus() == SessionStatus.SCHEDULED || s.getStatus() == SessionStatus.IN_PROGRESS)
                .toList();

        if (employees.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employees found. Please register an employee first.", "Prerequisite Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (openSessions.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "There are no active/open sessions available for enrollment.\n" +
                "All current sessions are completed.\n\n" +
                "To enroll staff, please go to the 'Training Sessions' tab and click '➕ Schedule New Session'.", 
                "No Open Sessions", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Enroll Staff in Training Session", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(540, 270);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 15));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

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
                JOptionPane.showMessageDialog(dialog, "Staff " + selectedEmp.getFullName() + " successfully enrolled in " + selectedSess.getCourseTitle() + "!", "Enrollment Successful", JOptionPane.INFORMATION_MESSAGE);

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
            JOptionPane.showMessageDialog(this, "Please select an enrollment record to grade.", "Selection Required", JOptionPane.WARNING_MESSAGE);
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

        JDialog dialog = new JDialog(mainFrame, "Record Evaluation: " + staffName, true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JCheckBox chkAttended = new JCheckBox("Attended Training Session", current.isAttended());
        JTextField tfScore = UITheme.createTextField(10);
        tfScore.setText(current.getScore() != null ? current.getScore().toString() : "85.0");
        JTextField tfNotes = UITheme.createTextField(15);
        tfNotes.setText(current.getNotes() != null ? current.getNotes() : "");

        form.add(new JLabel("Course:"));
        form.add(new JLabel("<html><b>" + courseTitle + "</b></html>"));
        form.add(new JLabel("Attendance:"));
        form.add(chkAttended);
        form.add(new JLabel("Assessment Score (0 - 100%):"));
        form.add(tfScore);
        form.add(new JLabel("Evaluator Notes:"));
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
}
