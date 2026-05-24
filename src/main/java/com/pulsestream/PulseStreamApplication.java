package com.pulsestream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class PulseStreamApplication {
    public static void main(String[] args) {
        SpringApplication.run(PulseStreamApplication.class, args);
    }
}
