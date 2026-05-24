package com.pulsestream.infrastructure.persistence.adapter;

import com.pulsestream.application.port.in.MetricsQueryUseCase.ActivityMetric;
import com.pulsestream.application.port.in.MetricsQueryUseCase.ProductMetric;
import com.pulsestream.domain.repository.AnalyticsQueryRepository;
import com.pulsestream.application.port.in.MetricsQueryUseCase.IngestedEventInfo;
import com.pulsestream.infrastructure.persistence.entity.IngestedEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@CircuitBreaker(name = "databaseAnalytics")
@Retry(name = "databaseAnalytics")
@SuppressWarnings("null")
public class AnalyticsQueryPersistenceAdapter implements AnalyticsQueryRepository {

    private final EntityManager entityManager;

    public AnalyticsQueryPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public BigDecimal sumRevenue(Instant start, Instant end) {
        String jpql = "SELECT SUM(o.totalAmount) FROM OrderEntity o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end";
        TypedQuery<BigDecimal> query = entityManager.createQuery(jpql, BigDecimal.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        BigDecimal result = query.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public long countOrders(Instant start, Instant end) {
        String jpql = "SELECT COUNT(o.id) FROM OrderEntity o WHERE o.createdAt BETWEEN :start AND :end";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        Long result = query.getSingleResult();
        return result != null ? result : 0L;
    }

    @Override
    public BigDecimal sumRefunds(Instant start, Instant end) {
        String jpql = "SELECT SUM(r.amount) FROM RefundEntity r WHERE r.status <> 'REJECTED' AND r.createdAt BETWEEN :start AND :end";
        TypedQuery<BigDecimal> query = entityManager.createQuery(jpql, BigDecimal.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        BigDecimal result = query.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public List<ProductMetric> findTopProducts(int limit) {
        String jpql = "SELECT o.productId, SUM(o.quantity), SUM(o.totalAmount) " +
                      "FROM OrderEntity o " +
                      "WHERE o.status = 'COMPLETED' " +
                      "GROUP BY o.productId " +
                      "ORDER BY SUM(o.quantity) DESC";
        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setMaxResults(limit)
                .getResultList();

        return results.stream()
                .map(row -> new ProductMetric(
                        (String) row[0],
                        (Long) row[1],
                        (BigDecimal) row[2]
                ))
                .toList();
    }

    @Override
    public long countUniqueCustomers(Instant start, Instant end) {
        String jpql = "SELECT COUNT(DISTINCT a.customerId) FROM CustomerActivityEntity a WHERE a.createdAt BETWEEN :start AND :end";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        Long result = query.getSingleResult();
        return result != null ? result : 0L;
    }

    @Override
    public List<ActivityMetric> countActivitiesByType(Instant start, Instant end) {
        String jpql = "SELECT a.activityType, COUNT(a.id) " +
                      "FROM CustomerActivityEntity a " +
                      "WHERE a.createdAt BETWEEN :start AND :end " +
                      "GROUP BY a.activityType";
        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        return results.stream()
                .map(row -> new ActivityMetric(
                        (String) row[0],
                        (Long) row[1]
                ))
                .toList();
    }

    @Override
    public Page<IngestedEventInfo> findIngestedEvents(Pageable pageable) {
        String countJpql = "SELECT COUNT(e.eventId) FROM IngestedEventEntity e";
        Long total = entityManager.createQuery(countJpql, Long.class).getSingleResult();

        String jpql = "SELECT e FROM IngestedEventEntity e ORDER BY e.occurredAt DESC";
        List<IngestedEventEntity> entities = entityManager.createQuery(jpql, IngestedEventEntity.class)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<IngestedEventInfo> list = entities.stream()
                .map(e -> new IngestedEventInfo(e.getEventId(), e.getEventType(), e.getOccurredAt(), e.getPayload()))
                .toList();

        return new PageImpl<>(list, pageable, total != null ? total : 0L);
    }
}
