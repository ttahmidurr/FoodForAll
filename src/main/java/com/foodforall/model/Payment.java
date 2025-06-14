package com.foodforall.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int saleId;
    private PaymentType paymentType;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private BigDecimal changeAmount;

    public Payment() {
    }

    public Payment(int paymentId, int saleId, PaymentType paymentType, BigDecimal amount, LocalDateTime paymentDate, BigDecimal changeAmount) {
        this.paymentId = paymentId;
        this.saleId = saleId;
        this.paymentType = paymentType;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.changeAmount = changeAmount;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public BigDecimal calculateChange(BigDecimal totalDue) {
        if (amount.compareTo(totalDue) >= 0) {
            return amount.subtract(totalDue);
        }
        return BigDecimal.ZERO;
    }
}