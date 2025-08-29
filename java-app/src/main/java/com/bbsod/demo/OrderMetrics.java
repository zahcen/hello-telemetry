package com.bbsod.demo;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributeKey;

public class OrderMetrics {

    private final LongCounter ordersCounter;

    public OrderMetrics() {
        // Get the global Meter from the attached OpenTelemetry agent
        Meter meter = GlobalOpenTelemetry.getMeter("orders-service");

        // Create a counter metric
        ordersCounter = meter.counterBuilder("orders_total")
                .setDescription("Total number of orders created")
                .setUnit("orders")
                .build();
    }

    public void incrementOrderCount(String customerId, double amount) {
        // Record a new order with attributes
        ordersCounter.add(1, Attributes.of(
                AttributeKey.stringKey("customer_id"), customerId,
                AttributeKey.doubleKey("order_amount"), amount
        ));
    }
}
