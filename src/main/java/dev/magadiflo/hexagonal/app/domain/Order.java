package dev.magadiflo.hexagonal.app.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class Order {
    private String id;
    private String customerId;
    private BigDecimal total;
}
