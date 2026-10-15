package com.stms.ui;

import com.stms.model.User;
import com.stms.service.AuthService;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Login screen shown before the main application window.
 */
public class LoginDialog extends JDialog {
    private static final int MAX_ATTEMPTS = 5;

    private final AuthService authService;
    private User authenticatedUser;
    private int failedAttempts = 0;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;

    public LoginDialog(AuthService authService) {
        super((java.awt.Frame) null, "STMS - Sign In", true);
        this.authService = authService;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());

        // Left brand panel
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(UITheme.BG_DARK);
        brand.setPreferredSize(new Dimension(300, 380));
        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 0;
        b.insets = new Insets(4, 24, 4, 24);

        JLabel lblLogo = new JLabel("STMS", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblLogo.setForeground(Color.WHITE);
        JLabel lblName = new JLabel("<html><div style='text-align:center'>Staff Training<br>Management System</div></html>", SwingConstants.CENTER);
        lblName.setFont(UITheme.FONT_SUBTITLE);
        lblName.setForeground(new Color(0x94, 0xA3, 0xB8));
        brand.add(lblLogo, b);
        brand.add(lblName, b);

        // Right form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));
        form.setPreferredSize(new Dimension(380, 380));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(4, 0, 4, 0);

        JLabel lblTitle = new JLabel("Sign in");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Enter your account credentials");
        lblSub.setFont(UITheme.FONT_REGULAR);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        txtUsername = UITheme.createTextField(20);
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setPreferredSize(new Dimension(txtPassword.getPreferredSize().width, 32));
        txtPassword.setBorder(txtUsername.getBorder());

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);

        JButton btnLogin = UITheme.createButton("Sign In", UITheme.PRIMARY, Color.WHITE);
        btnLogin.addActionListener(e -> attemptLogin());
        getRootPane().setDefaultButton(btnLogin);

        JLabel lblHint = new JLabel("<html>Default accounts: <b>admin</b>/admin123, <b>trainer</b>/trainer123, <b>employee</b>/employee123</html>");
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(UITheme.TEXT_MUTED);

        form.add(lblTitle, g);
        form.add(lblSub, g);
        g.insets = new Insets(16, 0, 2, 0);
        form.add(label("Username"), g);
        g.insets = new Insets(2, 0, 4, 0);
        form.add(txtUsername, g);
        g.insets = new Insets(10, 0, 2, 0);
        form.add(label("Password"), g);
        g.insets = new Insets(2, 0, 4, 0);
        form.add(txtPassword, g);
        form.add(lblError, g);
        g.insets = new Insets(8, 0, 4, 0);
        form.add(btnLogin, g);
        g.insets = new Insets(16, 0, 0, 0);
        form.add(lblHint, g);

        root.add(brand, BorderLayout.WEST);
        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private void attemptLogin() {
        char[] password = txtPassword.getPassword();
        try {
            Optional<User> user = authService.authenticate(txtUsername.getText(), password);
            if (user.isPresent()) {
                authenticatedUser = user.get();
                dispose();
                return;
            }
        } finally {
            Arrays.fill(password, '\0');
        }

        failedAttempts++;
        txtPassword.setText("");
        if (failedAttempts >= MAX_ATTEMPTS) {
            lblError.setText("Too many failed attempts. Closing.");
            dispose();
            return;
        }
        lblError.setText("Invalid username or password (" + (MAX_ATTEMPTS - failedAttempts) + " attempts left)");
        txtPassword.requestFocusInWindow();
    }

    /**
     * Returns the logged-in user, or null if the dialog was closed without a successful login.
     */
    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}
