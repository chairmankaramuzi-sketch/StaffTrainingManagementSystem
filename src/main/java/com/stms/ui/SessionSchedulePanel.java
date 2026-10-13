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
 */
public class SessionSchedulePanel extends JPanel {
    private final TrainingManagerService service;
    private final MainApplicationFrame mainFrame;

    private JTable sessionTable;
    private DefaultTableModel tableModel;

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
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("📅 Active Training Schedules & Sessions");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnSchedule = UITheme.createButton("➕ Schedule New Session", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnSchedule.addActionListener(e -> showScheduleDialog());

        JButton btnChangeStatus = UITheme.createButton("🔄 Update Status", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnChangeStatus.addActionListener(e -> changeSessionStatus());

        JButton btnAttendees = UITheme.createButton("👥 View Attendees", UITheme.PURPLE, UITheme.TEXT_LIGHT);
        btnAttendees.addActionListener(e -> showAttendeesDialog());

        actionPanel.add(btnSchedule);
        actionPanel.add(btnChangeStatus);
        actionPanel.add(btnAttendees);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
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
        List<TrainingSession> sessions = service.getAllSessions();
        for (TrainingSession s : sessions) {
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

        if (courses.isEmpty() || trainers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You need at least one course and one trainer to schedule a session.", "Prerequisite Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Schedule Training Session", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JComboBox<TrainingCourse> cbCourses = UITheme.createComboBox(courses.toArray(new TrainingCourse[0]));
        JComboBox<Trainer> cbTrainers = UITheme.createComboBox(trainers.toArray(new Trainer[0]));
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
                LocalDate date = LocalDate.parse(tfDate.getText().trim());
                String time = tfTime.getText().trim();
                String loc = tfLocation.getText().trim();
                int seats = Integer.parseInt(tfSeats.getText().trim());

                if (selectedCourse == null || selectedTrainer == null) return;

                service.scheduleSession(selectedCourse.getId(), selectedTrainer.getId(), date, time, loc, seats);

                dialog.dispose();
                loadSessionsTable();
                mainFrame.refreshAllViews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
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
        dialog.setSize(520, 360);
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

        JButton btnClose = UITheme.createButton("Close", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(btnClose);
        dialog.add(p, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
