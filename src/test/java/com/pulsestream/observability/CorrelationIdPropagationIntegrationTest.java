package com.pulsestream.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsestream.AbstractIntegrationTest;
import com.pulsestream.application.port.in.IngestEventUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SuppressWarnings("null")
class CorrelationIdPropagationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IngestEventUseCase ingestEventUseCase;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldPropagateSuppliedCorrelationIdHeader() throws Exception {
        String testCorrelationId = "custom-corr-id-12345";
        Map<String, Object> body = Map.of(
                "customerId", "cust-001",
                "productId", "prod-001",
                "quantity", 2,
                "price", new BigDecimal("15.00")
        );

        when(ingestEventUseCase.ingestOrder(any())).thenReturn(
                new IngestEventUseCase.IngestionResult("evt-123", "order-123", "ACCEPTED", Instant.now())
        );

        mockMvc.perform(post("/api/v1/events/orders")
                        .with(csrf())
                        .header("X-Correlation-Id", testCorrelationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isAccepted())
                .andExpect(header().string("X-Correlation-Id", testCorrelationId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGenerateCorrelationIdHeaderIfMissing() throws Exception {
        Map<String, Object> body = Map.of(
                "customerId", "cust-001",
                "productId", "prod-001",
                "quantity", 2,
                "price", new BigDecimal("15.00")
        );

        when(ingestEventUseCase.ingestOrder(any())).thenReturn(
                new IngestEventUseCase.IngestionResult("evt-123", "order-123", "ACCEPTED", Instant.now())
        );

        mockMvc.perform(post("/api/v1/events/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isAccepted())
                .andExpect(header().exists("X-Correlation-Id"));
    }
}
