package com.foodforall.service;

import com.foodforall.dao.ProductDAO;
import com.foodforall.dao.StockDAO;
import com.foodforall.model.Product;

import java.sql.SQLException;
import java.util.List;

public class ProductService {
    private static ProductService instance;
    private final ProductDAO productDAO;
    private final StockDAO stockDAO;
    
    private ProductService() {
        this.productDAO = new ProductDAO();
        this.stockDAO = new StockDAO();
    }
    
    public static synchronized ProductService getInstance() {
        if (instance == null) {
            instance = new ProductService();
        }
        return instance;
    }
    
    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAllProducts();
    }
    
    public List<Product> getProductsInStock() throws SQLException {
        return productDAO.getProductsInStock();
    }
    
    public Product getProductById(int productId) throws SQLException {
        return productDAO.getProductById(productId);
    }
    
    public boolean addProduct(Product product) throws SQLException {
        boolean success = productDAO.addProduct(product);
        
        if (success) {
            AuditService.getInstance().logAction("CREATE", "PRODUCT", product.getProductId(),
                    "Created product: " + product.getProductName() + " with stock: " + product.getCurrentStock());
        }
        
        return success;
    }
    
    public boolean updateProduct(Product product) throws SQLException {
        boolean success = productDAO.updateProduct(product);
        
        if (success) {
            AuditService.getInstance().logAction("UPDATE", "PRODUCT", product.getProductId(),
                    "Updated product: " + product.getProductName());
        }
        
        return success;
    }
    
    public boolean deleteProduct(int productId) throws SQLException {
        Product product = getProductById(productId);
        
        if (product != null) {
            boolean success = productDAO.deleteProduct(productId);
            
            if (success) {
                AuditService.getInstance().logAction("DELETE", "PRODUCT", productId,
                        "Deleted product: " + product.getProductName());
            }
            
            return success;
        }
        
        return false;
    }
    
    public boolean updateStock(int productId, int quantity) throws SQLException {
        boolean success = stockDAO.updateStock(productId, quantity);
        
        if (success) {
            AuditService.getInstance().logAction("UPDATE", "STOCK", productId,
                    "Updated stock for product ID: " + productId + " to " + quantity);
        }
        
        return success;
    }
    
    public boolean adjustStock(int productId, int adjustment) throws SQLException {
        boolean success = stockDAO.adjustStock(productId, adjustment);
        
        if (success) {
            AuditService.getInstance().logAction("ADJUST", "STOCK", productId,
                    "Adjusted stock for product ID: " + productId + " by " + adjustment);
        }
        
        return success;
    }
    
    public boolean hasEnoughStock(int productId, int requestedQuantity) throws SQLException {
        return stockDAO.hasEnoughStock(productId, requestedQuantity);
    }
}
