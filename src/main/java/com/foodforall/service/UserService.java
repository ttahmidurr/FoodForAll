package com.foodforall.service;

import com.foodforall.dao.UserDAO;
import com.foodforall.model.Role;
import com.foodforall.model.User;
import com.foodforall.util.SecurityUtil;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private static UserService instance;
    private final UserDAO userDAO;
    
    private UserService() {
        this.userDAO = new UserDAO();
    }
    
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }
    
    public User getUserById(int userId) throws SQLException {
        return userDAO.getUserById(userId);
    }
    
    public boolean addUser(User user, String password) throws SQLException {
        // Hash the password before storing
        user.setPassword(SecurityUtil.hashPassword(password));
        
        boolean success = userDAO.addUser(user);
        
        if (success) {
            AuditService.getInstance().logAction("CREATE", "USER", user.getUserId(),
                    "Created user: " + user.getUsername());
        }
        
        return success;
    }
    
    public boolean updateUser(User user) throws SQLException {
        boolean success = userDAO.updateUser(user);
        
        if (success) {
            AuditService.getInstance().logAction("UPDATE", "USER", user.getUserId(),
                    "Updated user: " + user.getUsername());
        }
        
        return success;
    }
    
    public boolean updateUserPassword(int userId, String newPassword) throws SQLException {
        String hashedPassword = SecurityUtil.hashPassword(newPassword);
        boolean success = userDAO.updateUserPassword(userId, hashedPassword);
        
        if (success) {
            AuditService.getInstance().logAction("UPDATE", "USER", userId,
                    "Updated password for user ID: " + userId);
        }
        
        return success;
    }
    
    public boolean deleteUser(int userId) throws SQLException {
        User user = getUserById(userId);
        
        if (user != null) {
            boolean success = userDAO.deleteUser(userId);
            
            if (success) {
                AuditService.getInstance().logAction("DELETE", "USER", userId,
                        "Deleted user: " + user.getUsername());
            }
            
            return success;
        }
        
        return false;
    }
    
    public List<Role> getAllRoles() throws SQLException {
        return userDAO.getAllRoles();
    }
}
