package com.foodforall.dao;
import com.foodforall.model.AuditLog;
import com.foodforall.model.User;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {
    
    public boolean addLog(AuditLog log) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, action, entity, entity_id, details) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, log.getUser().getUserId());
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getEntity());
            
            if (log.getEntityId() != null) {
                stmt.setInt(4, log.getEntityId());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            stmt.setString(5, log.getDetails());
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        }
    }
    
    public List<AuditLog> getAllLogs() throws SQLException {
        String sql = "SELECT l.log_id, l.user_id, l.action, l.entity, l.entity_id, l.details, l.timestamp, " +
                     "u.username, u.full_name " +
                     "FROM audit_logs l " +
                     "JOIN users u ON l.user_id = u.user_id " +
                     "ORDER BY l.timestamp DESC";
        
        List<AuditLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                
                AuditLog log = new AuditLog();
                log.setLogId(rs.getInt("log_id"));
                log.setUser(user);
                log.setAction(rs.getString("action"));
                log.setEntity(rs.getString("entity"));
                
                int entityId = rs.getInt("entity_id");
                if (!rs.wasNull()) {
                    log.setEntityId(entityId);
                }
                
                log.setDetails(rs.getString("details"));
                log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                
                logs.add(log);
            }
        }
        
        return logs;
    }
    
    public List<AuditLog> getLogsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT l.log_id, l.user_id, l.action, l.entity, l.entity_id, l.details, l.timestamp, " +
                     "u.username, u.full_name " +
                     "FROM audit_logs l " +
                     "JOIN users u ON l.user_id = u.user_id " +
                     "WHERE DATE(l.timestamp) BETWEEN ? AND ? " +
                     "ORDER BY l.timestamp DESC";
        
        List<AuditLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    
                    AuditLog log = new AuditLog();
                    log.setLogId(rs.getInt("log_id"));
                    log.setUser(user);
                    log.setAction(rs.getString("action"));
                    log.setEntity(rs.getString("entity"));
                    
                    int entityId = rs.getInt("entity_id");
                    if (!rs.wasNull()) {
                        log.setEntityId(entityId);
                    }
                    
                    log.setDetails(rs.getString("details"));
                    log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    
                    logs.add(log);
                }
            }
        }
        
        return logs;
    }
    
    public List<AuditLog> getLogsByUser(int userId) throws SQLException {
        String sql = "SELECT l.log_id, l.user_id, l.action, l.entity, l.entity_id, l.details, l.timestamp, " +
                     "u.username, u.full_name " +
                     "FROM audit_logs l " +
                     "JOIN users u ON l.user_id = u.user_id " +
                     "WHERE l.user_id = ? " +
                     "ORDER BY l.timestamp DESC";
        
        List<AuditLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    
                    AuditLog log = new AuditLog();
                    log.setLogId(rs.getInt("log_id"));
                    log.setUser(user);
                    log.setAction(rs.getString("action"));
                    log.setEntity(rs.getString("entity"));
                    
                    int entityId = rs.getInt("entity_id");
                    if (!rs.wasNull()) {
                        log.setEntityId(entityId);
                    }
                    
                    log.setDetails(rs.getString("details"));
                    log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    
                    logs.add(log);
                }
            }
        }
        
        return logs;
    }
}