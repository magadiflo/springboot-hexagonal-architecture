package dev.magadiflo.hexagonal.app.application;

import dev.magadiflo.hexagonal.app.domain.model.Order;
import dev.magadiflo.hexagonal.app.domain.service.OrderInputPort;
import dev.magadiflo.hexagonal.app.domain.repository.EntityRepository;
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
