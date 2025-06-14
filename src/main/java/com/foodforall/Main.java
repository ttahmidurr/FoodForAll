package com.foodforall;

import com.foodforall.ui.LoginUI;

import javax.swing.*;
import java.sql.SQLException;

public class Main {
    
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialise database tables if needed
        try {
            initialiseDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                    "Error initialising database: " + e.getMessage() + 
                    "\nThe application will now exit.", 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Start the application UI
        SwingUtilities.invokeLater(() -> new LoginUI());
    }
    
    private static void initialiseDatabase() throws SQLException {
        // This would contain code to create tables if they don't exist
        // For this implementation, we assume tables are already created
        // according to the SQL schema outlined in the project structure
        
        // In a real implementation, you would include code like:
        // 
        // Connection conn = DatabaseConnection.getInstance().getConnection();
        // Statement stmt = conn.createStatement();
        // 
        // // Create tables if they don't exist
        // stmt.execute("CREATE TABLE IF NOT EXISTS roles (...)");
        // stmt.execute("CREATE TABLE IF NOT EXISTS users (...)");
        // ...
        // 
        // // Insert default data if needed
        // ...
        // 
        // stmt.close();
    }
}