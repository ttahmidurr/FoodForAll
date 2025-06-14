package com.foodforall.service;

import com.foodforall.dao.AuditLogDAO;
import com.foodforall.model.AuditLog;
import com.foodforall.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AuditService {
    private static AuditService instance;
    private final AuditLogDAO auditLogDAO;
    
    private AuditService() {
        this.auditLogDAO = new AuditLogDAO();
    }
    
    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }
    
    public boolean logAction(String action, String entity, Integer entityId, String details) {
        try {
            User currentUser = AuthenticationService.getInstance().getCurrentUser();
            
            if (currentUser != null) {
                AuditLog log = new AuditLog();
                log.setUser(currentUser);
                log.setAction(action);
                log.setEntity(entity);
                log.setEntityId(entityId);
                log.setDetails(details);
                log.setTimestamp(LocalDateTime.now());
                
                return auditLogDAO.addLog(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error logging action: " + e.getMessage());
        }
        
        return false;
    }
    
    public List<AuditLog> getAllLogs() throws SQLException {
        return auditLogDAO.getAllLogs();
    }
    
    public List<AuditLog> getLogsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return auditLogDAO.getLogsByDateRange(startDate, endDate);
    }
    
    public List<AuditLog> getLogsByUser(int userId) throws SQLException {
        return auditLogDAO.getLogsByUser(userId);
    }
}