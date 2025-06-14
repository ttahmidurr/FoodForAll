package com.foodforall;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/foodforall";
        String user = "root";
        String password = "";

        try {
            // Load the MySQL driver
            System.out.println("Loading MySQL JDBC driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ Driver loaded successfully!");
            
            // Try to establish a connection
            System.out.println("Attempting to connect to database...");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✓ Connection established successfully!");
            
            // Get database metadata
            String dbVersion = conn.getMetaData().getDatabaseProductVersion();
            System.out.println("MySQL Version: " + dbVersion);
            
            // Close the connection
            conn.close();
            System.out.println("✓ Connection closed successfully!");
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ ERROR: MySQL JDBC driver not found!");
            System.err.println("  " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ ERROR: Database connection failed!");
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}