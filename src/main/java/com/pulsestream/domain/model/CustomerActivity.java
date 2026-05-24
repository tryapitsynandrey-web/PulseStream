package com.pulsestream.domain.model;

import java.time.Instant;
import java.util.Objects;

public class CustomerActivity {
    private final String id;
    private final String customerId;
    private final String activityType;
    private final String metadata;
    private final Instant createdAt;

    public CustomerActivity(String id, String customerId, String activityType, String metadata, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Activity ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.activityType = Objects.requireNonNull(activityType, "Activity type cannot be null");
        this.metadata = metadata; // can be null
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
    }

    // Getters
    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getActivityType() { return activityType; }
    public String getMetadata() { return metadata; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerActivity activity = (CustomerActivity) o;
        return Objects.equals(id, activity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
