package dev.magadiflo.hexagonal.app.domain.repository;

import java.util.Optional;

public interface EntityRepository<T, ID> {
    Iterable<T> findAll();

    Optional<T> findById(ID primaryKey);

    T save(T t);

    void deleteById(ID primaryKey);
}
