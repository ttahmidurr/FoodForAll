package com.foodforall.service;

import com.foodforall.dao.ReportDAO;
import com.foodforall.model.Report;

import java.sql.SQLException;
import java.time.LocalDate;

public class ReportService {
    private static ReportService instance;
    private final ReportDAO reportDAO;
    
    private ReportService() {
        this.reportDAO = new ReportDAO();
    }
    
    public static synchronized ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }
    
    public Report generateSalesReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        Report report = reportDAO.generateSalesReport(startDate, endDate);
        
        AuditService.getInstance().logAction("GENERATE", "REPORT", null,
                "Generated sales report from " + startDate + " to " + endDate);
        
        return report;
    }
    
    public Report generateStockReport() throws SQLException {
        Report report = reportDAO.generateStockReport();
        
        AuditService.getInstance().logAction("GENERATE", "REPORT", null,
                "Generated stock report");
        
        return report;
    }
}
