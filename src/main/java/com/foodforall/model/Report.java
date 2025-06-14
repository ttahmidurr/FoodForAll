package com.foodforall.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report {
    private LocalDate startDate;
    private LocalDate endDate;
    private String reportType; // "SALES", "STOCK", "TRANSACTIONS"
    private BigDecimal totalRevenue;
    private int totalTransactions;
    private List<Sale> sales;
    private Map<String, Object> additionalData;

    public Report() {
        this.sales = new ArrayList<>();
        this.additionalData = new HashMap<>();
    }

    public Report(LocalDate startDate, LocalDate endDate, String reportType) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.reportType = reportType;
        this.sales = new ArrayList<>();
        this.additionalData = new HashMap<>();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public List<Sale> getSales() {
        return sales;
    }

    public void setSales(List<Sale> sales) {
        this.sales = sales;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    public void addAdditionalData(String key, Object value) {
        this.additionalData.put(key, value);
    }
}