package com.foodforall.model;

import java.math.BigDecimal;

public class SaleItem {
    private int saleItemId;
    private int saleId;
    private Product product;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public SaleItem() {
    }

    public SaleItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public SaleItem(int saleItemId, int saleId, Product product, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.saleItemId = saleItemId;
        this.saleId = saleId;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public int getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(int saleItemId) {
        this.saleItemId = saleItemId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null && quantity > 0) {
            this.unitPrice = product.getPrice();
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        if (product != null && quantity > 0) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        if (quantity > 0) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void recalculateSubtotal() {
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
