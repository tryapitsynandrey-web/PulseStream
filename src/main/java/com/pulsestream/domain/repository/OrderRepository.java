package com.pulsestream.domain.repository;

import com.pulsestream.domain.model.Order;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
}
