package com.pulsestream.infrastructure.persistence.adapter;

import com.pulsestream.domain.model.Payment;
import com.pulsestream.domain.repository.PaymentRepository;
import com.pulsestream.infrastructure.persistence.entity.PaymentEntity;
import com.pulsestream.infrastructure.persistence.repository.SpringDataPaymentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@SuppressWarnings("null")
public class PaymentPersistenceAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository repository;

    public PaymentPersistenceAdapter(SpringDataPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Payment payment) {
        PaymentEntity entity = new PaymentEntity(
            payment.getId(),
            payment.getOrderId(),
            payment.getAmount(),
            payment.getStatus(),
            payment.getTransactionRef(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
        repository.save(entity);
    }

    @Override
    public Optional<Payment> findById(String id) {
        return repository.findById(id).map(entity -> new Payment(
            entity.getId(),
            entity.getOrderId(),
            entity.getAmount(),
            entity.getStatus(),
            entity.getTransactionRef(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        ));
    }
}
