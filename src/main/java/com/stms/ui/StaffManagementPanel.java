package com.stms.ui;

import com.stms.model.Department;
import com.stms.model.Employee;
import com.stms.service.TrainingManagerService;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
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
 * Staff (Employee) Management Panel.
 * Demonstrates CRUD operations, status toggling, and SQLite persistence.
 */
public class StaffManagementPanel extends JPanel {
    private final TrainingManagerService service;
    private final MainApplicationFrame mainFrame;

    private JTable staffTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbDeptFilter;
    private JComboBox<String> cbStatusFilter;

    public StaffManagementPanel(TrainingManagerService service, MainApplicationFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadStaffTable();
    }

    private void initComponents() {
        // TOP: Filter Toolbar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        txtSearch = UITheme.createTextField(14);
        txtSearch.addActionListener(e -> loadStaffTable());

        JLabel lblFilter = new JLabel("Department:");
        lblFilter.setFont(UITheme.FONT_BOLD);

        String[] depts = new String[Department.values().length + 1];
        depts[0] = "All Departments";
        for (int i = 0; i < Department.values().length; i++) {
            depts[i + 1] = Department.values()[i].getDisplayName();
        }
        cbDeptFilter = UITheme.createComboBox(depts);
        cbDeptFilter.addActionListener(e -> loadStaffTable());

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        cbStatusFilter = UITheme.createComboBox(new String[]{"All Statuses", "Active Only", "Inactive Only"});
        cbStatusFilter.addActionListener(e -> loadStaffTable());

        JButton btnFilter = UITheme.createButton("Filter", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnFilter.addActionListener(e -> loadStaffTable());

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblFilter);
        filterPanel.add(cbDeptFilter);
        filterPanel.add(lblStatus);
        filterPanel.add(cbStatusFilter);
        filterPanel.add(btnFilter);

        // Buttons Panel on Right
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd = UITheme.createButton("➕ Add Staff", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnAdd.addActionListener(e -> showAddEditStaffDialog(null));

        JButton btnEdit = UITheme.createButton("✏️ Edit Selected", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnEdit.addActionListener(e -> editSelectedStaff());

        JButton btnToggle = UITheme.createButton("🔄 Toggle Status", UITheme.PURPLE, UITheme.TEXT_LIGHT);
        btnToggle.addActionListener(e -> toggleActiveSelectedStaff());

        JButton btnDelete = UITheme.createButton("🗑️ Delete", UITheme.DANGER, UITheme.TEXT_LIGHT);
        btnDelete.addActionListener(e -> deleteSelectedStaff());

        JButton btnTranscript = UITheme.createButton("📄 Transcript", UITheme.SUCCESS, UITheme.TEXT_LIGHT);
        btnTranscript.addActionListener(e -> viewSelectedTranscript());

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnToggle);
        actionPanel.add(btnDelete);
        actionPanel.add(btnTranscript);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // CENTER: JTable
        String[] columns = {"ID", "Full Name", "Department", "Job Title", "Email", "Phone", "Status", "Enrollments"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        staffTable = new JTable(tableModel);
        UITheme.styleTable(staffTable);

        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadStaffTable() {
        tableModel.setRowCount(0);
        String query = txtSearch.getText().trim();
        String deptFilter = (String) cbDeptFilter.getSelectedItem();
        String statusFilter = (String) cbStatusFilter.getSelectedItem();

        String deptParam = (deptFilter != null && !deptFilter.equals("All Departments")) ? null : "ALL";
        if (deptFilter != null && !deptFilter.equals("All Departments")) {
            for (Department d : Department.values()) {
                if (d.getDisplayName().equalsIgnoreCase(deptFilter)) {
                    deptParam = d.name();
                    break;
                }
            }
        }

        Boolean activeParam = null;
        if ("Active Only".equals(statusFilter)) activeParam = Boolean.TRUE;
        if ("Inactive Only".equals(statusFilter)) activeParam = Boolean.FALSE;

        List<Employee> list = service.searchEmployees(query, deptParam, activeParam);
        for (Employee e : list) {
            int enrCount = service.getEnrollmentsByEmployee(e.getId()).size();
            tableModel.addRow(new Object[]{
                    e.getId(),
                    e.getFullName(),
                    e.getDepartment().getDisplayName(),
                    e.getJobTitle(),
                    e.getEmail(),
                    e.getPhone(),
                    e.isActive() ? "Active" : "Inactive",
                    enrCount
            });
        }
    }

    private void showAddEditStaffDialog(Employee existing) {
        JDialog dialog = new JDialog(mainFrame, existing == null ? "Register New Staff Member" : "Edit Staff Member", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(440, 480);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JTextField tfId = UITheme.createTextField(15);
        JTextField tfFirst = UITheme.createTextField(15);
        JTextField tfLast = UITheme.createTextField(15);
        JTextField tfEmail = UITheme.createTextField(15);
        JTextField tfPhone = UITheme.createTextField(15);
        JTextField tfTitle = UITheme.createTextField(15);
        JComboBox<Department> cbDept = UITheme.createComboBox(Department.values());
        JCheckBox chkActive = new JCheckBox("Active Status", true);

        if (existing != null) {
            tfId.setText(existing.getId());
            tfId.setEditable(false);
            tfFirst.setText(existing.getFirstName());
            tfLast.setText(existing.getLastName());
            tfEmail.setText(existing.getEmail());
            tfPhone.setText(existing.getPhone());
            tfTitle.setText(existing.getJobTitle());
            cbDept.setSelectedItem(existing.getDepartment());
            chkActive.setSelected(existing.isActive());
        } else {
            tfId.setText(service.getNextEmployeeId());
        }

        form.add(new JLabel("Staff ID (Auto):"));
        form.add(tfId);
        form.add(new JLabel("First Name:"));
        form.add(tfFirst);
        form.add(new JLabel("Last Name:"));
        form.add(tfLast);
        form.add(new JLabel("Corporate Email:"));
        form.add(tfEmail);
        form.add(new JLabel("Phone Number:"));
        form.add(tfPhone);
        form.add(new JLabel("Job Title:"));
        form.add(tfTitle);
        form.add(new JLabel("Department:"));
        form.add(cbDept);
        form.add(new JLabel("Status:"));
        form.add(chkActive);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createButton("💾 Save", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        JButton btnCancel = UITheme.createButton("Cancel", UITheme.SECONDARY, UITheme.TEXT_LIGHT);

        btnSave.addActionListener(e -> {
            try {
                String empId = tfId.getText().trim();
                if (empId.isEmpty()) {
                    empId = service.getNextEmployeeId();
                }
                String first = tfFirst.getText().trim();
                String last = tfLast.getText().trim();
                String email = tfEmail.getText().trim();
                String phone = tfPhone.getText().trim();
                String title = tfTitle.getText().trim();
                Department dept = (Department) cbDept.getSelectedItem();
                boolean active = chkActive.isSelected();

                if (first.isEmpty() || last.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "First and last name are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!email.contains("@")) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (existing == null) {
                    Employee newEmp = new Employee(empId, first, last, email, phone, dept, title, LocalDate.now());
                    newEmp.setActive(active);
                    service.registerEmployee(newEmp);
                } else {
                    existing.setFirstName(first);
                    existing.setLastName(last);
                    existing.setEmail(email);
                    existing.setPhone(phone);
                    existing.setJobTitle(title);
                    existing.setDepartment(dept);
                    existing.setActive(active);
                    service.updateEmployee(existing);
                }

                dialog.dispose();
                loadStaffTable();
                mainFrame.refreshAllViews();
                JOptionPane.showMessageDialog(this, "Employee record saved successfully in SQLite!", "Success", JOptionPane.INFORMATION_MESSAGE);
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

    private void editSelectedStaff() {
        int selected = staffTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(selected, 0);
        service.findEmployeeById(id).ifPresent(this::showAddEditStaffDialog);
    }

    private void toggleActiveSelectedStaff() {
        int selected = staffTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to toggle status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(selected, 0);
        service.findEmployeeById(id).ifPresent(emp -> {
            boolean newStatus = !emp.isActive();
            service.setEmployeeActive(id, newStatus);
            loadStaffTable();
            mainFrame.refreshAllViews();
            JOptionPane.showMessageDialog(this, "Staff " + emp.getFullName() + " status updated to " +
                    (newStatus ? "Active" : "Inactive"), "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void deleteSelectedStaff() {
        int selected = staffTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(selected, 0);
        String name = (String) tableModel.getValueAt(selected, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete staff member: " + name + " [" + id + "]?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteEmployee(id);
                loadStaffTable();
                mainFrame.refreshAllViews();
                JOptionPane.showMessageDialog(this, "Employee record deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Deletion Blocked", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewSelectedTranscript() {
        int selected = staffTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(selected, 0);
        mainFrame.showEmployeeTranscript(id);
    }
}
