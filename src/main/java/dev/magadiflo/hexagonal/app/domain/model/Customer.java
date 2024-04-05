package dev.magadiflo.hexagonal.app.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Customer {
    private String id;
    private String name;
    private String country;
}
