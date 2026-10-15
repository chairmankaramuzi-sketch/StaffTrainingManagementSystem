package com.stms.ui;

import com.stms.model.Certificate;
import com.stms.model.Employee;
import com.stms.service.TrainingManagerService;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Reporting, Transcripts & Certificate Verification Panel.
 * Supports Official Transcript Generation, Department Analytics,
 * Verification Code Authentication, and CSV/Text Exports.
 */
public class ReportsPanel extends JPanel {
    private final TrainingManagerService service;
    private final MainApplicationFrame mainFrame;

    private JComboBox<Employee> cbEmployees;
    private JTextArea txtReportArea;

    public ReportsPanel(TrainingManagerService service, MainApplicationFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadEmployeesDropdown();
        generateSelectedTranscript();
    }

    private void initComponents() {
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel lblSelect = new JLabel("Select Staff Member:");
        lblSelect.setFont(UITheme.FONT_BOLD);
        cbEmployees = new JComboBox<>();
        cbEmployees.setFont(UITheme.FONT_REGULAR);
        cbEmployees.addActionListener(e -> generateSelectedTranscript());

        JButton btnDeptSummary = UITheme.createButton("📊 Department Summary", UITheme.PURPLE, UITheme.TEXT_LIGHT);
        btnDeptSummary.addActionListener(e -> showDepartmentSummary());

        leftPanel.add(lblSelect);
        leftPanel.add(cbEmployees);
        leftPanel.add(btnDeptSummary);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        JButton btnVerify = UITheme.createButton("🔍 Verify Certificate", UITheme.SUCCESS, UITheme.TEXT_LIGHT);
        btnVerify.addActionListener(e -> showVerifyCertificateDialog());

        JButton btnExportCsv = UITheme.createButton("📊 Export CSV", UITheme.SECONDARY, UITheme.TEXT_LIGHT);
        btnExportCsv.addActionListener(e -> exportCsvDialog());

        JButton btnExportTxt = UITheme.createButton("💾 Export Text", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnExportTxt.addActionListener(e -> exportReportToFile());

        rightPanel.add(btnVerify);
        rightPanel.add(btnExportCsv);
        rightPanel.add(btnExportTxt);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Text Area
        txtReportArea = new JTextArea();
        txtReportArea.setFont(UITheme.FONT_MONO);
        txtReportArea.setEditable(false);
        txtReportArea.setBackground(UITheme.BG_SURFACE);
        txtReportArea.setForeground(UITheme.TEXT_PRIMARY);
        txtReportArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(txtReportArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadEmployeesDropdown() {
        cbEmployees.removeAllItems();
        List<Employee> list = service.getAllEmployees();
        for (Employee e : list) {
            cbEmployees.addItem(e);
        }
    }

    public void selectEmployee(String employeeId) {
        for (int i = 0; i < cbEmployees.getItemCount(); i++) {
            Employee e = cbEmployees.getItemAt(i);
            if (e.getId().equalsIgnoreCase(employeeId)) {
                cbEmployees.setSelectedIndex(i);
                break;
            }
        }
    }

    public void generateSelectedTranscript() {
        Employee selected = (Employee) cbEmployees.getSelectedItem();
        if (selected == null) {
            txtReportArea.setText("No staff member selected.");
            return;
        }
        String transcript = service.generateStaffTranscript(selected.getId());
        txtReportArea.setText(transcript);
        txtReportArea.setCaretPosition(0);
    }

    private void showDepartmentSummary() {
        String report = service.generateDepartmentSummary();
        txtReportArea.setText(report);
        txtReportArea.setCaretPosition(0);
    }

    private void showVerifyCertificateDialog() {
        String code = JOptionPane.showInputDialog(mainFrame,
                "Enter Certificate Verification Code (e.g., VER-JAVA-901):\n\n" +
                "Tip: You can find verification codes on issued transcripts or the certificate database.",
                "Verify Certificate Authenticity", JOptionPane.QUESTION_MESSAGE);

        if (code == null || code.trim().isEmpty()) {
            return;
        }

        Optional<Certificate> optCert = service.verifyCertificate(code.trim());
        if (optCert.isPresent()) {
            Certificate cert = optCert.get();
            String details = String.format(
                    "🎓 CERTIFICATE VERIFIED AUTHENTIC & GENUINE\n\n" +
                    "Certificate ID      : %s\n" +
                    "Verification Code   : %s\n" +
                    "Recipient           : %s [%s]\n" +
                    "Course Name         : %s\n" +
                    "Category            : %s\n" +
                    "Date Issued         : %s\n" +
                    "Expiry Date         : %s\n" +
                    "Current Status      : %s\n\n" +
                    "Official record stored in SQLite JDBC Database (data/stms.db).",
                    cert.getId(), cert.getVerificationCode(),
                    cert.getEmployeeName(), cert.getEmployeeId(),
                    cert.getCourseTitle(), cert.getCourseCategory(),
                    cert.getIssueDate(),
                    cert.getExpiryDate() != null ? cert.getExpiryDate() : "Permanent / No Expiry",
                    cert.getValidityStatus()
            );
            JOptionPane.showMessageDialog(mainFrame, details, "Certificate Verification Result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(mainFrame,
                    "❌ UNVERIFIED: No authentic certificate record found matching:\n\n'" + code.trim() + "'\n\n" +
                    "Please verify the code and ensure it was registered and stored in SQLite.",
                    "Certificate Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsvDialog() {
        String[] options = {"Selected Staff Transcript (CSV)", "Full Department Summary (CSV)"};
        int choice = JOptionPane.showOptionDialog(mainFrame,
                "Select CSV Export Format:", "Export CSV Data",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            Employee selected = (Employee) cbEmployees.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(mainFrame, "No staff member selected.", "Export Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(selected.getId() + "_training_transcript.csv"));
            if (chooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                try {
                    service.exportTranscriptToCsv(selected.getId(), chooser.getSelectedFile());
                    JOptionPane.showMessageDialog(mainFrame,
                            "Transcript exported to CSV:\n" + chooser.getSelectedFile().getAbsolutePath(),
                            "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Error exporting CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (choice == 1) {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("department_training_summary.csv"));
            if (chooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                try {
                    service.exportDepartmentSummaryToCsv(chooser.getSelectedFile());
                    JOptionPane.showMessageDialog(mainFrame,
                            "Department summary exported to CSV:\n" + chooser.getSelectedFile().getAbsolutePath(),
                            "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Error exporting CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void exportReportToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Training Report as Text");
        fileChooser.setSelectedFile(new File("staff_training_transcript.txt"));

        int userSelection = fileChooser.showSaveDialog(mainFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(fileToSave)) {
                fw.write(txtReportArea.getText());
                JOptionPane.showMessageDialog(mainFrame,
                        "Report successfully exported to:\n" + fileToSave.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Error exporting file: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
