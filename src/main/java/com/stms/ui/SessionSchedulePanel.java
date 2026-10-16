package com.stms.ui;

import com.stms.model.Enrollment;
import com.stms.model.SessionStatus;
import com.stms.model.Trainer;
import com.stms.model.TrainingCourse;
import com.stms.model.TrainingSession;
import com.stms.service.TrainingManagerService;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
 * Session Scheduling & Management Panel.
 * Supports Scheduling, Editing, Status Updates, Conflict Prevention, and Attendee Management.
 */
public class SessionSchedulePanel extends JPanel {
    private final TrainingManagerService service;
    private final MainApplicationFrame mainFrame;

    private JTable sessionTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;

    public SessionSchedulePanel(TrainingManagerService service, MainApplicationFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadSessionsTable();
    }

    private void initComponents() {
        JPanel topBar = new JPanel(new BorderLayout(0, 10));
        topBar.setOpaque(false);

        // Row 1: Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnSchedule = UITheme.createButton("➕ Schedule Session", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnSchedule.addActionListener(e -> showScheduleDialog());

        JButton btnEdit = UITheme.createButton("✏️ Edit Session", UITheme.PURPLE, UITheme.TEXT_LIGHT);
        btnEdit.addActionListener(e -> showEditSessionDialog());

        JButton btnChangeStatus = UITheme.createButton("🔄 Update Status", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnChangeStatus.addActionListener(e -> changeSessionStatus());

        JButton btnAddStaff = UITheme.createButton("➕ Add Staff to Session", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnAddStaff.addActionListener(e -> addStaffToSelectedSession());

        JButton btnAttendees = UITheme.createButton("👥 View Attendees", UITheme.SUCCESS, UITheme.TEXT_LIGHT);
        btnAttendees.addActionListener(e -> showAttendeesDialog());

        JButton btnCancel = UITheme.createButton("❌ Cancel / Delete", UITheme.DANGER, UITheme.TEXT_LIGHT);
        btnCancel.addActionListener(e -> cancelOrDeleteSession());

        if (mainFrame.isTrainerOrAdmin()) {
            actionPanel.add(btnSchedule);
            actionPanel.add(btnEdit);
            actionPanel.add(btnChangeStatus);
            actionPanel.add(btnAddStaff);
        }
        actionPanel.add(btnAttendees);
        if (mainFrame.isAdmin()) {
            actionPanel.add(btnCancel);
        }

        // Row 2: Search & Filter Toolbar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        txtSearch = UITheme.createTextField(14);
        txtSearch.addActionListener(e -> loadSessionsTable());

        JLabel lblFilter = new JLabel("Status:");
        lblFilter.setFont(UITheme.FONT_BOLD);
        cbStatusFilter = UITheme.createComboBox(new String[]{"All Statuses", "Scheduled", "In Progress", "Completed", "Cancelled"});
        cbStatusFilter.addActionListener(e -> loadSessionsTable());

        JButton btnFilter = UITheme.createButton("Filter", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnFilter.addActionListener(e -> loadSessionsTable());

        JButton btnReset = UITheme.createButton("Reset", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbStatusFilter.setSelectedIndex(0);
            loadSessionsTable();
        });

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblFilter);
        filterPanel.add(cbStatusFilter);
        filterPanel.add(btnFilter);
        filterPanel.add(btnReset);

        topBar.add(actionPanel, BorderLayout.NORTH);
        topBar.add(filterPanel, BorderLayout.SOUTH);
        add(topBar, BorderLayout.NORTH);

        String[] columns = {"Session ID", "Course Title", "Instructor / Trainer", "Date", "Time", "Location / Link", "Seats (Booked / Max)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        sessionTable = new JTable(tableModel);
        UITheme.styleTable(sessionTable);

        JScrollPane scrollPane = new JScrollPane(sessionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadSessionsTable() {
        tableModel.setRowCount(0);
        String query = txtSearch.getText().trim().toLowerCase();
        String statusFilter = (String) cbStatusFilter.getSelectedItem();

        List<TrainingSession> sessions = service.getAllSessions();
        for (TrainingSession s : sessions) {
            if (statusFilter != null && !statusFilter.equals("All Statuses")) {
                if (!s.getStatus().getLabel().equalsIgnoreCase(statusFilter) && !s.getStatus().name().equalsIgnoreCase(statusFilter)) {
                    continue;
                }
            }

            if (!query.isEmpty()) {
                boolean matchId = s.getId().toLowerCase().contains(query);
                boolean matchCourse = s.getCourseTitle().toLowerCase().contains(query);
                boolean matchTrainer = s.getTrainerName().toLowerCase().contains(query);
                boolean matchLoc = s.getLocationOrUrl().toLowerCase().contains(query);
                if (!matchId && !matchCourse && !matchTrainer && !matchLoc) {
                    continue;
                }
            }

            int enrolledCount = service.getEnrollmentsBySession(s.getId()).size();
            String seatText = enrolledCount + " / " + s.getMaxSeats();

            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getCourseTitle(),
                    s.getTrainerName(),
                    s.getSessionDate(),
                    s.getTimeSlot(),
                    s.getLocationOrUrl(),
                    seatText,
                    s.getStatus().getLabel()
            });
        }
    }

    private void showScheduleDialog() {
        List<TrainingCourse> courses = service.getAllCourses();
        List<Trainer> trainers = service.getAllTrainers();

        // Filter out inactive trainers
        List<Trainer> activeTrainers = trainers.stream().filter(Trainer::isActive).toList();

        if (courses.isEmpty() || activeTrainers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "You need at least one active course and one active trainer to schedule a session.",
                    "Prerequisite Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Schedule Training Session", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(500, 440);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JComboBox<TrainingCourse> cbCourses = UITheme.createComboBox(courses.toArray(new TrainingCourse[0]));
        JComboBox<Trainer> cbTrainers = UITheme.createComboBox(activeTrainers.toArray(new Trainer[0]));
        JTextField tfDate = UITheme.createTextField(15);
        tfDate.setText(LocalDate.now().plusDays(7).toString());
        JTextField tfTime = UITheme.createTextField(15);
        tfTime.setText("09:00 - 17:00");
        JTextField tfLocation = UITheme.createTextField(15);
        tfLocation.setText("Training Room 204");
        JTextField tfSeats = UITheme.createTextField(15);
        tfSeats.setText("20");

        form.add(new JLabel("Select Course:"));
        form.add(cbCourses);
        form.add(new JLabel("Assigned Trainer:"));
        form.add(cbTrainers);
        form.add(new JLabel("Date (YYYY-MM-DD):"));
        form.add(tfDate);
        form.add(new JLabel("Time Slot:"));
        form.add(tfTime);
        form.add(new JLabel("Location / URL:"));
        form.add(tfLocation);
        form.add(new JLabel("Max Seats Capacity:"));
        form.add(tfSeats);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createButton("Schedule Session", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        JButton btnCancel = UITheme.createButton("Cancel", UITheme.SECONDARY, UITheme.TEXT_LIGHT);

        btnSave.addActionListener(e -> {
            try {
                TrainingCourse selectedCourse = (TrainingCourse) cbCourses.getSelectedItem();
                Trainer selectedTrainer = (Trainer) cbTrainers.getSelectedItem();
                if (selectedCourse == null || selectedTrainer == null) return;

                LocalDate date = LocalDate.parse(tfDate.getText().trim());
                if (date.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(dialog, "Session date cannot be in the past.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String time = tfTime.getText().trim();
                String loc = tfLocation.getText().trim();
                int seats = Integer.parseInt(tfSeats.getText().trim());

                if (seats <= 0 || seats > selectedCourse.getMaxCapacity()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Max seats must be between 1 and course limit (" + selectedCourse.getMaxCapacity() + ").",
                            "Capacity Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                service.scheduleSession(selectedCourse.getId(), selectedTrainer.getId(), date, time, loc, seats);
                JOptionPane.showMessageDialog(dialog, "Session successfully scheduled in SQLite!", "Success", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
                loadSessionsTable();
                mainFrame.refreshAllViews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Scheduling prevented: " + ex.getMessage(), "Schedule Error / Conflict", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditSessionDialog() {
        int selected = sessionTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a session to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sessionId = (String) tableModel.getValueAt(selected, 0);
        TrainingSession session = service.findSessionById(sessionId).orElse(null);
        if (session == null) {
            JOptionPane.showMessageDialog(this, "Session not found: " + sessionId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Trainer> activeTrainers = service.getAllTrainers().stream().filter(Trainer::isActive).toList();

        JDialog dialog = new JDialog(mainFrame, "Edit Session [" + session.getId() + "]", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JComboBox<Trainer> cbTrainers = UITheme.createComboBox(activeTrainers.toArray(new Trainer[0]));
        for (int i = 0; i < activeTrainers.size(); i++) {
            if (activeTrainers.get(i).getId().equalsIgnoreCase(session.getTrainerId())) {
                cbTrainers.setSelectedIndex(i);
                break;
            }
        }

        JTextField tfDate = UITheme.createTextField(15);
        tfDate.setText(session.getSessionDate().toString());
        JTextField tfTime = UITheme.createTextField(15);
        tfTime.setText(session.getTimeSlot());
        JTextField tfLocation = UITheme.createTextField(15);
        tfLocation.setText(session.getLocationOrUrl());
        JTextField tfSeats = UITheme.createTextField(15);
        tfSeats.setText(String.valueOf(session.getMaxSeats()));
        JComboBox<SessionStatus> cbStatus = UITheme.createComboBox(SessionStatus.values());
        cbStatus.setSelectedItem(session.getStatus());

        form.add(new JLabel("Assigned Trainer:"));
        form.add(cbTrainers);
        form.add(new JLabel("Session Date (YYYY-MM-DD):"));
        form.add(tfDate);
        form.add(new JLabel("Time Slot:"));
        form.add(tfTime);
        form.add(new JLabel("Location / URL:"));
        form.add(tfLocation);
        form.add(new JLabel("Max Seats Capacity:"));
        form.add(tfSeats);
        form.add(new JLabel("Session Status:"));
        form.add(cbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createButton("Save Changes", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        JButton btnCancel = UITheme.createButton("Cancel", UITheme.SECONDARY, UITheme.TEXT_LIGHT);

        btnSave.addActionListener(e -> {
            try {
                Trainer selectedTrainer = (Trainer) cbTrainers.getSelectedItem();
                if (selectedTrainer == null) return;

                LocalDate date = LocalDate.parse(tfDate.getText().trim());
                String time = tfTime.getText().trim();
                String loc = tfLocation.getText().trim();
                int seats = Integer.parseInt(tfSeats.getText().trim());
                SessionStatus status = (SessionStatus) cbStatus.getSelectedItem();

                session.setTrainerId(selectedTrainer.getId());
                session.setTrainerName(selectedTrainer.getFullName());
                session.setSessionDate(date);
                session.setTimeSlot(time);
                session.setLocationOrUrl(loc);
                session.setMaxSeats(seats);
                session.setStatus(status);

                service.updateSession(session);
                JOptionPane.showMessageDialog(dialog, "Session updated successfully in SQLite!", "Success", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
                loadSessionsTable();
                mainFrame.refreshAllViews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Update error: " + ex.getMessage(), "Error / Conflict", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void changeSessionStatus() {
        int selected = sessionTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a session to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(selected, 0);
        service.findSessionById(id).ifPresent(s -> {
            SessionStatus[] statuses = SessionStatus.values();
            SessionStatus next = (SessionStatus) JOptionPane.showInputDialog(
                    this, "Select new status for session " + id + ":",
                    "Update Session Status", JOptionPane.QUESTION_MESSAGE, null, statuses, s.getStatus());
            if (next != null) {
                s.setStatus(next);
                service.updateSession(s);
                loadSessionsTable();
                mainFrame.refreshAllViews();
            }
        });
    }

    private void cancelOrDeleteSession() {
        int selected = sessionTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a session.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String) tableModel.getValueAt(selected, 0);
        int enrolledCount = service.getEnrollmentsBySession(id).size();

        if (enrolledCount > 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Session " + id + " has " + enrolledCount + " enrolled staff member(s).\n" +
                    "To preserve historical training records, mark this session as 'CANCELLED' instead of deleting?",
                    "Cancel Training Session", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                service.updateSessionStatus(id, SessionStatus.CANCELLED);
                loadSessionsTable();
                mainFrame.refreshAllViews();
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Permanently delete empty session " + id + "?",
                    "Confirm Delete Session", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                service.deleteSession(id);
                loadSessionsTable();
                mainFrame.refreshAllViews();
            }
        }
    }

    private void addStaffToSelectedSession() {
        int selected = sessionTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a session first to add staff.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(selected, 0);
        showQuickEnrollDialog(id, null);
    }

    private void showAttendeesDialog() {
        int selected = sessionTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a session to view attendees.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(selected, 0);
        List<Enrollment> attendees = service.getEnrollmentsBySession(id);

        JDialog dialog = new JDialog(mainFrame, "Enrolled Attendees for Session: " + id, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(580, 400);
        dialog.setLocationRelativeTo(mainFrame);

        String[] cols = {"Enrollment ID", "Staff Name", "Attended", "Score", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (Enrollment en : attendees) {
            model.addRow(new Object[]{
                    en.getId(),
                    en.getEmployeeName(),
                    en.isAttended() ? "Yes" : "No",
                    (en.getScore() != null ? String.format("%.1f%%", en.getScore()) : "N/A"),
                    en.getStatus().getLabel()
            });
        }

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        if (mainFrame != null && mainFrame.isTrainerOrAdmin()) {
            JButton btnAdd = UITheme.createButton("➕ Add Staff to Session", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
            btnAdd.addActionListener(e -> showQuickEnrollDialog(id, model));
            p.add(btnAdd);
        }

        JButton btnClose = UITheme.createButton("Close", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnClose.addActionListener(e -> dialog.dispose());
        p.add(btnClose);

        dialog.add(p, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showQuickEnrollDialog(String sessionId, DefaultTableModel attendeeModel) {
        List<com.stms.model.Employee> activeEmps = service.getAllEmployees().stream()
                .filter(com.stms.model.Employee::isActive)
                .collect(java.util.stream.Collectors.toList());

        if (activeEmps.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No active staff members found to enroll.", "No Staff Available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<com.stms.model.Employee> cbEmp = new JComboBox<>();
        for (com.stms.model.Employee emp : activeEmps) {
            cbEmp.addItem(emp);
        }

        JPanel p = new JPanel(new GridLayout(2, 1, 6, 8));
        p.add(new JLabel("Select Staff Member to add to Session [" + sessionId + "]:"));
        p.add(cbEmp);

        int opt = JOptionPane.showConfirmDialog(mainFrame, p, "Add Staff to Session", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            com.stms.model.Employee selectedEmp = (com.stms.model.Employee) cbEmp.getSelectedItem();
            if (selectedEmp == null) return;
            try {
                service.enrollStaff(selectedEmp.getId(), sessionId);
                loadSessionsTable();
                if (mainFrame != null) {
                    mainFrame.refreshAllViews();
                }
                if (attendeeModel != null) {
                    attendeeModel.setRowCount(0);
                    for (Enrollment en : service.getEnrollmentsBySession(sessionId)) {
                        attendeeModel.addRow(new Object[]{
                                en.getId(),
                                en.getEmployeeName(),
                                en.isAttended() ? "Yes" : "No",
                                (en.getScore() != null ? String.format("%.1f%%", en.getScore()) : "N/A"),
                                en.getStatus().getLabel()
                        });
                    }
                }
                JOptionPane.showMessageDialog(this, "Staff " + selectedEmp.getFullName() + " successfully enrolled in Session " + sessionId + "!",
                        "Enrollment Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to enroll: " + ex.getMessage(), "Enrollment Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
