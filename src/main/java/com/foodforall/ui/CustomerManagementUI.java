package com.foodforall.ui;
import com.foodforall.model.Customer;
import com.foodforall.service.CustomerService;
import com.foodforall.util.DateUtil;
import com.foodforall.util.ValidationUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

public class CustomerManagementUI extends JDialog implements ActionListener {
    
    private DefaultTableModel tableModel;
    private JTable customerTable;
    
    private JTextField fullNameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextArea addressArea;
    
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton searchButton;
    private JTextField searchField;
    
    private List<Customer> customers;
    private Customer selectedCustomer;
    private boolean editMode = false;
    
    public CustomerManagementUI(JFrame parent) {
        super(parent, "Customer Management", true);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        
        initComponents();
        layoutComponents();
        loadCustomers();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Table
        String[] columns = {"ID", "Full Name", "Phone", "Email", "Address", "Created"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectCustomer(selectedRow);
                }
            }
        });
        
        // Search components
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.setActionCommand("search");
        searchButton.addActionListener(this);
        
        // Form Fields
        fullNameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        addressArea = new JTextArea(4, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        
        // Buttons
        addButton = new JButton("Add Customer");
        addButton.setActionCommand("add");
        addButton.addActionListener(this);
        
        editButton = new JButton("Edit Customer");
        editButton.setActionCommand("edit");
        editButton.addActionListener(this);
        editButton.setEnabled(false);
        
        deleteButton = new JButton("Delete Customer");
        deleteButton.setActionCommand("delete");
        deleteButton.addActionListener(this);
        deleteButton.setEnabled(false);
        
        saveButton = new JButton("Save");
        saveButton.setActionCommand("save");
        saveButton.addActionListener(this);
        saveButton.setEnabled(false);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        cancelButton.setEnabled(false);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Customer List"));
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button Panel for Table
        JPanel tableButtonPanel = new JPanel();
        tableButtonPanel.add(addButton);
        tableButtonPanel.add(editButton);
        tableButtonPanel.add(deleteButton);
        
        tablePanel.add(tableButtonPanel, BorderLayout.SOUTH);
        
        // North panel containing search
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        formPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(fullNameField, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Phone:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(phoneField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(emailField, gbc);
        
        // Address
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Address:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane addressScrollPane = new JScrollPane(addressArea);
        formPanel.add(addressScrollPane, gbc);
        
        // Form buttons
        JPanel formButtonPanel = new JPanel();
        formButtonPanel.add(saveButton);
        formButtonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(formButtonPanel, gbc);
        
        // Add panels to main panel
        add(northPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);
        
        // Disable form fields initially
        setFormEnabled(false);
    }
    
    private void loadCustomers() {
        try {
            // Clear the table
            tableModel.setRowCount(0);
            
            // Get all customers
            customers = CustomerService.getInstance().getAllCustomers();
            
            // Add customers to table
            for (Customer customer : customers) {
                addCustomerToTable(customer);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addCustomerToTable(Customer customer) {
        Object[] row = {
            customer.getCustomerId(),
            customer.getFullName(),
            customer.getPhone(),
            customer.getEmail(),
            customer.getAddress(),
            DateUtil.formatDisplayDateTime(customer.getCreatedAt())
        };
        tableModel.addRow(row);
    }
    
    private void selectCustomer(int row) {
        selectedCustomer = customers.get(row);
        
        // Set form values
        fullNameField.setText(selectedCustomer.getFullName());
        phoneField.setText(selectedCustomer.getPhone());
        emailField.setText(selectedCustomer.getEmail());
        addressArea.setText(selectedCustomer.getAddress());
        
        // Enable edit and delete buttons
        editButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }
    
    private void clearForm() {
        fullNameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressArea.setText("");
    }
    
    private void setFormEnabled(boolean enabled) {
        fullNameField.setEnabled(enabled);
        phoneField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        addressArea.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        
        // Disable other buttons when form is active
        addButton.setEnabled(!enabled);
        editButton.setEnabled(!enabled && selectedCustomer != null);
        deleteButton.setEnabled(!enabled && selectedCustomer != null);
        customerTable.setEnabled(!enabled);
        searchField.setEnabled(!enabled);
        searchButton.setEnabled(!enabled);
    }
    
    private boolean validateForm() {
        // Check required fields
        if (fullNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate email if provided
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate phone if provided
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Invalid phone format", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void searchCustomers() {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadCustomers(); // If search is empty, load all customers
            return;
        }
        
        try {
            // Search customers
            customers = CustomerService.getInstance().searchCustomers(searchTerm);
            
            // Clear the table
            tableModel.setRowCount(0);
            
            // Add customers to table
            for (Customer customer : customers) {
                addCustomerToTable(customer);
            }
            
            // Clear selection
            selectedCustomer = null;
            clearForm();
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching customers: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addCustomer() {
        editMode = false;
        selectedCustomer = null;
        clearForm();
        setFormEnabled(true);
        
        // Focus on first field
        fullNameField.requestFocus();
    }
    
    private void editCustomer() {
        if (selectedCustomer != null) {
            editMode = true;
            setFormEnabled(true);
            
            // Focus on first field
            fullNameField.requestFocus();
        }
    }
    
    private void deleteCustomer() {
        if (selectedCustomer != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the customer: " + selectedCustomer.getFullName() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                try {
                    boolean success = CustomerService.getInstance().deleteCustomer(selectedCustomer.getCustomerId());
                    
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Customer deleted successfully");
                        
                        // Reload customers
                        loadCustomers();
                        
                        // Clear selection
                        selectedCustomer = null;
                        clearForm();
                        editButton.setEnabled(false);
                        deleteButton.setEnabled(false);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete customer",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void saveCustomer() {
        if (!validateForm()) {
            return;
        }
        
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressArea.getText().trim();
        
        try {
            if (editMode && selectedCustomer != null) {
                // Update existing customer
                selectedCustomer.setFullName(fullName);
                selectedCustomer.setPhone(phone);
                selectedCustomer.setEmail(email);
                selectedCustomer.setAddress(address);
                
                boolean success = CustomerService.getInstance().updateCustomer(selectedCustomer);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Customer updated successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update customer",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Create new customer
                Customer newCustomer = new Customer();
                newCustomer.setFullName(fullName);
                newCustomer.setPhone(phone);
                newCustomer.setEmail(email);
                newCustomer.setAddress(address);
                
                boolean success = CustomerService.getInstance().addCustomer(newCustomer);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Customer created successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create customer",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            
            // Reload customers
            loadCustomers();
            
            // Reset form
            setFormEnabled(false);
            clearForm();
            selectedCustomer = null;
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving customer: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cancelEdit() {
        setFormEnabled(false);
        
        if (selectedCustomer != null) {
            // Restore form to selected customer values
            selectCustomer(customers.indexOf(selectedCustomer));
        } else {
            clearForm();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        switch (command) {
            case "add":
                addCustomer();
                break;
            case "edit":
                editCustomer();
                break;
            case "delete":
                deleteCustomer();
                break;
            case "save":
                saveCustomer();
                break;
            case "cancel":
                cancelEdit();
                break;
            case "search":
                searchCustomers();
                break;
        }
    }
}