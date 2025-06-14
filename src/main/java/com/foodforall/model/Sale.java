package com.foodforall.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int saleId;
    private Customer customer;
    private User user;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    private List<SaleItem> items;
    private Payment payment;

    public Sale() {
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
    }

    public Sale(int saleId, Customer customer, User user, LocalDateTime saleDate, BigDecimal totalAmount) {
        this.saleId = saleId;
        this.customer = customer;
        this.user = user;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.items = new ArrayList<>();
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
        recalculateTotal();
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public void addItem(SaleItem item) {
        // Check if the product is already in the cart, if so, update quantity
        for (SaleItem existingItem : items) {
            if (existingItem.getProduct().getProductId() == item.getProduct().getProductId()) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                existingItem.recalculateSubtotal();
                recalculateTotal();
                return;
            }
        }
        
        // Otherwise, add as new item
        items.add(item);
        recalculateTotal();
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            recalculateTotal();
        }
    }

    public void updateItemQuantity(int index, int quantity) {
        if (index >= 0 && index < items.size()) {
            SaleItem item = items.get(index);
            item.setQuantity(quantity);
            item.recalculateSubtotal();
            recalculateTotal();
        }
    }

    public void recalculateTotal() {
        this.totalAmount = BigDecimal.ZERO;
        for (SaleItem item : items) {
            this.totalAmount = this.totalAmount.add(item.getSubtotal());
        }
    }

    public void clear() {
        items.clear();
        totalAmount = BigDecimal.ZERO;
        payment = null;
    }
}
