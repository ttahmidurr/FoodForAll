// File: com/foodforall/ui/ReportUI.java
package com.foodforall.ui;
import com.foodforall.model.Product;
import com.foodforall.model.Report;
import com.foodforall.model.Sale;
import com.foodforall.service.ReportService;
import com.foodforall.util.Constants;
import com.foodforall.util.DateUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportUI extends JDialog implements ActionListener {
    
    private JComboBox<String> reportTypeComboBox;
    private JPanel datePanel;
    private JComboBox<String> dateRangeComboBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton generateButton;
    private JButton exportButton;
    private JTabbedPane reportTabbedPane;
    
    private JPanel salesReportPanel;
    private JPanel stockReportPanel;
    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    
    public ReportUI(JFrame parent) {
        super(parent, "Reports", true);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        
        initComponents();
        layoutComponents();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Report Type
        reportTypeComboBox = new JComboBox<>(new String[]{"Sales Report", "Stock Report"});
        reportTypeComboBox.addActionListener(e -> updateUIForReportType());
        
        // Date Range
        dateRangeComboBox = new JComboBox<>(new String[]{"Today", "Yesterday", "This Week", "This Month", "Custom"});
        dateRangeComboBox.addActionListener(e -> updateDateFields());
        
        startDateField = new JTextField(10);
        endDateField = new JTextField(10);
        
        // Set default dates
        setDefaultDates();
        
        // Generate Button
        generateButton = new JButton("Generate Report");
        generateButton.setActionCommand("generate");
        generateButton.addActionListener(this);
        
        // Export Button
        exportButton = new JButton("Export Report");
        exportButton.setActionCommand("export");
        exportButton.addActionListener(this);
        exportButton.setEnabled(false);
        
        // Report Tab Pane
        reportTabbedPane = new JTabbedPane();
        
        // Sales Report Panel
        salesReportPanel = new JPanel(new BorderLayout());
        
        // Stock Report Panel
        stockReportPanel = new JPanel(new BorderLayout());
        
        // Report Table
        String[] columns = {"Date", "Transactions", "Revenue"};
        reportTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new JTable(reportTableModel);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Controls Panel
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BorderLayout());
        
        // Report Type Panel
        JPanel reportTypePanel = new JPanel();
        reportTypePanel.setBorder(BorderFactory.createTitledBorder("Report Type"));
        reportTypePanel.add(new JLabel("Type:"));
        reportTypePanel.add(reportTypeComboBox);
        
        // Date Panel
        datePanel = new JPanel();
        datePanel.setBorder(BorderFactory.createTitledBorder("Date Range"));
        datePanel.add(new JLabel("Range:"));
        datePanel.add(dateRangeComboBox);
        datePanel.add(new JLabel("Start:"));
        datePanel.add(startDateField);
        datePanel.add(new JLabel("End:"));
        datePanel.add(endDateField);
        
        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(generateButton);
        buttonPanel.add(exportButton);
        
        // Add to controls panel
        controlsPanel.add(reportTypePanel, BorderLayout.NORTH);
        controlsPanel.add(datePanel, BorderLayout.CENTER);
        controlsPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add initial tabs to report pane
        reportTabbedPane.addTab("Report", new JScrollPane());
        
        // Add to main layout
        add(controlsPanel, BorderLayout.NORTH);
        add(reportTabbedPane, BorderLayout.CENTER);
        
        // Initial UI update
        updateUIForReportType();
    }
    
    private void setDefaultDates() {
        LocalDate today = LocalDate.now();
        startDateField.setText(DateUtil.formatDate(today));
        endDateField.setText(DateUtil.formatDate(today));
    }
    
    private void updateUIForReportType() {
        String selectedType = (String) reportTypeComboBox.getSelectedItem();
        
        if ("Stock Report".equals(selectedType)) {
            datePanel.setVisible(false);
        } else {
            datePanel.setVisible(true);
        }
    }
    
    private void updateDateFields() {
        String selectedRange = (String) dateRangeComboBox.getSelectedItem();
        LocalDate start = null;
        LocalDate end = LocalDate.now();
        
        switch (selectedRange) {
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
            case "Custom":
                // Don't change the dates
                return;
        }
        
        if (start != null) {
            startDateField.setText(DateUtil.formatDate(start));
            endDateField.setText(DateUtil.formatDate(end));
        }
    }
    
    private void generateReport() {
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        
        try {
            if ("Sales Report".equals(reportType)) {
                generateSalesReport();
            } else if ("Stock Report".equals(reportType)) {
                generateStockReport();
            }
            
            // Enable export button
            exportButton.setEnabled(true);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateSalesReport() throws SQLException {
        // Validate dates
        LocalDate startDate;
        LocalDate endDate;
        
        try {
            startDate = DateUtil.parseDate(startDateField.getText());
            endDate = DateUtil.parseDate(endDateField.getText());
            
            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(this, "Please enter valid dates",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this, "Start date cannot be after end date",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter dates in the format YYYY-MM-DD",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Generate report
        Report report = ReportService.getInstance().generateSalesReport(startDate, endDate);
        
        // Display report
        displaySalesReport(report);
    }
    
    private void generateStockReport() throws SQLException {
        // Generate report
        Report report = ReportService.getInstance().generateStockReport();
        
        // Display report
        displayStockReport(report);
    }
    
    private void displaySalesReport(Report report) {
        // Create a new panel for the report
        JPanel reportPanel = new JPanel(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Sales Report");
        titleLabel.setFont(Constants.TITLE_FONT);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        infoPanel.add(new JLabel("Period: " + DateUtil.formatDisplayDate(report.getStartDate()) +
                " to " + DateUtil.formatDisplayDate(report.getEndDate())));
        infoPanel.add(new JLabel("Total Revenue: £" + report.getTotalRevenue()));
        infoPanel.add(new JLabel("Total Transactions: " + report.getTotalTransactions()));
        headerPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Create tabbed pane for report sections
        JTabbedPane reportSections = new JTabbedPane();
        
        // Summary Tab
        JPanel summaryPanel = new JPanel(new BorderLayout());
        
        // Create table for daily sales
        DefaultTableModel dailySalesModel = new DefaultTableModel(
                new String[]{"Date", "Transactions", "Revenue"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable dailySalesTable = new JTable(dailySalesModel);
        
        // Add daily sales data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dailySales = (List<Map<String, Object>>) report.getAdditionalData().get("dailySales");
        if (dailySales != null) {
            for (Map<String, Object> daySale : dailySales) {
                LocalDate date = (LocalDate) daySale.get("date");
                Integer count = (Integer) daySale.get("transactionCount");
                BigDecimal amount = (BigDecimal) daySale.get("totalAmount");
                
                Object[] row = {
                    DateUtil.formatDisplayDate(date),
                    count,
                    "£" + amount
                };
                dailySalesModel.addRow(row);
            }
        }
        
        summaryPanel.add(new JScrollPane(dailySalesTable), BorderLayout.CENTER);
        
        // Top Products Tab
        JPanel topProductsPanel = new JPanel(new BorderLayout());
        
        // Create table for top products
        DefaultTableModel topProductsModel = new DefaultTableModel(
                new String[]{"Product", "Quantity Sold", "Revenue"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable topProductsTable = new JTable(topProductsModel);
        
        // Add top products data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topProducts = (List<Map<String, Object>>) report.getAdditionalData().get("topProducts");
        if (topProducts != null) {
            for (Map<String, Object> product : topProducts) {
                String name = (String) product.get("productName");
                Integer quantity = (Integer) product.get("totalQuantity");
                BigDecimal sales = (BigDecimal) product.get("totalSales");
                
                Object[] row = {
                    name,
                    quantity,
                    "£" + sales
                };
                topProductsModel.addRow(row);
            }
        }
        
        topProductsPanel.add(new JScrollPane(topProductsTable), BorderLayout.CENTER);
        
        // Transactions Tab
        JPanel transactionsPanel = new JPanel(new BorderLayout());
        
        // Create table for transactions
        DefaultTableModel transactionsModel = new DefaultTableModel(
                new String[]{"ID", "Date", "Customer", "Items", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable transactionsTable = new JTable(transactionsModel);
        
        // Add transactions data
        List<Sale> sales = report.getSales();
        if (sales != null) {
            for (Sale sale : sales) {
                String customerName = sale.getCustomer() != null ? sale.getCustomer().getFullName() : "Walk-in";
                
                Object[] row = {
                    sale.getSaleId(),
                    DateUtil.formatDisplayDateTime(sale.getSaleDate()),
                    customerName,
                    sale.getItems().size(),
                    "£" + sale.getTotalAmount()
                };
                transactionsModel.addRow(row);
            }
        }
        
        transactionsPanel.add(new JScrollPane(transactionsTable), BorderLayout.CENTER);
        
        // Add tabs to the report sections
        reportSections.addTab("Daily Summary", summaryPanel);
        reportSections.addTab("Top Products", topProductsPanel);
        reportSections.addTab("Transactions", transactionsPanel);
        
        // Add header and sections to report panel
        reportPanel.add(headerPanel, BorderLayout.NORTH);
        reportPanel.add(reportSections, BorderLayout.CENTER);
        
        // Update the main tabbed pane
        reportTabbedPane.removeAll();
        reportTabbedPane.addTab("Sales Report", reportPanel);
    }
    
    private void displayStockReport(Report report) {
        // Create a new panel for the report
        JPanel reportPanel = new JPanel(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Stock Report");
        titleLabel.setFont(Constants.TITLE_FONT);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel dateLabel = new JLabel("Generated on: " + DateUtil.formatDisplayDate(LocalDate.now()));
        headerPanel.add(dateLabel, BorderLayout.CENTER);
        
        // Create tabbed pane for report sections
        JTabbedPane reportSections = new JTabbedPane();
        
        // All Products Tab
        JPanel allProductsPanel = new JPanel(new BorderLayout());
        
        // Create table for all products
        DefaultTableModel allProductsModel = new DefaultTableModel(
                new String[]{"ID", "Product", "Price", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable allProductsTable = new JTable(allProductsModel);
        
        // Add products data
        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) report.getAdditionalData().get("products");
        if (products != null) {
            for (Product product : products) {
                Object[] row = {
                    product.getProductId(),
                    product.getProductName(),
                    "£" + product.getPrice(),
                    product.getCurrentStock()
                };
                allProductsModel.addRow(row);
            }
        }
        
        allProductsPanel.add(new JScrollPane(allProductsTable), BorderLayout.CENTER);
        
        // Low Stock Tab
        JPanel lowStockPanel = new JPanel(new BorderLayout());
        
        // Create table for low stock products
        DefaultTableModel lowStockModel = new DefaultTableModel(
                new String[]{"ID", "Product", "Price", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable lowStockTable = new JTable(lowStockModel);
        
        // Add low stock products data
        @SuppressWarnings("unchecked")
        List<Product> lowStockProducts = (List<Product>) report.getAdditionalData().get("lowStockProducts");
        if (lowStockProducts != null) {
            for (Product product : lowStockProducts) {
                Object[] row = {
                    product.getProductId(),
                    product.getProductName(),
                    "£" + product.getPrice(),
                    product.getCurrentStock()
                };
                lowStockModel.addRow(row);
            }
        }
        
        lowStockPanel.add(new JScrollPane(lowStockTable), BorderLayout.CENTER);
        
        // Add tabs to the report sections
        reportSections.addTab("All Products", allProductsPanel);
        reportSections.addTab("Low Stock", lowStockPanel);
        
        // Add header and sections to report panel
        reportPanel.add(headerPanel, BorderLayout.NORTH);
        reportPanel.add(reportSections, BorderLayout.CENTER);
        
        // Update the main tabbed pane
        reportTabbedPane.removeAll();
        reportTabbedPane.addTab("Stock Report", reportPanel);
    }
    
    private void exportReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "Report would be exported to: " + 
                    fileChooser.getSelectedFile().getAbsolutePath() +
                    "\n\nThis feature is not fully implemented in this version.",
                    "Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if ("generate".equals(command)) {
            generateReport();
        } else if ("export".equals(command)) {
            exportReport();
        }
    }
}