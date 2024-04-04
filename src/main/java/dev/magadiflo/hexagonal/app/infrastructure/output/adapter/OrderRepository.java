package dev.magadiflo.hexagonal.app.infrastructure.output.adapter;

import dev.magadiflo.hexagonal.app.domain.Order;
import dev.magadiflo.hexagonal.app.infrastructure.output.port.EntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class OrderRepository implements EntityRepository<Order, String> {

    private final JdbcClient jdbcClient;

    @Override
    public Iterable<Order> findAll() {
        return this.jdbcClient.sql("SELECT id, customer_id, total FROM orders" )
                .query(new OrderRowMapper())
                .list();
    }

    @Override
    public Optional<Order> findById(String primaryKey) {
        return this.jdbcClient.sql("""
                        SELECT o.id, o.customer_id, o.total
                        FROM orders AS o
                        WHERE o.id = :id
                        """)
                .param("id", primaryKey)
                .query(new OrderRowMapper())
                .optional();
    }

    @Override
    public Order save(Order order) {
        this.jdbcClient.sql("""
                        INSERT INTO orders(id, customer_id, total)
                        VALUES(:id, :customerId, :total)
                        """)
                .param("id", order.getId())
                .param("customerId", order.getCustomerId())
                .param("total", order.getTotal())
                .update();
        return order;
    }

    @Override
    public void deleteById(String primaryKey) {
        this.jdbcClient.sql("DELETE FROM orders WHERE id = :id" )
                .param("id", primaryKey)
                .update();
    }

    private static class OrderRowMapper implements RowMapper<Order> {

        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Order.builder()
                    .id(rs.getString("id" ))
                    .customerId(rs.getString("customer_id" ))
                    .total(rs.getBigDecimal("total" ))
                    .build();
        }
    }
}
