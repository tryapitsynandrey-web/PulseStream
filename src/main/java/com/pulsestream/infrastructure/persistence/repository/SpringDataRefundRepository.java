package com.pulsestream.infrastructure.persistence.repository;

import com.pulsestream.infrastructure.persistence.entity.RefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataRefundRepository extends JpaRepository<RefundEntity, String> {
}
