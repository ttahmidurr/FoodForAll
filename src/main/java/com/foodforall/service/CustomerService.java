package com.foodforall.service;

import com.foodforall.dao.CustomerDAO;
import com.foodforall.model.Customer;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private static CustomerService instance;
    private final CustomerDAO customerDAO;
    
    private CustomerService() {
        this.customerDAO = new CustomerDAO();
    }
    
    public static synchronized CustomerService getInstance() {
        if (instance == null) {
            instance = new CustomerService();
        }
        return instance;
    }
    
    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.getAllCustomers();
    }
    
    public Customer getCustomerById(int customerId) throws SQLException {
        return customerDAO.getCustomerById(customerId);
    }
    
    public List<Customer> searchCustomers(String searchTerm) throws SQLException {
        return customerDAO.searchCustomers(searchTerm);
    }
    
    public boolean addCustomer(Customer customer) throws SQLException {
        boolean success = customerDAO.addCustomer(customer);
        
        if (success) {
            AuditService.getInstance().logAction("CREATE", "CUSTOMER", customer.getCustomerId(),
                    "Created customer: " + customer.getFullName());
        }
        
        return success;
    }
    
    public boolean updateCustomer(Customer customer) throws SQLException {
        boolean success = customerDAO.updateCustomer(customer);
        
        if (success) {
            AuditService.getInstance().logAction("UPDATE", "CUSTOMER", customer.getCustomerId(),
                    "Updated customer: " + customer.getFullName());
        }
        
        return success;
    }
    
    public boolean deleteCustomer(int customerId) throws SQLException {
        Customer customer = getCustomerById(customerId);
        
        if (customer != null) {
            boolean success = customerDAO.deleteCustomer(customerId);
            
            if (success) {
                AuditService.getInstance().logAction("DELETE", "CUSTOMER", customerId,
                        "Deleted customer: " + customer.getFullName());
            }
            
            return success;
        }
        
        return false;
    }
}