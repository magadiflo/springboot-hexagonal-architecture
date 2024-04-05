package dev.magadiflo.hexagonal.app.application;

import dev.magadiflo.hexagonal.app.domain.Customer;
import dev.magadiflo.hexagonal.app.infrastructure.input.port.CustomerInputPort;
import dev.magadiflo.hexagonal.app.infrastructure.output.port.EntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CustomerUserCase implements CustomerInputPort {

    private final EntityRepository<Customer, String> customerRepository;

    @Override
    public List<Customer> getAllCustomers() {
        return (List<Customer>) this.customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(String customerId) {
        return this.customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer no encontrado" ));
    }

    @Override
    public Customer createCustomer(String name, String country) {
        Customer customer = Customer.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .country(country)
                .build();
        return this.customerRepository.save(customer);
    }

    @Override
    public void deleteCustomerById(String customerId) {
        this.customerRepository.deleteById(customerId);
    }
}
