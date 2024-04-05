package dev.magadiflo.hexagonal.app.application;

import dev.magadiflo.hexagonal.app.domain.Order;
import dev.magadiflo.hexagonal.app.infrastructure.input.port.OrderInputPort;
import dev.magadiflo.hexagonal.app.infrastructure.output.port.EntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderUseCase implements OrderInputPort {

    private final EntityRepository<Order, String> orderRepository;

    @Override
    public Order createOrder(String customerId, BigDecimal total) {
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .total(total)
                .build();
        return this.orderRepository.save(order);
    }
}
