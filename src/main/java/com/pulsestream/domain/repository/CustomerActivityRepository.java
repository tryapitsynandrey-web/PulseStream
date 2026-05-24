package com.pulsestream.domain.repository;

import com.pulsestream.domain.model.CustomerActivity;
import java.util.Optional;

public interface CustomerActivityRepository {
    void save(CustomerActivity activity);
    Optional<CustomerActivity> findById(String id);
}
