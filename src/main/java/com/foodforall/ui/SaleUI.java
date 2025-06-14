// File: com/foodforall/ui/SaleUI.java
package com.foodforall.ui;
import com.foodforall.model.*;
import com.foodforall.service.CustomerService;
import com.foodforall.service.ProductService;
import com.foodforall.service.SaleService;
import com.foodforall.util.Constants;
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

public class SaleUI extends JDialog implements ActionListener {
    
    private DefaultTableModel productTableModel;
    private JTable productTable;
    
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    
    private JComboBox<Customer> customerComboBox;
    private JButton selectCustomerButton;
    private JButton addToCartButton;
    private JButton removeFromCartButton;
    private JButton clearCartButton;
    private JButton checkoutButton;
    private JLabel totalLabel;
    
    private List<Product> products;
    private Sale currentSale;
    private JFrame parentFrame;
    
    public SaleUI(JFrame parent) {
        super(parent, "Sales", true);
        this.parentFrame = parent;
        setSize(900, 700);
        setLocationRelativeTo(parent);
        
        // Initialize current sale
        currentSale = new Sale();
        
        initComponents();
        layoutComponents();
        loadProducts();
        loadCustomers();
        updateTotal();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Product Table
        String[] productColumns = {"ID", "Product Name", "Description", "Price", "Available"};
        productTableModel = new DefaultTableModel(productColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedProductToCart();
                }
            }
        });
        
        // Cart Table
        String[] cartColumns = {"Product", "Price", "Quantity", "Subtotal"};
        cartTableModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Customer Selection
        customerComboBox = new JComboBox<>();
        customerComboBox.setPreferredSize(new Dimension(200, 25));
        
        // Buttons
        selectCustomerButton = new JButton("Select Customer");
        selectCustomerButton.setActionCommand("select_customer");
        selectCustomerButton.addActionListener(this);
        
        addToCartButton = new JButton("Add to Cart");
        addToCartButton.setActionCommand("add_to_cart");
        addToCartButton.addActionListener(this);
        
        removeFromCartButton = new JButton("Remove from Cart");
        removeFromCartButton.setActionCommand("remove_from_cart");
        removeFromCartButton.addActionListener(this);
        
        clearCartButton = new JButton("Clear Cart");
        clearCartButton.setActionCommand("clear_cart");
        clearCartButton.addActionListener(this);
        
        checkoutButton = new JButton("Checkout");
        checkoutButton.setActionCommand("checkout");
        checkoutButton.addActionListener(this);
        checkoutButton.setEnabled(false);
        
        // Total Label
        totalLabel = new JLabel("Total: £0.00");
        totalLabel.setFont(Constants.SUBTITLE_FONT);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // North Panel (Customer Selection)
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBorder(BorderFactory.createTitledBorder("Customer"));
        
        northPanel.add(new JLabel("Customer:"));
        northPanel.add(customerComboBox);
        northPanel.add(selectCustomerButton);
        
        // Center Panel (Split between Products and Cart)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        // Products Panel
        JPanel productsPanel = new JPanel(new BorderLayout());
        productsPanel.setBorder(BorderFactory.createTitledBorder("Available Products"));
        
        JScrollPane productScrollPane = new JScrollPane(productTable);
        productsPanel.add(productScrollPane, BorderLayout.CENTER);
        
        JPanel productButtonPanel = new JPanel();
        productButtonPanel.add(addToCartButton);
        productsPanel.add(productButtonPanel, BorderLayout.SOUTH);
        
        // Cart Panel
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));
        
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartPanel.add(cartScrollPane, BorderLayout.CENTER);
        
        JPanel cartButtonPanel = new JPanel();
        cartButtonPanel.add(removeFromCartButton);
        cartButtonPanel.add(clearCartButton);
        cartPanel.add(cartButtonPanel, BorderLayout.SOUTH);
        
        // Add panels to split pane
        splitPane.setTopComponent(productsPanel);
        splitPane.setBottomComponent(cartPanel);
        splitPane.setDividerLocation(300);
        
        // South Panel (Total and Checkout)
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(totalLabel);
        
        JPanel checkoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        checkoutPanel.add(checkoutButton);
        
        southPanel.add(totalPanel, BorderLayout.CENTER);
        southPanel.add(checkoutPanel, BorderLayout.SOUTH);
        
        // Add all panels to main panel
        add(northPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }
    
    private void loadProducts() {
        try {
            // Clear the table
            productTableModel.setRowCount(0);
            
            // Get products in stock
            products = ProductService.getInstance().getProductsInStock();
            
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
    
    private void loadCustomers() {
        try {
            // Clear the combo box
            customerComboBox.removeAllItems();
            
            // Add default "no customer" option
            customerComboBox.addItem(null);
            
            // Get all customers
            List<Customer> customers = CustomerService.getInstance().getAllCustomers();
            
            // Add customers to combo box
            for (Customer customer : customers) {
                customerComboBox.addItem(customer);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addSelectedProductToCart() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            Product product = products.get(selectedRow);
            
            // Check if there's enough stock
            try {
                if (!ProductService.getInstance().hasEnoughStock(product.getProductId(), 1)) {
                    JOptionPane.showMessageDialog(this, "Not enough stock available",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error checking stock: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Ask for quantity
            String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:", "1");
            if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    
                    if (quantity <= 0) {
                        JOptionPane.showMessageDialog(this, "Quantity must be positive",
                                "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Check if there's enough stock for the requested quantity
                    try {
                        if (!ProductService.getInstance().hasEnoughStock(product.getProductId(), quantity)) {
                            JOptionPane.showMessageDialog(this, "Not enough stock available",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error checking stock: " + e.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Add to current sale
                    SaleItem item = new SaleItem(product, quantity);
                    currentSale.addItem(item);
                    
                    // Update cart display
                    updateCart();
                    
                    // Update total
                    updateTotal();
                    
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void removeSelectedItemFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow >= 0) {
            currentSale.removeItem(selectedRow);
            
            // Update cart display
            updateCart();
            
            // Update total
            updateTotal();
        }
    }
    
    private void clearCart() {
        currentSale.clear();
        
        // Update cart display
        updateCart();
        
        // Update total
        updateTotal();
    }
    
    private void updateCart() {
        // Clear the cart table
        cartTableModel.setRowCount(0);
        
        // Add items to cart table
        for (SaleItem item : currentSale.getItems()) {
            Object[] row = {
                item.getProduct().getProductName(),
                "£" + item.getUnitPrice(),
                item.getQuantity(),
                "£" + item.getSubtotal()
            };
            cartTableModel.addRow(row);
        }
        
        // Enable/disable checkout button
        checkoutButton.setEnabled(!currentSale.getItems().isEmpty());
    }
    
    private void updateTotal() {
        totalLabel.setText("Total: £" + currentSale.getTotalAmount());
    }
    
    private void selectCustomer() {
        Customer selectedCustomer = (Customer) customerComboBox.getSelectedItem();
        currentSale.setCustomer(selectedCustomer);
    }
    
    private void checkout() {
        if (currentSale.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show checkout dialog
        new CheckoutDialog(this, currentSale);
        
        // If payment was successful, sale will be cleared
        // so check if cart is empty
        if (currentSale.getItems().isEmpty()) {
            // Reload products to update stock levels
            loadProducts();
            
            // Refresh dashboard if parent is dashboard
            if (parentFrame instanceof DashboardUI) {
                ((DashboardUI) parentFrame).refreshDashboard();
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        switch (command) {
            case "select_customer":
                selectCustomer();
                break;
            case "add_to_cart":
                addSelectedProductToCart();
                break;
            case "remove_from_cart":
                removeSelectedItemFromCart();
                break;
            case "clear_cart":
                clearCart();
                break;
            case "checkout":
                checkout();
                break;
        }
    }
    
    // Inner class for checkout dialog
    private class CheckoutDialog extends JDialog implements ActionListener {
        private Sale sale;
        private JComboBox<PaymentType> paymentTypeComboBox;
        private JTextField amountField;
        private JLabel changeLabel;
        private JButton processButton;
        private JButton cancelButton;
        
        public CheckoutDialog(JDialog parent, Sale sale) {
            super(parent, "Checkout", true);
            this.sale = sale;
            
            setSize(400, 300);
            setLocationRelativeTo(parent);
            
            initComponents();
            layoutComponents();
            
            setVisible(true);
        }
        
        private void initComponents() {
            // Payment Type
            paymentTypeComboBox = new JComboBox<>();
            try {
                List<PaymentType> paymentTypes = SaleService.getInstance().getAllPaymentTypes();
                for (PaymentType type : paymentTypes) {
                    paymentTypeComboBox.addItem(type);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading payment types: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            paymentTypeComboBox.addActionListener(e -> updateChangeLabel());
            
            // Amount Field
            amountField = new JTextField(10);
            amountField.setText(sale.getTotalAmount().toString());
            amountField.addActionListener(e -> updateChangeLabel());
            
            // Change Label
            changeLabel = new JLabel("Change: £0.00");
            
            // Buttons
            processButton = new JButton("Process Payment");
            processButton.setActionCommand("process");
            processButton.addActionListener(this);
            
            cancelButton = new JButton("Cancel");
            cancelButton.setActionCommand("cancel");
            cancelButton.addActionListener(this);
        }
        
        private void layoutComponents() {
            setLayout(new BorderLayout(10, 10));
            
            // Main panel
            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Sale Total
            gbc.gridx = 0;
            gbc.gridy = 0;
            mainPanel.add(new JLabel("Sale Total:"), gbc);
            
            gbc.gridx = 1;
            mainPanel.add(new JLabel("£" + sale.getTotalAmount()), gbc);
            
            // Payment Type
            gbc.gridx = 0;
            gbc.gridy = 1;
            mainPanel.add(new JLabel("Payment Type:"), gbc);
            
            gbc.gridx = 1;
            mainPanel.add(paymentTypeComboBox, gbc);
            
            // Amount Paid
            gbc.gridx = 0;
            gbc.gridy = 2;
            mainPanel.add(new JLabel("Amount Paid:"), gbc);
            
            gbc.gridx = 1;
            mainPanel.add(amountField, gbc);
            
            // Change
            gbc.gridx = 0;
            gbc.gridy = 3;
            mainPanel.add(new JLabel("Change:"), gbc);
            
            gbc.gridx = 1;
            mainPanel.add(changeLabel, gbc);
            
            // Button panel
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(processButton);
            buttonPanel.add(cancelButton);
            
            // Add panels to main layout
            add(mainPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
            
            // Initial update
            updateChangeLabel();
        }
        
        private void updateChangeLabel() {
            try {
                BigDecimal amountPaid = new BigDecimal(amountField.getText());
                BigDecimal totalDue = sale.getTotalAmount();
                
                // Only calculate change for cash payments
                PaymentType selectedType = (PaymentType) paymentTypeComboBox.getSelectedItem();
                if (selectedType != null && selectedType.getName().equals("Cash")) {
                    if (amountPaid.compareTo(totalDue) >= 0) {
                        BigDecimal change = amountPaid.subtract(totalDue);
                        changeLabel.setText("Change: £" + change);
                        
                        // Set foreground to normal color
                        changeLabel.setForeground(Color.BLACK);
                        
                        // Enable process button
                        processButton.setEnabled(true);
                    } else {
                        changeLabel.setText("Insufficient amount");
                        
                        // Set foreground to error color
                        changeLabel.setForeground(Color.RED);
                        
                        // Disable process button
                        processButton.setEnabled(false);
                    }
                } else {
                    // For card payments, no change is calculated
                    changeLabel.setText("Change: N/A");
                    
                    // Set foreground to normal color
                    changeLabel.setForeground(Color.BLACK);
                    
                    // Enable process button if amount is sufficient
                    processButton.setEnabled(amountPaid.compareTo(totalDue) >= 0);
                }
            } catch (NumberFormatException e) {
                changeLabel.setText("Invalid amount");
                
                // Set foreground to error color
                changeLabel.setForeground(Color.RED);
                
                // Disable process button
                processButton.setEnabled(false);
            }
        }
        
        private void processPayment() {
            try {
                BigDecimal amountPaid = new BigDecimal(amountField.getText());
                PaymentType selectedType = (PaymentType) paymentTypeComboBox.getSelectedItem();
                
                if (selectedType == null) {
                    JOptionPane.showMessageDialog(this, "Please select a payment type",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // For cash payments, check if amount is sufficient
                if (selectedType.getName().equals("Cash") && amountPaid.compareTo(sale.getTotalAmount()) < 0) {
                    JOptionPane.showMessageDialog(this, "Insufficient amount paid",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Process the sale
                try {
                    boolean success = SaleService.getInstance().processSale(sale, selectedType, amountPaid);
                    
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Sale processed successfully");
                        
                        // Clear the sale
                        currentSale.clear();
                        
                        // Close the dialog
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to process sale",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error processing sale: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid amount",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            
            if ("process".equals(command)) {
                processPayment();
            } else if ("cancel".equals(command)) {
                dispose();
            }
        }
    }
}