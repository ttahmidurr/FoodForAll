package com.foodforall.dao;

import com.foodforall.model.Product;
import com.foodforall.model.Report;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {
    
    public Report generateSalesReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        Report report = new Report(startDate, endDate, "SALES");
        
        // Get total revenue
        report.setTotalRevenue(new SaleDAO().getTotalSalesAmount(startDate, endDate));
        
        // Get total transactions
        report.setTotalTransactions(new SaleDAO().getTransactionCount(startDate, endDate));
        
        // Get sales in the date range
        report.setSales(new SaleDAO().getSalesByDateRange(startDate, endDate));
        
        // Get top selling products
        report.addAdditionalData("topProducts", getTopSellingProducts(startDate, endDate, 5));
        
        // Get daily sales totals
        report.addAdditionalData("dailySales", getDailySalesTotals(startDate, endDate));
        
        return report;
    }
    
    public Report generateStockReport() throws SQLException {
        Report report = new Report(null, null, "STOCK");
        
        // Get current stock levels
        List<Product> products = new ProductDAO().getAllProducts();
        report.addAdditionalData("products", products);
        
        // Get low stock items (less than 10 units)
        List<Product> lowStockProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getCurrentStock() < 10) {
                lowStockProducts.add(product);
            }
        }
        report.addAdditionalData("lowStockProducts", lowStockProducts);
        
        return report;
    }
    
    private List<Map<String, Object>> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit) throws SQLException {
        String sql = "SELECT p.product_id, p.product_name, SUM(si.quantity) as total_quantity, " +
                     "SUM(si.subtotal) as total_sales " +
                     "FROM sale_items si " +
                     "JOIN sales s ON si.sale_id = s.sale_id " +
                     "JOIN products p ON si.product_id = p.product_id " +
                     "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                     "GROUP BY p.product_id, p.product_name " +
                     "ORDER BY total_quantity DESC " +
                     "LIMIT ?";
        
        List<Map<String, Object>> topProducts = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            stmt.setInt(3, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", rs.getInt("product_id"));
                    product.put("productName", rs.getString("product_name"));
                    product.put("totalQuantity", rs.getInt("total_quantity"));
                    product.put("totalSales", rs.getBigDecimal("total_sales"));
                    
                    topProducts.add(product);
                }
            }
        }
        
        return topProducts;
    }
    
    private List<Map<String, Object>> getDailySalesTotals(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT DATE(sale_date) as sale_day, COUNT(*) as transaction_count, " +
                     "SUM(total_amount) as daily_total " +
                     "FROM sales " +
                     "WHERE DATE(sale_date) BETWEEN ? AND ? " +
                     "GROUP BY DATE(sale_date) " +
                     "ORDER BY sale_day";
        
        List<Map<String, Object>> dailySales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> daySales = new HashMap<>();
                    daySales.put("date", rs.getDate("sale_day").toLocalDate());
                    daySales.put("transactionCount", rs.getInt("transaction_count"));
                    daySales.put("totalAmount", rs.getBigDecimal("daily_total"));
                    
                    dailySales.add(daySales);
                }
            }
        }
        
        return dailySales;
    }
}