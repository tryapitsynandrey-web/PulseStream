package com.pulsestream.infrastructure.persistence.adapter;

import com.pulsestream.domain.model.CustomerActivity;
import com.pulsestream.domain.repository.CustomerActivityRepository;
import com.pulsestream.infrastructure.persistence.entity.CustomerActivityEntity;
import com.pulsestream.infrastructure.persistence.repository.SpringDataCustomerActivityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerActivityPersistenceAdapter implements CustomerActivityRepository {

    private final SpringDataCustomerActivityRepository repository;

    public CustomerActivityPersistenceAdapter(SpringDataCustomerActivityRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(CustomerActivity activity) {
        CustomerActivityEntity entity = new CustomerActivityEntity(
            activity.getId(),
            activity.getCustomerId(),
            activity.getActivityType(),
            activity.getMetadata(),
            activity.getCreatedAt()
        );
        repository.save(entity);
    }

    @Override
    public Optional<CustomerActivity> findById(String id) {
        return repository.findById(id).map(entity -> new CustomerActivity(
            entity.getId(),
            entity.getCustomerId(),
            entity.getActivityType(),
            entity.getMetadata(),
            entity.getCreatedAt()
        ));
    }
}
