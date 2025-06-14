package com.foodforall.dao;

import com.foodforall.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    public List<Product> getAllProducts() throws SQLException {
        String sql = "SELECT p.product_id, p.product_name, p.description, p.price, " +
                     "p.created_at, p.updated_at, COALESCE(s.quantity, 0) as current_stock " +
                     "FROM products p " +
                     "LEFT JOIN stock s ON p.product_id = s.product_id " +
                     "ORDER BY p.product_name";
        
        List<Product> products = new ArrayList<>();
        
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
                product.setCurrentStock(rs.getInt("current_stock"));
                
                products.add(product);
            }
        }
        
        return products;
    }
    
    public List<Product> getProductsInStock() throws SQLException {
        String sql = "SELECT p.product_id, p.product_name, p.description, p.price, " +
                     "p.created_at, p.updated_at, s.quantity as current_stock " +
                     "FROM products p " +
                     "JOIN stock s ON p.product_id = s.product_id " +
                     "WHERE s.quantity > 0 " +
                     "ORDER BY p.product_name";
        
        List<Product> products = new ArrayList<>();
        
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
                product.setCurrentStock(rs.getInt("current_stock"));
                
                products.add(product);
            }
        }
        
        return products;
    }
    
    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT p.product_id, p.product_name, p.description, p.price, " +
                     "p.created_at, p.updated_at, COALESCE(s.quantity, 0) as current_stock " +
                     "FROM products p " +
                     "LEFT JOIN stock s ON p.product_id = s.product_id " +
                     "WHERE p.product_id = ?";
        
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
                    product.setCurrentStock(rs.getInt("current_stock"));
                    
                    return product;
                }
            }
        }
        
        return null;
    }
    
    public boolean addProduct(Product product) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            String productSql = "INSERT INTO products (product_name, description, price) " +
                                "VALUES (?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(productSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, product.getProductName());
                stmt.setString(2, product.getDescription());
                stmt.setBigDecimal(3, product.getPrice());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setProductId(generatedKeys.getInt(1));
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }
            
            // If stock is provided, insert it
            if (product.getCurrentStock() > 0) {
                String stockSql = "INSERT INTO stock (product_id, quantity) VALUES (?, ?)";
                
                try (PreparedStatement stmt = conn.prepareStatement(stockSql)) {
                    stmt.setInt(1, product.getProductId());
                    stmt.setInt(2, product.getCurrentStock());
                    
                    int affectedRows = stmt.executeUpdate();
                    
                    if (affectedRows == 0) {
                        conn.rollback();
                        return false;
                    }
                }
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            throw ex;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET product_name = ?, description = ?, price = ? " +
                     "WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getProductId());
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        }
    }
    
    public boolean deleteProduct(int productId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // First delete from stock
            String stockSql = "DELETE FROM stock WHERE product_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(stockSql)) {
                stmt.setInt(1, productId);
                stmt.executeUpdate();
            }
            
            // Then delete from products
            String productSql = "DELETE FROM products WHERE product_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(productSql)) {
                stmt.setInt(1, productId);
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            throw ex;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
