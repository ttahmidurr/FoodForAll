package com.foodforall.model;

import java.time.LocalDateTime;

public class Stock {
    private int stockId;
    private Product product;
    private int quantity;
    private LocalDateTime lastUpdated;

    public Stock() {
    }

    public Stock(int stockId, Product product, int quantity, LocalDateTime lastUpdated) {
        this.stockId = stockId;
        this.product = product;
        this.quantity = quantity;
        this.lastUpdated = lastUpdated;
    }

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
