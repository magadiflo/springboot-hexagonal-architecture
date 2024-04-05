package dev.magadiflo.hexagonal.app.domain.service;

import dev.magadiflo.hexagonal.app.domain.model.Customer;

import java.util.List;

public interface CustomerInputPort {
    List<Customer> getAllCustomers();

    Customer getCustomerById(String customerId);

    Customer createCustomer(String name, String country);

    void deleteCustomerById(String customerId);
}
