package com.foodforall.dao;

import com.foodforall.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    
    public List<Customer> getAllCustomers() throws SQLException {
        String sql = "SELECT customer_id, full_name, phone, email, address, created_at " +
                     "FROM customers ORDER BY full_name";
        
        List<Customer> customers = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Customer customer = new Customer();
                customer.setCustomerId(rs.getInt("customer_id"));
                customer.setFullName(rs.getString("full_name"));
                customer.setPhone(rs.getString("phone"));
                customer.setEmail(rs.getString("email"));
                customer.setAddress(rs.getString("address"));
                customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                
                customers.add(customer);
            }
        }
        
        return customers;
    }
    
    public Customer getCustomerById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, full_name, phone, email, address, created_at " +
                     "FROM customers WHERE customer_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(rs.getInt("customer_id"));
                    customer.setFullName(rs.getString("full_name"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setEmail(rs.getString("email"));
                    customer.setAddress(rs.getString("address"));
                    customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    
                    return customer;
                }
            }
        }
        
        return null;
    }
    
    public List<Customer> searchCustomers(String searchTerm) throws SQLException {
        String sql = "SELECT customer_id, full_name, phone, email, address, created_at " +
                     "FROM customers " +
                     "WHERE full_name LIKE ? OR phone LIKE ? OR email LIKE ? " +
                     "ORDER BY full_name";
        
        List<Customer> customers = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(rs.getInt("customer_id"));
                    customer.setFullName(rs.getString("full_name"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setEmail(rs.getString("email"));
                    customer.setAddress(rs.getString("address"));
                    customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    
                    customers.add(customer);
                }
            }
        }
        
        return customers;
    }
    
    public boolean addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (full_name, phone, email, address) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getAddress());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customer.setCustomerId(generatedKeys.getInt(1));
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
    
    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, phone = ?, email = ?, address = ? " +
                     "WHERE customer_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getAddress());
            stmt.setInt(5, customer.getCustomerId());
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        }
    }
    
    public boolean deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        }
    }
}
