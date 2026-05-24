package com.pulsestream.infrastructure.persistence.adapter;

import com.pulsestream.domain.model.Refund;
import com.pulsestream.domain.repository.RefundRepository;
import com.pulsestream.infrastructure.persistence.entity.RefundEntity;
import com.pulsestream.infrastructure.persistence.repository.SpringDataRefundRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RefundPersistenceAdapter implements RefundRepository {

    private final SpringDataRefundRepository repository;

    public RefundPersistenceAdapter(SpringDataRefundRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Refund refund) {
        RefundEntity entity = new RefundEntity(
            refund.getId(),
            refund.getPaymentId(),
            refund.getAmount(),
            refund.getReason(),
            refund.getStatus(),
            refund.getCreatedAt(),
            refund.getUpdatedAt()
        );
        repository.save(entity);
    }

    @Override
    public Optional<Refund> findById(String id) {
        return repository.findById(id).map(entity -> new Refund(
            entity.getId(),
            entity.getPaymentId(),
            entity.getAmount(),
            entity.getReason(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        ));
    }
}
