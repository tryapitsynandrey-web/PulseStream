package com.pulsestream.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsestream.application.port.in.IngestEventUseCase;
import com.pulsestream.security.CustomUserDetailsService;
import com.pulsestream.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventIngestionController.class)
@SuppressWarnings("null")
class EventIngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IngestEventUseCase ingestEventUseCase;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAcceptValidOrderIngestRequest() throws Exception {
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
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.eventId").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectOrderIngestRequestWithMissingCustomerId() throws Exception {
        Map<String, Object> body = Map.of(
                "productId", "prod-001",
                "quantity", 2,
                "price", new BigDecimal("15.00")
        );

        mockMvc.perform(post("/api/v1/events/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUnauthenticatedOrderIngestRequest() throws Exception {
        Map<String, Object> body = Map.of(
                "customerId", "cust-001",
                "productId", "prod-001",
                "quantity", 2,
                "price", new BigDecimal("15.00")
        );

        mockMvc.perform(post("/api/v1/events/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }
}
