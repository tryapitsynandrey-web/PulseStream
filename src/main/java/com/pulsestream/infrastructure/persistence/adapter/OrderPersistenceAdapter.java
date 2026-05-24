package com.pulsestream.infrastructure.persistence.adapter;

import com.pulsestream.domain.model.Order;
import com.pulsestream.domain.repository.OrderRepository;
import com.pulsestream.infrastructure.persistence.entity.OrderEntity;
import com.pulsestream.infrastructure.persistence.repository.SpringDataOrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderPersistenceAdapter implements OrderRepository {

    private final SpringDataOrderRepository repository;

    public OrderPersistenceAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Order order) {
        OrderEntity entity = new OrderEntity(
            order.getId(),
            order.getCustomerId(),
            order.getProductId(),
            order.getQuantity(),
            order.getPrice(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
        repository.save(entity);
    }

    @Override
    public Optional<Order> findById(String id) {
        return repository.findById(id).map(entity -> new Order(
            entity.getId(),
            entity.getCustomerId(),
            entity.getProductId(),
            entity.getQuantity(),
            entity.getPrice(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        ));
    }
}
