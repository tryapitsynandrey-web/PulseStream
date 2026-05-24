package com.pulsestream.domain.repository;

import com.pulsestream.domain.model.Payment;
import java.util.Optional;

public interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(String id);
}
