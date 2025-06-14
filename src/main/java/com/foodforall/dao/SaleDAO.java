package com.foodforall.dao;
import com.foodforall.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

public boolean saveSale(Sale sale) throws SQLException {
    Connection conn = null;
    PreparedStatement saleStmt = null;
    PreparedStatement saleItemStmt = null;
    PreparedStatement stockUpdateStmt = null;
    PreparedStatement paymentStmt = null;
    
    try {
        // Get connection and start transaction
        conn = DatabaseConnection.getInstance().getConnection();
        conn.setAutoCommit(false);
        
        System.out.println("=== PROCESSING SALE ===");
        System.out.println("Connection obtained: " + (conn != null));
        System.out.println("Connection closed: " + conn.isClosed());
        
        // Set sale date if not set
        if (sale.getSaleDate() == null) {
            sale.setSaleDate(LocalDateTime.now());
        }
        
        // 1. Insert main sale record
        String saleSQL = "INSERT INTO sales (customer_id, user_id, sale_date, total_amount) VALUES (?, ?, ?, ?)";
        saleStmt = conn.prepareStatement(saleSQL, Statement.RETURN_GENERATED_KEYS);
        
        // Handle nullable customer
        if (sale.getCustomer() != null) {
            saleStmt.setInt(1, sale.getCustomer().getCustomerId());
        } else {
            saleStmt.setNull(1, Types.INTEGER);
        }
        
        saleStmt.setInt(2, sale.getUser().getUserId());
        saleStmt.setTimestamp(3, Timestamp.valueOf(sale.getSaleDate()));
        saleStmt.setBigDecimal(4, sale.getTotalAmount());
        
        int saleResult = saleStmt.executeUpdate();
        if (saleResult == 0) {
            throw new SQLException("Failed to insert sale record");
        }
        
        // Get generated sale ID
        int saleId = 0;
        try (ResultSet generatedKeys = saleStmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                saleId = generatedKeys.getInt(1);
                sale.setSaleId(saleId); // Set the ID back to the sale object
                System.out.println("Sale ID generated: " + saleId);
            } else {
                throw new SQLException("Failed to get sale ID");
            }
        }
        
        // 2. Insert sale items using batch (FIXED VERSION)
        if (!sale.getItems().isEmpty()) {
            String saleItemSQL = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
            saleItemStmt = conn.prepareStatement(saleItemSQL);
            
            System.out.println("Preparing sale items batch...");
            for (SaleItem item : sale.getItems()) {
                saleItemStmt.setInt(1, saleId);
                saleItemStmt.setInt(2, item.getProduct().getProductId());
                saleItemStmt.setInt(3, item.getQuantity());
                saleItemStmt.setBigDecimal(4, item.getUnitPrice());
                saleItemStmt.setBigDecimal(5, item.getSubtotal());
                saleItemStmt.addBatch();
                
                System.out.println("Added to batch: Product " + item.getProduct().getProductId() + 
                                 ", Qty: " + item.getQuantity() + 
                                 ", Price: " + item.getUnitPrice());
            }
            
            // Execute sale items batch BEFORE closing statement
            System.out.println("Executing sale items batch...");
            int[] saleItemResults = saleItemStmt.executeBatch();
            System.out.println("Sale items inserted: " + saleItemResults.length);
            
            // Clear batch after execution
            saleItemStmt.clearBatch();
        }
        
        // 3. Update stock levels using batch (FIXED VERSION)
        if (!sale.getItems().isEmpty()) {
            String stockUpdateSQL = "UPDATE stock SET quantity = quantity - ? WHERE product_id = ?";
            stockUpdateStmt = conn.prepareStatement(stockUpdateSQL);
            
            System.out.println("Preparing stock update batch...");
            for (SaleItem item : sale.getItems()) {
                stockUpdateStmt.setInt(1, item.getQuantity());
                stockUpdateStmt.setInt(2, item.getProduct().getProductId());
                stockUpdateStmt.addBatch();
                
                System.out.println("Stock update batch: Product " + item.getProduct().getProductId() + 
                                 ", Reduce by: " + item.getQuantity());
            }
            
            // Execute stock update batch BEFORE closing statement
            System.out.println("Executing stock update batch...");
            int[] stockResults = stockUpdateStmt.executeBatch();
            System.out.println("Stock updates completed: " + stockResults.length);
            
            // Clear batch after execution
            stockUpdateStmt.clearBatch();
        }
        
        // 4. Insert payment record
        if (sale.getPayment() != null) {
            String paymentSQL = "INSERT INTO payments (sale_id, payment_type_id, amount, change_amount, payment_date) VALUES (?, ?, ?, ?, ?)";
            paymentStmt = conn.prepareStatement(paymentSQL);
            
            paymentStmt.setInt(1, saleId);
            paymentStmt.setInt(2, sale.getPayment().getPaymentType().getPaymentTypeId());
            paymentStmt.setBigDecimal(3, sale.getPayment().getAmount());
            paymentStmt.setBigDecimal(4, sale.getPayment().getChangeAmount());
            paymentStmt.setTimestamp(5, Timestamp.valueOf(sale.getPayment().getPaymentDate()));
            
            int paymentResult = paymentStmt.executeUpdate();
            if (paymentResult == 0) {
                throw new SQLException("Failed to insert payment record");
            }
            
            System.out.println("Payment recorded successfully");
        }
        
        // Commit transaction
        conn.commit();
        System.out.println("=== SALE COMPLETED SUCCESSFULLY ===");
        
        return true;
        
    } catch (SQLException e) {
        System.err.println("=== SALE PROCESSING ERROR ===");
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
        
        // Rollback transaction
        if (conn != null) {
            try {
                conn.rollback();
                System.out.println("Transaction rolled back");
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
        }
        throw e;
        
    } finally {
        // Close all statements in correct order - THIS IS THE KEY FIX
        if (paymentStmt != null) {
            try { 
                paymentStmt.close(); 
                System.out.println("Payment statement closed");
            } catch (SQLException e) { 
                System.err.println("Error closing payment statement: " + e.getMessage());
            }
        }
        
        if (stockUpdateStmt != null) {
            try { 
                stockUpdateStmt.close(); 
                System.out.println("Stock update statement closed");
            } catch (SQLException e) { 
                System.err.println("Error closing stock update statement: " + e.getMessage());
            }
        }
        
        if (saleItemStmt != null) {
            try { 
                saleItemStmt.close(); 
                System.out.println("Sale item statement closed");
            } catch (SQLException e) { 
                System.err.println("Error closing sale item statement: " + e.getMessage());
            }
        }
        
        if (saleStmt != null) {
            try { 
                saleStmt.close(); 
                System.out.println("Sale statement closed");
            } catch (SQLException e) { 
                System.err.println("Error closing sale statement: " + e.getMessage());
            }
        }
        
        // Reset auto-commit
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                System.out.println("Auto-commit reset to true");
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
}

    
    public Sale getSaleById(int saleId) throws SQLException {
        String saleSql = "SELECT s.sale_id, s.customer_id, s.user_id, s.sale_date, s.total_amount, " +
                         "u.username, u.full_name, " +
                         "c.full_name as customer_name " +
                         "FROM sales s " +
                         "JOIN users u ON s.user_id = u.user_id " +
                         "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                         "WHERE s.sale_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(saleSql)) {
            
            stmt.setInt(1, saleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = new Sale();
                    sale.setSaleId(rs.getInt("sale_id"));
                    
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    sale.setUser(user);
                    
                    int customerId = rs.getInt("customer_id");
                    if (!rs.wasNull()) {
                        Customer customer = new Customer();
                        customer.setCustomerId(customerId);
                        customer.setFullName(rs.getString("customer_name"));
                        sale.setCustomer(customer);
                    }
                    
                    sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                    sale.setTotalAmount(rs.getBigDecimal("total_amount"));
                    
                    // Get sale items
                    sale.setItems(getSaleItems(saleId));
                    
                    // Get payment
                    sale.setPayment(getPaymentForSale(saleId));
                    
                    return sale;
                }
            }
        }
        
        return null;
    }
    
    private List<SaleItem> getSaleItems(int saleId) throws SQLException {
        String sql = "SELECT si.sale_item_id, si.product_id, si.quantity, si.unit_price, si.subtotal, " +
                     "p.product_name, p.description " +
                     "FROM sale_items si " +
                     "JOIN products p ON si.product_id = p.product_id " +
                     "WHERE si.sale_id = ?";
        
        List<SaleItem> items = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, saleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("unit_price"));
                    
                    SaleItem item = new SaleItem();
                    item.setSaleItemId(rs.getInt("sale_item_id"));
                    item.setProduct(product);
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    
                    items.add(item);
                }
            }
        }
        
        return items;
    }
    
    private Payment getPaymentForSale(int saleId) throws SQLException {
        String sql = "SELECT p.payment_id, p.payment_type_id, p.amount, p.payment_date, p.change_amount, " +
                     "pt.name as payment_type_name " +
                     "FROM payments p " +
                     "JOIN payment_types pt ON p.payment_type_id = pt.payment_type_id " +
                     "WHERE p.sale_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, saleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PaymentType paymentType = new PaymentType();
                    paymentType.setPaymentTypeId(rs.getInt("payment_type_id"));
                    paymentType.setName(rs.getString("payment_type_name"));
                    
                    Payment payment = new Payment();
                    payment.setPaymentId(rs.getInt("payment_id"));
                    payment.setSaleId(saleId);
                    payment.setPaymentType(paymentType);
                    payment.setAmount(rs.getBigDecimal("amount"));
                    payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
                    payment.setChangeAmount(rs.getBigDecimal("change_amount"));
                    
                    return payment;
                }
            }
        }
        
        return null;
    }
    
    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT s.sale_id, s.customer_id, s.user_id, s.sale_date, s.total_amount, " +
                     "u.username, u.full_name, " +
                     "c.full_name as customer_name " +
                     "FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                     "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                     "ORDER BY s.sale_date DESC";
        
        List<Sale> sales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setSaleId(rs.getInt("sale_id"));
                    
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    sale.setUser(user);
                    
                    int customerId = rs.getInt("customer_id");
                    if (!rs.wasNull()) {
                        Customer customer = new Customer();
                        customer.setCustomerId(customerId);
                        customer.setFullName(rs.getString("customer_name"));
                        sale.setCustomer(customer);
                    }
                    
                    sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                    sale.setTotalAmount(rs.getBigDecimal("total_amount"));
                    
                    sales.add(sale);
                }
            }
        }
        
        return sales;
    }
    
    public List<Sale> getSalesByUser(int userId) throws SQLException {
        String sql = "SELECT s.sale_id, s.customer_id, s.user_id, s.sale_date, s.total_amount, " +
                     "u.username, u.full_name, " +
                     "c.full_name as customer_name " +
                     "FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                     "WHERE s.user_id = ? " +
                     "ORDER BY s.sale_date DESC";
        
        List<Sale> sales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setSaleId(rs.getInt("sale_id"));
                    
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    sale.setUser(user);
                    
                    int customerId = rs.getInt("customer_id");
                    if (!rs.wasNull()) {
                        Customer customer = new Customer();
                        customer.setCustomerId(customerId);
                        customer.setFullName(rs.getString("customer_name"));
                        sale.setCustomer(customer);
                    }
                    
                    sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                    sale.setTotalAmount(rs.getBigDecimal("total_amount"));
                    
                    sales.add(sale);
                }
            }
        }
        
        return sales;
    }
    
    public BigDecimal getTotalSalesAmount(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM sales WHERE DATE(sale_date) BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    public int getTransactionCount(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sales WHERE DATE(sale_date) BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    public List<PaymentType> getAllPaymentTypes() throws SQLException {
        String sql = "SELECT payment_type_id, name FROM payment_types ORDER BY name";
        
        List<PaymentType> paymentTypes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                PaymentType paymentType = new PaymentType();
                paymentType.setPaymentTypeId(rs.getInt("payment_type_id"));
                paymentType.setName(rs.getString("name"));
                
                paymentTypes.add(paymentType);
            }
        }
        
        return paymentTypes;
    }
}
