package com.foodforall.ui;

import com.foodforall.model.Product;
import com.foodforall.model.User;
import com.foodforall.service.AuthenticationService;
import com.foodforall.service.ProductService;
import com.foodforall.util.Constants;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class DashboardUI extends JFrame implements ActionListener {
    
    private JPanel contentPanel;
    private JButton usersButton;
    private JButton customersButton;
    private JButton productsButton;
    private JButton salesButton;
    private JButton reportsButton;
    private JButton logoutButton;
    private JLabel welcomeLabel;
    private JTable productTable;
    private DefaultTableModel productTableModel;
    
    private User currentUser;
    
    public DashboardUI() {
        currentUser = AuthenticationService.getInstance().getCurrentUser();
        
        setTitle(Constants.APP_NAME);
        setSize(Constants.DASHBOARD_WIDTH, Constants.DASHBOARD_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
        loadProducts();
        
        setVisible(true);
    }
    
    private void initComponents() {
        welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName());
        welcomeLabel.setFont(Constants.TITLE_FONT);
        
        usersButton = new JButton("Users");
        usersButton.setActionCommand("users");
        usersButton.addActionListener(this);
        usersButton.setEnabled(currentUser.isAdmin());
        
        customersButton = new JButton("Customers");
        customersButton.setActionCommand("customers");
        customersButton.addActionListener(this);
        
        productsButton = new JButton("Products");
        productsButton.setActionCommand("products");
        productsButton.addActionListener(this);
        
        salesButton = new JButton("Sales");
        salesButton.setActionCommand("sales");
        salesButton.addActionListener(this);
        
        reportsButton = new JButton("Reports");
        reportsButton.setActionCommand("reports");
        reportsButton.addActionListener(this);
        reportsButton.setEnabled(currentUser.isAdmin());
        
        logoutButton = new JButton("Logout");
        logoutButton.setActionCommand(Constants.CMD_LOGOUT);
        logoutButton.addActionListener(this);
        
        // Product Table
        String[] columns = {"ID", "Name", "Description", "Price", "Available"};
        productTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productTableModel);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        
        contentPanel = new JPanel();
    }
    
    private void layoutComponents() {
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        // Button panel on top
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(usersButton);
        buttonPanel.add(customersButton);
        buttonPanel.add(productsButton);
        buttonPanel.add(salesButton);
        buttonPanel.add(reportsButton);
        buttonPanel.add(logoutButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Content panel in center
        contentPanel.setLayout(new BorderLayout());
        
        // Product list panel
        JPanel productListPanel = new JPanel(new BorderLayout());
        JLabel productListLabel = new JLabel("Available Products");
        productListLabel.setFont(Constants.SUBTITLE_FONT);
        productListPanel.add(productListLabel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        productListPanel.add(scrollPane, BorderLayout.CENTER);
        
        contentPanel.add(productListPanel, BorderLayout.CENTER);
        
        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Status panel at bottom
        JPanel statusPanel = new JPanel(new BorderLayout());
        JLabel statusLabel = new JLabel(Constants.COPYRIGHT);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Set main panel as content pane
        setContentPane(mainPanel);
    }
    
    private void loadProducts() {
        try {
            // Clear the table
            productTableModel.setRowCount(0);
            
            // Get all products
            List<Product> products = ProductService.getInstance().getProductsInStock();
            
            // Add products to table
            for (Product product : products) {
                Object[] row = {
                    product.getProductId(),
                    product.getProductName(),
                    product.getDescription(),
                    "£" + product.getPrice(),
                    product.getCurrentStock()
                };
                productTableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void logout() {
        AuthenticationService.getInstance().logout();
        dispose();
        new LoginUI();
    }
    
    private void openUserManagement() {
        new UserManagementUI(this);
    }
    
    private void openCustomerManagement() {
        new CustomerManagementUI(this);
    }
    
    private void openProductManagement() {
        new StockManagementUI(this);
    }
    
    private void openSalesUI() {
        new SaleUI(this);
    }
    
    private void openReportsUI() {
        new ReportUI(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        switch (command) {
            case "users":
                openUserManagement();
                break;
            case "customers":
                openCustomerManagement();
                break;
            case "products":
                openProductManagement();
                break;
            case "sales":
                openSalesUI();
                break;
            case "reports":
                openReportsUI();
                break;
            case Constants.CMD_LOGOUT:
                logout();
                break;
        }
    }
    
    public void refreshDashboard() {
        loadProducts();
    }
}