package com.stms.ui;

import com.stms.model.Trainer;
import com.stms.service.TrainingManagerService;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Trainer Management Panel.
 * Provides complete CRUD operations, specialization tracking, and status toggles.
 */
public class TrainerManagementPanel extends JPanel {
    private final TrainingManagerService service;
    private final MainApplicationFrame mainFrame;

    private JTable trainerTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JCheckBox chkActiveOnly;

    public TrainerManagementPanel(TrainingManagerService service, MainApplicationFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadTrainersTable();
    }

    private void initComponents() {
        // TOP: Filter Toolbar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Search Trainer:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        txtSearch = UITheme.createTextField(15);
        txtSearch.addActionListener(e -> loadTrainersTable());

        chkActiveOnly = new JCheckBox("Active Only");
        chkActiveOnly.setFont(UITheme.FONT_BOLD);
        chkActiveOnly.setOpaque(false);
        chkActiveOnly.addActionListener(e -> loadTrainersTable());

        JButton btnFilter = UITheme.createButton("Filter", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnFilter.addActionListener(e -> loadTrainersTable());

        JButton btnReset = UITheme.createButton("Reset", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            chkActiveOnly.setSelected(false);
            loadTrainersTable();
        });

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(chkActiveOnly);
        filterPanel.add(btnFilter);
        filterPanel.add(btnReset);

        // Buttons Panel on Right
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd = UITheme.createButton("➕ Add Trainer", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnAdd.addActionListener(e -> showAddEditTrainerDialog(null));

        JButton btnEdit = UITheme.createButton("✏️ Edit Selected", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnEdit.addActionListener(e -> editSelectedTrainer());

        JButton btnToggle = UITheme.createButton("🔄 Toggle Status", UITheme.PURPLE, UITheme.TEXT_LIGHT);
        btnToggle.addActionListener(e -> toggleActiveSelectedTrainer());

        JButton btnDelete = UITheme.createButton("🗑️ Delete", UITheme.DANGER, UITheme.TEXT_LIGHT);
        btnDelete.addActionListener(e -> deleteSelectedTrainer());

        // Trainer records can only be changed by HR Administrators
        if (mainFrame.isAdmin()) {
            actionPanel.add(btnAdd);
            actionPanel.add(btnEdit);
            actionPanel.add(btnToggle);
            actionPanel.add(btnDelete);
        }

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"Trainer ID", "Full Name", "Email Address", "Phone", "Specialization", "Experience", "Rate/Hr", "Type", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        trainerTable = new JTable(tableModel);
        UITheme.styleTable(trainerTable);

        JScrollPane scrollPane = new JScrollPane(trainerTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadTrainersTable() {
        tableModel.setRowCount(0);
        String query = txtSearch.getText().trim();
        Boolean activeOnly = chkActiveOnly.isSelected() ? Boolean.TRUE : null;

        List<Trainer> trainers = service.searchTrainers(query, activeOnly);
        for (Trainer t : trainers) {
            tableModel.addRow(new Object[]{
                t.getId(),
                t.getFullName(),
                t.getEmail(),
                t.getPhone(),
                t.getSpecialization(),
                t.getYearsOfExperience() + " yrs",
                String.format("$%.2f", t.getHourlyRate()),
                t.isExternal() ? "External" : "In-House",
                t.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private void showAddEditTrainerDialog(Trainer existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog(mainFrame, isEdit ? "Edit Trainer Details" : "Register New Trainer", true);
        dlg.setSize(480, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridBagLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 12, 8, 12);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtFn = UITheme.createTextField(18);
        JTextField txtLn = UITheme.createTextField(18);
        JTextField txtEmail = UITheme.createTextField(18);
        JTextField txtPhone = UITheme.createTextField(18);
        JTextField txtSpec = UITheme.createTextField(18);
        JTextField txtExp = UITheme.createTextField(18);
        JTextField txtRate = UITheme.createTextField(18);
        JCheckBox chkExt = new JCheckBox("External Consultant");
        chkExt.setOpaque(false);

        if (isEdit) {
            txtFn.setText(existing.getFirstName());
            txtLn.setText(existing.getLastName());
            txtEmail.setText(existing.getEmail());
            txtPhone.setText(existing.getPhone());
            txtSpec.setText(existing.getSpecialization());
            txtExp.setText(String.valueOf(existing.getYearsOfExperience()));
            txtRate.setText(String.valueOf(existing.getHourlyRate()));
            chkExt.setSelected(existing.isExternal());
        } else {
            txtExp.setText("5");
            txtRate.setText("100.0");
        }

        g.gridx = 0; g.gridy = 0; dlg.add(new JLabel("First Name:"), g);
        g.gridx = 1; dlg.add(txtFn, g);

        g.gridx = 0; g.gridy = 1; dlg.add(new JLabel("Last Name:"), g);
        g.gridx = 1; dlg.add(txtLn, g);

        g.gridx = 0; g.gridy = 2; dlg.add(new JLabel("Email:"), g);
        g.gridx = 1; dlg.add(txtEmail, g);

        g.gridx = 0; g.gridy = 3; dlg.add(new JLabel("Phone:"), g);
        g.gridx = 1; dlg.add(txtPhone, g);

        g.gridx = 0; g.gridy = 4; dlg.add(new JLabel("Specialization:"), g);
        g.gridx = 1; dlg.add(txtSpec, g);

        g.gridx = 0; g.gridy = 5; dlg.add(new JLabel("Years Experience:"), g);
        g.gridx = 1; dlg.add(txtExp, g);

        g.gridx = 0; g.gridy = 6; dlg.add(new JLabel("Hourly Rate ($):"), g);
        g.gridx = 1; dlg.add(txtRate, g);

        g.gridx = 1; g.gridy = 7; dlg.add(chkExt, g);

        JButton btnSave = UITheme.createButton(isEdit ? "Update Trainer" : "Save Trainer", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnSave.addActionListener(e -> {
            String fn = txtFn.getText().trim();
            String ln = txtLn.getText().trim();
            String email = txtEmail.getText().trim();
            String spec = txtSpec.getText().trim();

            if (fn.isEmpty() || ln.isEmpty() || email.isEmpty() || spec.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(dlg, "Please enter a valid email format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int exp;
            double rate;
            try {
                exp = Integer.parseInt(txtExp.getText().trim());
                rate = Double.parseDouble(txtRate.getText().trim());
                if (exp < 0 || rate < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Experience and hourly rate must be non-negative numbers.", "Format Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (isEdit) {
                    existing.setFirstName(fn);
                    existing.setLastName(ln);
                    existing.setEmail(email);
                    existing.setPhone(txtPhone.getText().trim());
                    existing.setSpecialization(spec);
                    existing.setYearsOfExperience(exp);
                    existing.setHourlyRate(rate);
                    existing.setExternal(chkExt.isSelected());
                    service.updateTrainer(existing);
                } else {
                    Trainer t = new Trainer(null, fn, ln, email, txtPhone.getText().trim(),
                            spec, exp, rate, chkExt.isSelected());
                    service.registerTrainer(t);
                }
                dlg.dispose();
                loadTrainersTable();
                mainFrame.refreshAllViews();
                JOptionPane.showMessageDialog(this, "Trainer record saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        g.gridx = 0; g.gridy = 8; g.gridwidth = 2;
        g.insets = new Insets(16, 12, 8, 12);
        dlg.add(btnSave, g);

        dlg.setVisible(true);
    }

    private void editSelectedTrainer() {
        int row = trainerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a trainer to edit.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        service.findTrainerById(id).ifPresent(this::showAddEditTrainerDialog);
    }

    private void toggleActiveSelectedTrainer() {
        int row = trainerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a trainer to toggle status.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        service.findTrainerById(id).ifPresent(t -> {
            boolean newStatus = !t.isActive();
            service.setTrainerActive(id, newStatus);
            loadTrainersTable();
            mainFrame.refreshAllViews();
            JOptionPane.showMessageDialog(this, "Trainer " + t.getFullName() + " status updated to " +
                    (newStatus ? "Active" : "Inactive"), "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void deleteSelectedTrainer() {
        int row = trainerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a trainer to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently delete trainer " + name + " [" + id + "]?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteTrainer(id);
                loadTrainersTable();
                mainFrame.refreshAllViews();
                JOptionPane.showMessageDialog(this, "Trainer deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Deletion Blocked", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
