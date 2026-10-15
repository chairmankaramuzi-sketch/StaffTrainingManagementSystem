package com.stms;

import com.stms.model.User;
import com.stms.service.AuthService;
import com.stms.service.TrainingManagerService;
import com.stms.ui.LoginDialog;
import com.stms.ui.MainApplicationFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the Staff Training Management System.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("        STAFF TRAINING MANAGEMENT SYSTEM (STMS) - OOP          ");
        System.out.println("===============================================================");
        System.out.println("Status: Initializing Enterprise Domain Model and Repositories...");

        // Set native system look and feel for sleek UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Using default Swing Look and Feel.");
        }

        // Initialize business logic service
        TrainingManagerService service = new TrainingManagerService();
        AuthService authService = new AuthService();

        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> showLoginAndLaunch(service, authService));
    }

    /**
     * Shows the login screen; on success opens the main window for that user.
     * Closing the login screen without signing in exits the application.
     */
    private static void showLoginAndLaunch(TrainingManagerService service, AuthService authService) {
        LoginDialog login = new LoginDialog(authService);
        login.setVisible(true); // modal - blocks until closed

        User user = login.getAuthenticatedUser();
        if (user == null) {
            System.out.println("Status: Login cancelled. Exiting.");
            System.exit(0);
            return;
        }

        System.out.println("Status: Logged in as " + user.getUsername() + " (" + user.getRole().getDisplayName() + ")");
        MainApplicationFrame frame = new MainApplicationFrame(service, authService, user,
                () -> showLoginAndLaunch(service, authService));
        frame.setVisible(true);
        System.out.println("Status: GUI Window launched successfully.");
    }
}

