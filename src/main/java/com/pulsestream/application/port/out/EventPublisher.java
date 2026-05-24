package com.pulsestream.application.port.out;

import com.pulsestream.domain.event.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
