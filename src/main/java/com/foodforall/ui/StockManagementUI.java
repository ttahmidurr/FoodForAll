package com.foodforall.ui;
import com.foodforall.model.Product;
import com.foodforall.service.ProductService;
import com.foodforall.util.ValidationUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class StockManagementUI extends JDialog implements ActionListener {
    
    private DefaultTableModel tableModel;
    private JTable productTable;
    
    private JTextField productNameField;
    private JTextArea descriptionArea;
    private JTextField priceField;
    private JTextField stockField;
    
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton adjustStockButton;
    
    private List<Product> products;
    private Product selectedProduct;
    private boolean editMode = false;
    private JFrame parentFrame;
    
    public StockManagementUI(JFrame parent) {
        super(parent, "Stock Management", true);
        this.parentFrame = parent;
        setSize(800, 600);
        setLocationRelativeTo(parent);
        
        initComponents();
        layoutComponents();
        loadProducts();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Table
        String[] columns = {"ID", "Product Name", "Description", "Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectProduct(selectedRow);
                }
            }
        });
        
        // Form Fields
        productNameField = new JTextField(20);
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        priceField = new JTextField(20);
        stockField = new JTextField(20);
        
        // Buttons
        addButton = new JButton("Add Product");
        addButton.setActionCommand("add");
        addButton.addActionListener(this);
        
        editButton = new JButton("Edit Product");
        editButton.setActionCommand("edit");
        editButton.addActionListener(this);
        editButton.setEnabled(false);
        
        deleteButton = new JButton("Delete Product");
        deleteButton.setActionCommand("delete");
        deleteButton.addActionListener(this);
        deleteButton.setEnabled(false);
        
        adjustStockButton = new JButton("Adjust Stock");
        adjustStockButton.setActionCommand("adjust");
        adjustStockButton.addActionListener(this);
        adjustStockButton.setEnabled(false);
        
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
        tablePanel.setBorder(BorderFactory.createTitledBorder("Product List"));
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button Panel for Table
        JPanel tableButtonPanel = new JPanel();
        tableButtonPanel.add(addButton);
        tableButtonPanel.add(editButton);
        tableButtonPanel.add(deleteButton);
        tableButtonPanel.add(adjustStockButton);
        
        tablePanel.add(tableButtonPanel, BorderLayout.SOUTH);
        
        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        formPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Product Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Product Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(productNameField, gbc);
        
        // Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        formPanel.add(descScrollPane, gbc);
        
        // Price
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Price (£):"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(priceField, gbc);
        
        // Stock
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Stock:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(stockField, gbc);
        
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
    
    private void loadProducts() {
        try {
            // Clear the table
            tableModel.setRowCount(0);
            
            // Get all products
            products = ProductService.getInstance().getAllProducts();
            
            // Add products to table
            for (Product product : products) {
                addProductToTable(product);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addProductToTable(Product product) {
        Object[] row = {
            product.getProductId(),
            product.getProductName(),
            product.getDescription(),
            "£" + product.getPrice(),
            product.getCurrentStock()
        };
        tableModel.addRow(row);
    }
    
    private void selectProduct(int row) {
        selectedProduct = products.get(row);
        
        // Set form values
        productNameField.setText(selectedProduct.getProductName());
        descriptionArea.setText(selectedProduct.getDescription());
        priceField.setText(selectedProduct.getPrice().toString());
        stockField.setText(String.valueOf(selectedProduct.getCurrentStock()));
        
        // Enable edit, delete, and adjust buttons
        editButton.setEnabled(true);
        deleteButton.setEnabled(true);
        adjustStockButton.setEnabled(true);
    }
    
    private void clearForm() {
        productNameField.setText("");
        descriptionArea.setText("");
        priceField.setText("");
        stockField.setText("");
    }
    
    private void setFormEnabled(boolean enabled) {
        productNameField.setEnabled(enabled);
        descriptionArea.setEnabled(enabled);
        priceField.setEnabled(enabled);
        stockField.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        
        // Disable other buttons when form is active
        addButton.setEnabled(!enabled);
        editButton.setEnabled(!enabled && selectedProduct != null);
        deleteButton.setEnabled(!enabled && selectedProduct != null);
        adjustStockButton.setEnabled(!enabled && selectedProduct != null);
        productTable.setEnabled(!enabled);
    }
    
    private boolean validateForm() {
        // Check required fields
        if (productNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product Name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate price
        if (priceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Price is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!ValidationUtil.isPositiveNumber(priceField.getText())) {
            JOptionPane.showMessageDialog(this, "Price must be a positive number", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate stock
        if (stockField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Stock is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!ValidationUtil.isPositiveInteger(stockField.getText())) {
            JOptionPane.showMessageDialog(this, "Stock must be a positive integer", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void addProduct() {
        editMode = false;
        selectedProduct = null;
        clearForm();
        setFormEnabled(true);
        
        // Focus on first field
        productNameField.requestFocus();
    }
    
    private void editProduct() {
        if (selectedProduct != null) {
            editMode = true;
            setFormEnabled(true);
            
            // Focus on first field
            productNameField.requestFocus();
        }
    }
    
    private void adjustStock() {
        if (selectedProduct != null) {
            String input = JOptionPane.showInputDialog(this,
                    "Enter adjustment amount (use negative value to reduce stock):",
                    "Adjust Stock", JOptionPane.QUESTION_MESSAGE);
            
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int adjustment = Integer.parseInt(input);
                    
                    // Ensure we don't go below zero
                    if (selectedProduct.getCurrentStock() + adjustment < 0) {
                        JOptionPane.showMessageDialog(this,
                                "Adjustment would result in negative stock. Maximum reduction: " + selectedProduct.getCurrentStock(),
                                "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    boolean success = ProductService.getInstance().adjustStock(selectedProduct.getProductId(), adjustment);
                    
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Stock adjusted successfully");
                        
                        // Reload products to update the display
                        loadProducts();
                        
                        // Clear selection
                        selectedProduct = null;
                        clearForm();
                        editButton.setEnabled(false);
                        deleteButton.setEnabled(false);
                        adjustStockButton.setEnabled(false);
                        
                        // Refresh dashboard
                        if (parentFrame instanceof DashboardUI) {
                            ((DashboardUI) parentFrame).refreshDashboard();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to adjust stock",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid integer",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error adjusting stock: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void deleteProduct() {
        if (selectedProduct != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the product: " + selectedProduct.getProductName() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                try {
                    boolean success = ProductService.getInstance().deleteProduct(selectedProduct.getProductId());
                    
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Product deleted successfully");
                        
                        // Reload products
                        loadProducts();
                        
                        // Clear selection
                        selectedProduct = null;
                        clearForm();
                        editButton.setEnabled(false);
                        deleteButton.setEnabled(false);
                        adjustStockButton.setEnabled(false);
                        
                        // Refresh dashboard
                        if (parentFrame instanceof DashboardUI) {
                            ((DashboardUI) parentFrame).refreshDashboard();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete product",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void saveProduct() {
        if (!validateForm()) {
            return;
        }
        
        String productName = productNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        BigDecimal price = new BigDecimal(priceField.getText().trim());
        int stock = Integer.parseInt(stockField.getText().trim());
        
        try {
            if (editMode && selectedProduct != null) {
                // Update existing product
                selectedProduct.setProductName(productName);
                selectedProduct.setDescription(description);
                selectedProduct.setPrice(price);
                
                boolean success = ProductService.getInstance().updateProduct(selectedProduct);
                
                // Update stock separately
                if (success && selectedProduct.getCurrentStock() != stock) {
                    success &= ProductService.getInstance().updateStock(selectedProduct.getProductId(), stock);
                }
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Product updated successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update product",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Create new product
                Product newProduct = new Product();
                newProduct.setProductName(productName);
                newProduct.setDescription(description);
                newProduct.setPrice(price);
                newProduct.setCurrentStock(stock);
                
                boolean success = ProductService.getInstance().addProduct(newProduct);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Product created successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create product",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            
            // Reload products
            loadProducts();
            
            // Reset form
            setFormEnabled(false);
            clearForm();
            selectedProduct = null;
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            adjustStockButton.setEnabled(false);
            
            // Refresh dashboard
            if (parentFrame instanceof DashboardUI) {
                ((DashboardUI) parentFrame).refreshDashboard();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving product: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cancelEdit() {
        setFormEnabled(false);
        
        if (selectedProduct != null) {
            // Restore form to selected product values
            selectProduct(products.indexOf(selectedProduct));
        } else {
            clearForm();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        switch (command) {
            case "add":
                addProduct();
                break;
            case "edit":
                editProduct();
                break;
            case "delete":
                deleteProduct();
                break;
            case "adjust":
                adjustStock();
                break;
            case "save":
                saveProduct();
                break;
            case "cancel":
                cancelEdit();
                break;
        }
    }
}