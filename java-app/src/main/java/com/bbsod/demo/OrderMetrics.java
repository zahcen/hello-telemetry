package com.bbsod.demo;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;


public class OrderMetrics {

    private final LongCounter ordersCounter;

    public OrderMetrics() {
        // Get the global Meter from the attached OpenTelemetry agent
        Meter meter = GlobalOpenTelemetry.getMeter("orders-service");

        // Create a counter metric
        LongCounterBuilder builder = meter.counterBuilder("ht_orders_total");
        builder .setDescription("Total number of orders created");
        builder .setUnit("ht_orders");
        ordersCounter = builder.build();
    }

    public void incrementOrderCount(String customerId, double amount) {
        // Record a new order with attributes
        ordersCounter.add(1, Attributes.of(
                AttributeKey.stringKey("ht_customer_id"), customerId,
                AttributeKey.doubleKey("ht_order_amount"), amount
        ));
    }
}
