package com.stms.ui;

import com.stms.model.User;
import com.stms.model.UserRole;
import com.stms.service.AuthService;
import com.stms.service.AuthService.UserAccount;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * HR Administrator screen for managing login accounts.
 */
public class UserManagementPanel extends JPanel {
    static final int MIN_PASSWORD_LENGTH = 6;

    private final AuthService authService;
    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable table;

    public UserManagementPanel(AuthService authService, User currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        initComponents();
        loadUsers();
    }

    private void initComponents() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("User Accounts");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnAdd = UITheme.createButton("Add User", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnAdd.addActionListener(e -> addUser());
        JButton btnEdit = UITheme.createButton("Edit", UITheme.PURPLE, UITheme.TEXT_LIGHT);
        btnEdit.addActionListener(e -> editUser());
        JButton btnReset = UITheme.createButton("Reset Password", UITheme.WARNING, UITheme.TEXT_LIGHT);
        btnReset.addActionListener(e -> resetPassword());
        JButton btnToggle = UITheme.createButton("Enable / Disable", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnToggle.addActionListener(e -> toggleActive());
        JButton btnDelete = UITheme.createButton("Delete", UITheme.DANGER, UITheme.TEXT_LIGHT);
        btnDelete.addActionListener(e -> deleteUser());

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnReset);
        actions.add(btnToggle);
        actions.add(btnDelete);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[] {"Username", "Full Name", "Role", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        add(scroll, BorderLayout.CENTER);
    }

    public void loadUsers() {
        tableModel.setRowCount(0);
        for (UserAccount u : authService.listUsers()) {
            tableModel.addRow(new Object[] {
                u.username(), u.fullName(), u.role().getDisplayName(), u.active() ? "Active" : "Disabled"
            });
        }
    }

    private UserAccount selectedAccount() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String username = (String) tableModel.getValueAt(row, 0);
        return authService.listUsers().stream().filter(u -> u.username().equals(username)).findFirst().orElse(null);
    }

    private boolean isSelf(UserAccount acc) {
        return acc.username().equalsIgnoreCase(currentUser.getUsername());
    }

    /** True if this change would leave no active HR Administrator. */
    private boolean wouldRemoveLastAdmin(UserAccount acc) {
        return acc.role() == UserRole.HR_ADMINISTRATOR && acc.active() && authService.countActiveAdmins() <= 1;
    }

    private void addUser() {
        JTextField txtUser = UITheme.createTextField(18);
        JTextField txtName = UITheme.createTextField(18);
        JComboBox<UserRole> cbRole = UITheme.createComboBox(UserRole.values());
        JPasswordField txtPw = new JPasswordField(18);
        JPasswordField txtPw2 = new JPasswordField(18);

        JPanel form = formPanel(
                "Username", txtUser, "Full Name", txtName, "Role", cbRole,
                "Password", txtPw, "Confirm Password", txtPw2);

        while (true) {
            int res = JOptionPane.showConfirmDialog(this, form, "Add User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) {
                return;
            }
            String username = txtUser.getText().trim().toLowerCase();
            String fullName = txtName.getText().trim();
            if (!username.matches("[a-z0-9._-]{3,50}")) {
                error("Username must be 3-50 characters: letters, numbers, dot, dash or underscore.");
                continue;
            }
            if (fullName.isEmpty()) {
                error("Full name is required.");
                continue;
            }
            if (authService.usernameExists(username)) {
                error("Username '" + username + "' is already taken.");
                continue;
            }
            String pw = validateNewPassword(txtPw, txtPw2);
            if (pw == null) {
                continue;
            }
            if (authService.createUser(username, pw, fullName, (UserRole) cbRole.getSelectedItem())) {
                loadUsers();
                info("User '" + username + "' created.");
            } else {
                error("Could not create user. See console for details.");
            }
            return;
        }
    }

    private void editUser() {
        UserAccount acc = selectedAccount();
        if (acc == null) {
            return;
        }
        JTextField txtName = UITheme.createTextField(18);
        txtName.setText(acc.fullName());
        JComboBox<UserRole> cbRole = UITheme.createComboBox(UserRole.values());
        cbRole.setSelectedItem(acc.role());

        JPanel form = formPanel("Username", new JLabel(acc.username()), "Full Name", txtName, "Role", cbRole);
        int res = JOptionPane.showConfirmDialog(this, form, "Edit User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }
        String fullName = txtName.getText().trim();
        UserRole newRole = (UserRole) cbRole.getSelectedItem();
        if (fullName.isEmpty()) {
            error("Full name is required.");
            return;
        }
        if (newRole != acc.role()) {
            if (isSelf(acc)) {
                error("You cannot change your own role.");
                return;
            }
            if (wouldRemoveLastAdmin(acc)) {
                error("At least one active HR Administrator is required.");
                return;
            }
        }
        if (authService.updateUser(acc.username(), fullName, newRole)) {
            loadUsers();
        } else {
            error("Could not update user.");
        }
    }

    private void resetPassword() {
        UserAccount acc = selectedAccount();
        if (acc == null) {
            return;
        }
        JPasswordField txtPw = new JPasswordField(18);
        JPasswordField txtPw2 = new JPasswordField(18);
        JPanel form = formPanel("New Password", txtPw, "Confirm Password", txtPw2);
        int res = JOptionPane.showConfirmDialog(this, form, "Reset Password - " + acc.username(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }
        String pw = validateNewPassword(txtPw, txtPw2);
        if (pw != null && authService.resetPassword(acc.username(), pw)) {
            info("Password for '" + acc.username() + "' has been reset.");
        }
    }

    private void toggleActive() {
        UserAccount acc = selectedAccount();
        if (acc == null) {
            return;
        }
        if (acc.active()) {
            if (isSelf(acc)) {
                error("You cannot disable your own account.");
                return;
            }
            if (wouldRemoveLastAdmin(acc)) {
                error("At least one active HR Administrator is required.");
                return;
            }
        }
        authService.setActive(acc.username(), !acc.active());
        loadUsers();
    }

    private void deleteUser() {
        UserAccount acc = selectedAccount();
        if (acc == null) {
            return;
        }
        if (isSelf(acc)) {
            error("You cannot delete your own account.");
            return;
        }
        if (wouldRemoveLastAdmin(acc)) {
            error("At least one active HR Administrator is required.");
            return;
        }
        int res = JOptionPane.showConfirmDialog(this, "Permanently delete user '" + acc.username() + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION && authService.deleteUser(acc.username())) {
            loadUsers();
        }
    }

    // ---------------------------------------------------------------------
    // Shared helpers
    // ---------------------------------------------------------------------

    /**
     * Self-service password change dialog, usable by any logged-in user.
     */
    public static void showChangePasswordDialog(Component parent, AuthService authService, User user) {
        JPasswordField txtCurrent = new JPasswordField(18);
        JPasswordField txtPw = new JPasswordField(18);
        JPasswordField txtPw2 = new JPasswordField(18);
        JPanel form = formPanel("Current Password", txtCurrent, "New Password", txtPw, "Confirm New Password", txtPw2);

        int res = JOptionPane.showConfirmDialog(parent, form, "Change Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }
        String newPw = validateNewPassword(parent, txtPw, txtPw2);
        if (newPw == null) {
            return;
        }
        char[] current = txtCurrent.getPassword();
        try {
            if (authService.changePassword(user.getUsername(), current, newPw)) {
                JOptionPane.showMessageDialog(parent, "Your password has been changed.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parent, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            Arrays.fill(current, '\0');
        }
    }

    private String validateNewPassword(JPasswordField pw, JPasswordField confirm) {
        return validateNewPassword(this, pw, confirm);
    }

    private static String validateNewPassword(Component parent, JPasswordField pw, JPasswordField confirm) {
        String a = new String(pw.getPassword());
        String b = new String(confirm.getPassword());
        if (a.length() < MIN_PASSWORD_LENGTH) {
            JOptionPane.showMessageDialog(parent, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.",
                    "Invalid Password", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (!a.equals(b)) {
            JOptionPane.showMessageDialog(parent, "Passwords do not match.", "Invalid Password", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return a;
    }

    private static JPanel formPanel(Object... labelAndField) {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        for (int i = 0; i < labelAndField.length; i += 2) {
            JLabel l = new JLabel((String) labelAndField[i]);
            l.setFont(UITheme.FONT_BOLD);
            p.add(l);
            p.add((Component) labelAndField[i + 1]);
        }
        return p;
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
