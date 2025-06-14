package com.foodforall.ui;
import com.foodforall.model.AuditLog;
import com.foodforall.model.User;
import com.foodforall.service.AuditService;
import com.foodforall.service.UserService;
import com.foodforall.util.DateUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AuditUI extends JDialog implements ActionListener {
    
    private JComboBox<String> dateRangeComboBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JComboBox<User> userComboBox;
    private JButton filterButton;
    private JButton exportButton;
    
    private DefaultTableModel tableModel;
    private JTable auditTable;
    
    private List<AuditLog> logs;
    
    public AuditUI(JFrame parent) {
        super(parent, "Audit Log", true);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        
        initComponents();
        layoutComponents();
        loadUsers();
        loadLogs();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Date Range
        dateRangeComboBox = new JComboBox<>(new String[]{"All Time", "Today", "Yesterday", "This Week", "This Month", "Custom"});
        dateRangeComboBox.addActionListener(e -> updateDateFields());
        
        startDateField = new JTextField(10);
        endDateField = new JTextField(10);
        
        // Set default dates
        setDefaultDates();
        
        // User Filter
        userComboBox = new JComboBox<>();
        userComboBox.setPreferredSize(new Dimension(200, 25));
        
        // Buttons
        filterButton = new JButton("Apply Filter");
        filterButton.setActionCommand("filter");
        filterButton.addActionListener(this);
        
        exportButton = new JButton("Export Log");
        exportButton.setActionCommand("export");
        exportButton.addActionListener(this);
        
        // Audit Table
        String[] columns = {"ID", "Timestamp", "User", "Action", "Entity", "Entity ID", "Details"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        auditTable = new JTable(tableModel);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Filter Panel
        JPanel filterPanel = new JPanel();
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        
        filterPanel.add(new JLabel("Date Range:"));
        filterPanel.add(dateRangeComboBox);
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(startDateField);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(endDateField);
        filterPanel.add(new JLabel("User:"));
        filterPanel.add(userComboBox);
        filterPanel.add(filterButton);
        filterPanel.add(exportButton);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Audit Logs"));
        
        JScrollPane scrollPane = new JScrollPane(auditTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add panels to main layout
        add(filterPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void setDefaultDates() {
        LocalDate today = LocalDate.now();
        startDateField.setText(DateUtil.formatDate(today));
        endDateField.setText(DateUtil.formatDate(today));
        
        // Initially disable date fields
        updateDateFields();
    }
    
    private void updateDateFields() {
        String selectedRange = (String) dateRangeComboBox.getSelectedItem();
        
        if ("Custom".equals(selectedRange)) {
            startDateField.setEnabled(true);
            endDateField.setEnabled(true);
            return;
        }
        
        startDateField.setEnabled(false);
        endDateField.setEnabled(false);
        
        LocalDate start = null;
        LocalDate end = LocalDate.now();
        
        switch (selectedRange) {
            case "All Time":
                // Don't set dates for "All Time"
                return;
            case "Today":
                start = end;
                break;
            case "Yesterday":
                start = end.minusDays(1);
                end = start;
                break;
            case "This Week":
                start = end.minusDays(end.getDayOfWeek().getValue() - 1);
                break;
            case "This Month":
                start = end.withDayOfMonth(1);
                break;
        }
        
        if (start != null) {
            startDateField.setText(DateUtil.formatDate(start));
            endDateField.setText(DateUtil.formatDate(end));
        }
    }
    
    private void loadUsers() {
        try {
            // Clear the combo box
            userComboBox.removeAllItems();
            
            // Add "All Users" option
            userComboBox.addItem(null);
            
            // Get all users
            List<User> users = UserService.getInstance().getAllUsers();
            
            // Add users to combo box
            for (User user : users) {
                userComboBox.addItem(user);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadLogs() {
        try {
            // Clear the table
            tableModel.setRowCount(0);
            
            String selectedRange = (String) dateRangeComboBox.getSelectedItem();
            User selectedUser = (User) userComboBox.getSelectedItem();
            
            // Get logs based on filters
            if ("All Time".equals(selectedRange) && selectedUser == null) {
                // Get all logs
                logs = AuditService.getInstance().getAllLogs();
            } else if (selectedUser != null) {
                // Filter by user
                logs = AuditService.getInstance().getLogsByUser(selectedUser.getUserId());
                
                // Further filter by date if needed
                if (!"All Time".equals(selectedRange)) {
                    filterLogsByDate();
                }
            } else {
                // Filter by date
                LocalDate startDate = DateUtil.parseDate(startDateField.getText());
                LocalDate endDate = DateUtil.parseDate(endDateField.getText());
                
                if (startDate != null && endDate != null) {
                    logs = AuditService.getInstance().getLogsByDateRange(startDate, endDate);
                } else {
                    logs = AuditService.getInstance().getAllLogs();
                }
            }
            
            // Add logs to table
            for (AuditLog log : logs) {
                Object[] row = {
                    log.getLogId(),
                    DateUtil.formatDisplayDateTime(log.getTimestamp()),
                    log.getUser().getUsername(),
                    log.getAction(),
                    log.getEntity(),
                    log.getEntityId(),
                    log.getDetails()
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading audit logs: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filterLogsByDate() {
        LocalDate startDate = DateUtil.parseDate(startDateField.getText());
        LocalDate endDate = DateUtil.parseDate(endDateField.getText());
        
        if (startDate != null && endDate != null && logs != null) {
            // Create a temporary list to hold filtered logs
            java.util.List<AuditLog> filteredLogs = new java.util.ArrayList<>();
            
            for (AuditLog log : logs) {
                LocalDate logDate = log.getTimestamp().toLocalDate();
                
                if ((logDate.isEqual(startDate) || logDate.isAfter(startDate)) &&
                        (logDate.isEqual(endDate) || logDate.isBefore(endDate))) {
                    filteredLogs.add(log);
                }
            }
            
            // Update logs list
            logs = filteredLogs;
        }
    }
    
    private void exportLogs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Audit Log");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "Audit log would be exported to: " + 
                    fileChooser.getSelectedFile().getAbsolutePath() +
                    "\n\nThis feature is not fully implemented in this version.",
                    "Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if ("filter".equals(command)) {
            loadLogs();
        } else if ("export".equals(command)) {
            exportLogs();
        }
    }
}