package com.foodforall.dao;
import com.foodforall.model.Product;
import com.foodforall.model.Stock;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {
    
    public List<Stock> getAllStock() throws SQLException {
        String sql = "SELECT s.stock_id, s.product_id, s.quantity, s.last_updated, " +
                     "p.product_name, p.description, p.price, p.created_at, p.updated_at " +
                     "FROM stock s " +
                     "JOIN products p ON s.product_id = p.product_id " +
                     "ORDER BY p.product_name";
        
        List<Stock> stocks = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setProductName(rs.getString("product_name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                
                Stock stock = new Stock();
                stock.setStockId(rs.getInt("stock_id"));
                stock.setProduct(product);
                stock.setQuantity(rs.getInt("quantity"));
                stock.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                
                stocks.add(stock);
            }
        }
        
        return stocks;
    }
    
    public Stock getStockByProductId(int productId) throws SQLException {
        String sql = "SELECT s.stock_id, s.product_id, s.quantity, s.last_updated, " +
                     "p.product_name, p.description, p.price, p.created_at, p.updated_at " +
                     "FROM stock s " +
                     "JOIN products p ON s.product_id = p.product_id " +
                     "WHERE s.product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    
                    Stock stock = new Stock();
                    stock.setStockId(rs.getInt("stock_id"));
                    stock.setProduct(product);
                    stock.setQuantity(rs.getInt("quantity"));
                    stock.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                    
                    return stock;
                }
            }
        }
        
        return null;
    }
    
    public boolean updateStock(int productId, int quantity) throws SQLException {
        // Check if stock entry exists
        String checkSql = "SELECT COUNT(*) FROM stock WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, productId);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    
                    if (count > 0) {
                        // Update existing stock
                        String updateSql = "UPDATE stock SET quantity = ?, last_updated = NOW() WHERE product_id = ?";
                        
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, quantity);
                            updateStmt.setInt(2, productId);
                            
                            int affectedRows = updateStmt.executeUpdate();
                            
                            return affectedRows > 0;
                        }
                    } else {
                        // Insert new stock
                        String insertSql = "INSERT INTO stock (product_id, quantity) VALUES (?, ?)";
                        
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, productId);
                            insertStmt.setInt(2, quantity);
                            
                            int affectedRows = insertStmt.executeUpdate();
                            
                            return affectedRows > 0;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean adjustStock(int productId, int adjustment) throws SQLException {
        String sql = "UPDATE stock SET quantity = quantity + ?, last_updated = NOW() WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adjustment);
            stmt.setInt(2, productId);
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        }
    }
    
    public boolean reduceStock(int productId, int quantity) throws SQLException {
        // First check if there's enough stock
        Stock stock = getStockByProductId(productId);
        
        if (stock == null || stock.getQuantity() < quantity) {
            return false;
        }
        
        // Then reduce the stock
        return adjustStock(productId, -quantity);
    }
    
    public boolean hasEnoughStock(int productId, int requestedQuantity) throws SQLException {
        Stock stock = getStockByProductId(productId);
        return stock != null && stock.getQuantity() >= requestedQuantity;
    }
}