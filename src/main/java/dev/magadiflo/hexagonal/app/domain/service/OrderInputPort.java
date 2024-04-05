package dev.magadiflo.hexagonal.app.domain.service;

import dev.magadiflo.hexagonal.app.domain.model.Order;

import java.math.BigDecimal;

public interface OrderInputPort {
    Order createOrder(String customerId, BigDecimal total);
}
