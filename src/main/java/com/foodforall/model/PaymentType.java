package com.foodforall.model;

public class PaymentType {
    private int paymentTypeId;
    private String name;

    public PaymentType() {
    }

    public PaymentType(int paymentTypeId, String name) {
        this.paymentTypeId = paymentTypeId;
        this.name = name;
    }

    public int getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(int paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}