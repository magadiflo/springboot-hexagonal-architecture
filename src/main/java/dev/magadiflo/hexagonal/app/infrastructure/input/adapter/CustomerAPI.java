package dev.magadiflo.hexagonal.app.infrastructure.input.adapter;

import dev.magadiflo.hexagonal.app.domain.Customer;
import dev.magadiflo.hexagonal.app.infrastructure.input.port.CustomerInputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/customers" )
public class CustomerAPI {

    private final CustomerInputPort customerInputPort;

    @GetMapping
    public ResponseEntity<List<Customer>> findAllCustomers() {
        return ResponseEntity.ok(this.customerInputPort.getAllCustomers());
    }

    @GetMapping(path = "/{customerId}" )
    public ResponseEntity<Customer> getCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(this.customerInputPort.getCustomerById(customerId));
    }

    @PostMapping
    public ResponseEntity<Customer> saveCustomer(@RequestParam String name, @RequestParam String country) {
        Customer customerDB = this.customerInputPort.createCustomer(name, country);
        URI uri = URI.create("/api/v1/customers/" + customerDB.getId());
        return ResponseEntity.created(uri).body(customerDB);
    }

    @DeleteMapping(path = "/{customerId}" )
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        this.customerInputPort.deleteCustomerById(customerId);
        return ResponseEntity.noContent().build();
    }
}
