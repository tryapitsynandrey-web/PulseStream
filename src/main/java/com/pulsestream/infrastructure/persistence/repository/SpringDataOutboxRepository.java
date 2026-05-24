package com.pulsestream.infrastructure.persistence.repository;

import com.pulsestream.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpringDataOutboxRepository extends JpaRepository<OutboxEventEntity, String> {
    List<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
}
