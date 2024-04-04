package dev.magadiflo.hexagonal.app.infrastructure.input.port;

import dev.magadiflo.hexagonal.app.domain.Order;

import java.math.BigDecimal;

public interface OrderInputPort {
    Order createOrder(String customerId, BigDecimal total);
}
