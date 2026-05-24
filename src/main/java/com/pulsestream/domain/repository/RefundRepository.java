package com.pulsestream.domain.repository;

import com.pulsestream.domain.model.Refund;
import java.util.Optional;

public interface RefundRepository {
    void save(Refund refund);
    Optional<Refund> findById(String id);
}
