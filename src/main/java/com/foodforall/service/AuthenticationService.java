package com.foodforall.service;
import com.foodforall.dao.UserDAO;
import com.foodforall.model.User;
import java.sql.SQLException;
import com.foodforall.util.SecurityUtil;

public class AuthenticationService {
    private static AuthenticationService instance;
    private User currentUser;
    
    private AuthenticationService() {
    }
    
    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }
    
    public boolean login(String username, String password) {
        try {
            String hashedPassword = SecurityUtil.hashPassword(password);
            //String hashedPassword = password; // For testing purposes, use plain password
            User user = new UserDAO().authenticate(username, hashedPassword);
            
            if (user != null) {
                currentUser = user;
                
                // Log the login action
                AuditService.getInstance().logAction("LOGIN", "USER", user.getUserId(),
                        "User logged in: " + username);
                
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error during login: " + e.getMessage());
        }
        
        return false;
    }
    
    public void logout() {
        if (currentUser != null) {
            // Log the logout action
            AuditService.getInstance().logAction("LOGOUT", "USER", currentUser.getUserId(),
                    "User logged out: " + currentUser.getUsername());
            
            currentUser = null;
        }
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }
}
