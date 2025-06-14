package com.foodforall.dao;
import com.foodforall.model.Role;
import com.foodforall.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public User authenticate(String username, String hashedPassword) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.password, u.full_name, " +
                     "u.created_at, u.last_login, r.role_id, r.role_name " +
                     "FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "WHERE u.username = ? AND u.password = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setFullName(rs.getString("full_name"));
                    
                    Role role = new Role();
                    role.setRoleId(rs.getInt("role_id"));
                    role.setRoleName(rs.getString("role_name"));
                    user.setRole(role);
                    
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    Timestamp lastLogin = rs.getTimestamp("last_login");
                    if (lastLogin != null) {
                        user.setLastLogin(lastLogin.toLocalDateTime());
                    }
                    
                    updateLastLogin(user.getUserId());
                    
                    return user;
                }
            }
        }
        
        return null;
    }
    
    private void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.password, u.full_name, " +
                     "u.created_at, u.last_login, r.role_id, r.role_name " +
                     "FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "ORDER BY u.username";
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                
                Role role = new Role();
                role.setRoleId(rs.getInt("role_id"));
                role.setRoleName(rs.getString("role_name"));
                user.setRole(role);
                
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                Timestamp lastLogin = rs.getTimestamp("last_login");
                if (lastLogin != null) {
                    user.setLastLogin(lastLogin.toLocalDateTime());
                }
                
                users.add(user);
            }
        }
        
        return users;
    }
    
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.password, u.full_name, " +
                     "u.created_at, u.last_login, r.role_id, r.role_name " +
                     "FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "WHERE u.user_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setFullName(rs.getString("full_name"));
                    
                    Role role = new Role();
                    role.setRoleId(rs.getInt("role_id"));
                    role.setRoleName(rs.getString("role_name"));
                    user.setRole(role);
                    
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    Timestamp lastLogin = rs.getTimestamp("last_login");
                    if (lastLogin != null) {
                        user.setLastLogin(lastLogin.toLocalDateTime());
                    }
                    
                    return user;
                }
            }
        }
        
        return null;
    }
    
    public boolean addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, role_id) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setInt(4, user.getRole().getRoleId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
    
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, full_name = ?, role_id = ? " +
                     "WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setInt(3, user.getRole().getRoleId());
            stmt.setInt(4, user.getUserId());
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        }
    }
    
    public boolean updateUserPassword(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        }
    }
    
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        }
    }
    
    public List<Role> getAllRoles() throws SQLException {
        String sql = "SELECT role_id, role_name FROM roles ORDER BY role_name";
        
        List<Role> roles = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getInt("role_id"));
                role.setRoleName(rs.getString("role_name"));
                
                roles.add(role);
            }
        }
        
        return roles;
    }
}
