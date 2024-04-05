package dev.magadiflo.hexagonal.app.infrastructure.input.adapter;

import dev.magadiflo.hexagonal.app.domain.model.Order;
import dev.magadiflo.hexagonal.app.domain.service.OrderInputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/orders" )
public class OrderAPI {

    private final OrderInputPort orderInputPort;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestParam String customerId, @RequestParam BigDecimal total) {
        Order orderDB = this.orderInputPort.createOrder(customerId, total);
        URI uri = URI.create("/api/v1/orders/" + orderDB.getId());
        return ResponseEntity.created(uri).body(orderDB);
    }
}
