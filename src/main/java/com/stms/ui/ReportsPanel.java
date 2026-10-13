package com.stms.ui;

import com.stms.model.Employee;
import com.stms.service.TrainingManagerService;
import com.stms.ui.components.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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
 * Reporting & Official Transcripts Panel.
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

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JButton btnExport = UITheme.createButton("💾 Export to File", UITheme.PRIMARY, UITheme.TEXT_LIGHT);
        btnExport.addActionListener(e -> exportReportToFile());

        rightPanel.add(btnExport);

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

    private void exportReportToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Training Report");
        fileChooser.setSelectedFile(new File("staff_training_transcript.txt"));

        int userSelection = fileChooser.showSaveDialog(mainFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(fileToSave)) {
                fw.write(txtReportArea.getText());
                JOptionPane.showMessageDialog(mainFrame, "Report successfully exported to:\n" + fileToSave.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Error exporting file: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
