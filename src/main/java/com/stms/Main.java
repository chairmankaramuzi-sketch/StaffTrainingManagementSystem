package com.stms;

import com.stms.service.TrainingManagerService;
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

        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainApplicationFrame frame = new MainApplicationFrame(service);
            frame.setVisible(true);
            System.out.println("Status: GUI Window launched successfully.");
            System.out.println("Ready to run in Apache NetBeans IDE!");
        });
    }
}
