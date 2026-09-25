package org.pancakelab.service;

import java.util.List;
import java.util.UUID;

public record OrderSummary(UUID orderId, int building, int room, List<String> pancakes) {
    public OrderSummary {
        pancakes = List.copyOf(pancakes);
    }
}