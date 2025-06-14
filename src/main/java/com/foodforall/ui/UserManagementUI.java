package com.foodforall.ui;
import com.foodforall.model.Role;
import com.foodforall.model.User;
import com.foodforall.service.UserService;
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

public class UserManagementUI extends JDialog implements ActionListener {
    
    private DefaultTableModel tableModel;
    private JTable userTable;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField fullNameField;
    private JComboBox<Role> roleComboBox;
    
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton cancelButton;
    
    private List<Role> roles;
    private List<User> users;
    private User selectedUser;
    private boolean editMode = false;
    
    public UserManagementUI(JFrame parent) {
        super(parent, "User Management", true);
        setSize(700, 500);
        setLocationRelativeTo(parent);
        
        try {
            roles = UserService.getInstance().getAllRoles();
            initComponents();
            layoutComponents();
            loadUsers();
            
            setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading roles: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
    
    private void initComponents() {
        // Table
        String[] columns = {"ID", "Username", "Full Name", "Role", "Created", "Last Login"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectUser(selectedRow);
                }
            }
        });
        
        // Form Fields
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        fullNameField = new JTextField(20);
        roleComboBox = new JComboBox<>();
        for (Role role : roles) {
            roleComboBox.addItem(role);
        }
        
        // Buttons
        addButton = new JButton("Add User");
        addButton.setActionCommand("add");
        addButton.addActionListener(this);
        
        editButton = new JButton("Edit User");
        editButton.setActionCommand("edit");
        editButton.addActionListener(this);
        editButton.setEnabled(false);
        
        deleteButton = new JButton("Delete User");
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
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("User List"));
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button Panel for Table
        JPanel tableButtonPanel = new JPanel();
        tableButtonPanel.add(addButton);
        tableButtonPanel.add(editButton);
        tableButtonPanel.add(deleteButton);
        
        tablePanel.add(tableButtonPanel, BorderLayout.SOUTH);
        
        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setBorder(BorderFactory.createTitledBorder("User Details"));
        formPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordField, gbc);
        
        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Full Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(fullNameField, gbc);
        
        // Role
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Role:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(roleComboBox, gbc);
        
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
        add(tablePanel, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);
        
        // Disable form fields initially
        setFormEnabled(false);
    }
    
    private void loadUsers() {
        try {
            // Clear the table
            tableModel.setRowCount(0);
            
            // Get all users
            users = UserService.getInstance().getAllUsers();
            
            // Add users to table
            for (User user : users) {
                Object[] row = {
                    user.getUserId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getRole().getRoleName(),
                    user.getCreatedAt(),
                    user.getLastLogin()
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void selectUser(int row) {
        selectedUser = users.get(row);
        
        // Set form values
        usernameField.setText(selectedUser.getUsername());
        passwordField.setText("");  // Don't show password
        fullNameField.setText(selectedUser.getFullName());
        
        // Find and select the user's role in the combo box
        for (int i = 0; i < roleComboBox.getItemCount(); i++) {
            Role role = roleComboBox.getItemAt(i);
            if (role.getRoleId() == selectedUser.getRole().getRoleId()) {
                roleComboBox.setSelectedIndex(i);
                break;
            }
        }
        
        // Enable edit and delete buttons
        editButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }
    
    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        fullNameField.setText("");
        if (roleComboBox.getItemCount() > 0) {
            roleComboBox.setSelectedIndex(0);
        }
    }
    
    private void setFormEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        fullNameField.setEnabled(enabled);
        roleComboBox.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        
        // Disable other buttons when form is active
        addButton.setEnabled(!enabled);
        editButton.setEnabled(!enabled && selectedUser != null);
        deleteButton.setEnabled(!enabled && selectedUser != null);
        userTable.setEnabled(!enabled);
    }
    
    private boolean validateForm() {
        // Check all fields are filled
        if (usernameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!editMode && passwordField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Password is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (fullNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate username
        if (!ValidationUtil.isValidUsername(usernameField.getText())) {
            JOptionPane.showMessageDialog(this, "Username must be at least 3 characters with no spaces", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate password if provided
        String password = new String(passwordField.getPassword());
        if (!password.isEmpty() && !ValidationUtil.isStrongPassword(password)) {
            JOptionPane.showMessageDialog(this, 
                    "Password must be at least 8 characters and include uppercase, lowercase, digit, and special character", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void addUser() {
        editMode = false;
        selectedUser = null;
        clearForm();
        setFormEnabled(true);
        
        // Focus on first field
        usernameField.requestFocus();
    }
    
    private void editUser() {
        if (selectedUser != null) {
            editMode = true;
            setFormEnabled(true);
            
            // Focus on name field
            fullNameField.requestFocus();
        }
    }
    
    private void deleteUser() {
        if (selectedUser != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the user: " + selectedUser.getUsername() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                try {
                    boolean success = UserService.getInstance().deleteUser(selectedUser.getUserId());
                    
                    if (success) {
                        JOptionPane.showMessageDialog(this, "User deleted successfully");
                        
                        // Reload users
                        loadUsers();
                        
                        // Clear selection
                        selectedUser = null;
                        clearForm();
                        editButton.setEnabled(false);
                        deleteButton.setEnabled(false);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete user",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void saveUser() {
        if (!validateForm()) {
            return;
        }
        
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String fullName = fullNameField.getText();
        Role role = (Role) roleComboBox.getSelectedItem();
        
        try {
            if (editMode && selectedUser != null) {
                // Update existing user
                selectedUser.setUsername(username);
                selectedUser.setFullName(fullName);
                selectedUser.setRole(role);
                
                boolean success = UserService.getInstance().updateUser(selectedUser);
                
                // Update password if provided
                if (!password.isEmpty()) {
                    success &= UserService.getInstance().updateUserPassword(selectedUser.getUserId(), password);
                }
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "User updated successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update user",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Create new user
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setFullName(fullName);
                newUser.setRole(role);
                
                boolean success = UserService.getInstance().addUser(newUser, password);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "User created successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create user",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            
            // Reload users
            loadUsers();
            
            // Reset form
            setFormEnabled(false);
            clearForm();
            selectedUser = null;
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving user: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cancelEdit() {
        setFormEnabled(false);
        
        if (selectedUser != null) {
            // Restore form to selected user values
            selectUser(users.indexOf(selectedUser));
        } else {
            clearForm();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        switch (command) {
            case "add":
                addUser();
                break;
            case "edit":
                editUser();
                break;
            case "delete":
                deleteUser();
                break;
            case "save":
                saveUser();
                break;
            case "cancel":
                cancelEdit();
                break;
        }
    }
}