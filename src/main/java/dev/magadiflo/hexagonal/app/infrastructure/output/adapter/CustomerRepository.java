package dev.magadiflo.hexagonal.app.infrastructure.output.adapter;

import dev.magadiflo.hexagonal.app.domain.model.Customer;
import dev.magadiflo.hexagonal.app.domain.repository.EntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CustomerRepository implements EntityRepository<Customer, String> {

    private final JdbcClient jdbcClient;

    @Override
    public Iterable<Customer> findAll() {
        return this.jdbcClient.sql("SELECT id, name, country FROM customers" )
                .query(Customer.class)
                .list();
    }

    @Override
    public Optional<Customer> findById(String primaryKey) {
        return this.jdbcClient.sql("""
                        SELECT c.id, c.name, c.country
                        FROM customers AS c
                        WHERE c.id = :id
                        """)
                .param("id", primaryKey)
                .query(Customer.class)
                .optional();
    }

    @Override
    public Customer save(Customer customer) {
        this.jdbcClient.sql("""
                        INSERT INTO customers(id, name, country)
                        VALUES(:id, :name, :country)
                        """)
                .param("id", customer.getId(), Types.VARCHAR)
                .param("name", customer.getName(), Types.VARCHAR)
                .param("country", customer.getCountry(), Types.VARCHAR)
                .update();
        return customer;
    }

    @Override
    public void deleteById(String primaryKey) {
        this.jdbcClient.sql("DELETE FROM customers WHERE id = :id" )
                .param("id", primaryKey)
                .update();
    }
}
