package org.pancakelab.service;

import org.pancakelab.model.Order;
import org.pancakelab.model.pancakes.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PancakeService {
    private final List<Order> orders = new ArrayList<>();
    private final List<Pancake> pancakes = new ArrayList<>();

    public synchronized UUID createOrder(int building, int room) {
        Order order = new Order(building, room);
        orders.add(order);
        return order.getId();
    }

    public synchronized List<String> viewOrder(UUID orderId) {
        return pancakes.stream()
                       .filter(pancake -> pancake.getOrderId().equals(orderId))
                       .map(PancakeRecipe::description).toList();
    }

    private void addPancake(Pancake pancake, Order order) {
        pancake.setOrderId(order.getId());
        pancakes.add(pancake);

        OrderLog.logAddPancake(order, pancake.description(), pancakes);
    }

    public synchronized int removePancakes(String description, UUID orderId, int count) {
        Order order = findOrder(orderId);
        order.ensureStatus(Order.Status.OPEN);
        final AtomicInteger removedCount = new AtomicInteger(0);
        pancakes.removeIf(pancake -> {
            if (removedCount.get() < count
                    && pancake.getOrderId().equals(orderId)
                    && pancake.description().equals(description)) {
                removedCount.incrementAndGet();
                return true;
            }
            return false;
        });

        OrderLog.logRemovePancakes(order, description, removedCount.get(), pancakes);
        return removedCount.get();
    }

    public synchronized void cancelOrder(UUID orderId) {
        Order order = findOrder(orderId);
        order.ensureStatus(Order.Status.OPEN);
        OrderLog.logCancelOrder(order, this.pancakes);

        pancakes.removeIf(pancake -> pancake.getOrderId().equals(orderId));
        orders.removeIf(o -> o.getId().equals(orderId));
    }

    private Order findOrder(UUID orderId) {
        return orders.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    private Pancake findPancake(UUID orderId, UUID pancakeId) {
        return pancakes.stream()
                .filter(p -> p.getOrderId().equals(orderId) && p.getId().equals(pancakeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pancake " + pancakeId + " not found in order " + orderId));
    }

    public synchronized void completeOrder(UUID orderId) {
        findOrder(orderId).complete();
    }

    public synchronized Set<UUID> listCompletedOrders() {
        return ordersWithStatus(Order.Status.COMPLETED);
    }

    public synchronized Set<UUID> listPreparedOrders() {
        return ordersWithStatus(Order.Status.PREPARED);
    }

    private Set<UUID> ordersWithStatus(Order.Status status) {
        return orders.stream()
                .filter(o -> o.getStatus() == status)
                .map(Order::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    public synchronized void prepareOrder(UUID orderId) {
        findOrder(orderId).prepare();
    }

    public synchronized OrderSummary orderSummary(UUID orderId) {
        Order order = findOrder(orderId);
        return new OrderSummary(order.getId(), order.getBuilding(), order.getRoom(), viewOrder(orderId));
    }

    public synchronized OrderSummary deliverOrder(UUID orderId) {
        Order order = findOrder(orderId);
        order.ensureStatus(Order.Status.PREPARED);
        OrderSummary delivery = orderSummary(orderId);
        OrderLog.logDeliverOrder(order, this.pancakes);

        pancakes.removeIf(pancake -> pancake.getOrderId().equals(orderId));
        orders.removeIf(o -> o.getId().equals(orderId));

        return delivery;
    }

    public synchronized UUID createPancake(UUID orderId) {
        Order order = findOrder(orderId);
        order.ensureStatus(Order.Status.OPEN);
        Pancake pancake = new Pancake();
        addPancake(pancake, order);
        return pancake.getId();
    }

    public synchronized void addIngredient(UUID orderId, UUID pancakeId, String ingredient) {
        findOrder(orderId).ensureStatus(Order.Status.OPEN);
        findPancake(orderId, pancakeId).addIngredient(Ingredient.fromName(ingredient));
    }
}
