package com.foodforall.service;

import com.foodforall.dao.SaleDAO;
import com.foodforall.model.Payment;
import com.foodforall.model.PaymentType;
import com.foodforall.model.Sale;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SaleService {
    private static SaleService instance;
    private final SaleDAO saleDAO;
    
    private SaleService() {
        this.saleDAO = new SaleDAO();
    }
    
    public static synchronized SaleService getInstance() {
        if (instance == null) {
            instance = new SaleService();
        }
        return instance;
    }
    
    public boolean processSale(Sale sale, PaymentType paymentType, BigDecimal amountPaid) throws SQLException {
        // Set the user to the current logged in user
        sale.setUser(AuthenticationService.getInstance().getCurrentUser());
        
        // Calculate the total amount
        sale.recalculateTotal();
        
        // Create the payment
        Payment payment = new Payment();
        payment.setPaymentType(paymentType);
        payment.setAmount(amountPaid);
        payment.setPaymentDate(LocalDateTime.now());
        
        // Calculate change if cash payment
        if (paymentType.getName().equals("Cash")) {
            payment.setChangeAmount(payment.calculateChange(sale.getTotalAmount()));
        } else {
            payment.setChangeAmount(BigDecimal.ZERO);
        }
        
        sale.setPayment(payment);
        
        // Process the sale
        boolean success = saleDAO.saveSale(sale);
        
        if (success) {
            AuditService.getInstance().logAction("PROCESS", "SALE", sale.getSaleId(),
                    "Processed sale with " + sale.getItems().size() + " items for a total of " + sale.getTotalAmount());
        }
        
        return success;
    }
    
    public Sale getSaleById(int saleId) throws SQLException {
        return saleDAO.getSaleById(saleId);
    }
    
    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return saleDAO.getSalesByDateRange(startDate, endDate);
    }
    
    public List<Sale> getSalesByUser(int userId) throws SQLException {
        return saleDAO.getSalesByUser(userId);
    }
    
    public List<PaymentType> getAllPaymentTypes() throws SQLException {
        return saleDAO.getAllPaymentTypes();
    }
}
