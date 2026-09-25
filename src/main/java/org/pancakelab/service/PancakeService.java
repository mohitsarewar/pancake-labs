package org.pancakelab.service;

import org.pancakelab.model.Order;
import org.pancakelab.model.pancakes.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PancakeService {
    private List<Order>         orders          = new ArrayList<>();
    private Set<UUID>           completedOrders = new HashSet<>();
    private Set<UUID>           preparedOrders  = new HashSet<>();
    private List<Pancake> pancakes        = new ArrayList<>();

    public Order createOrder(int building, int room) {
        Order order = new Order(building, room);
        orders.add(order);
        return order;
    }

    public void addDarkChocolatePancake(UUID orderId, int count) {
        for (int i = 0; i < count; ++i) {
            addPancake(pancakeWith("dark chocolate"),
                       orders.stream().filter(o -> o.getId().equals(orderId)).findFirst().get());
        }
    }

    public void addDarkChocolateWhippedCreamPancake(UUID orderId, int count) {
        for (int i = 0; i < count; ++i) {
            addPancake(pancakeWith("dark chocolate", "whipped cream"),
                       orders.stream().filter(o -> o.getId().equals(orderId)).findFirst().get());
        }
    }

    public void addDarkChocolateWhippedCreamHazelnutsPancake(UUID orderId, int count) {
        for (int i = 0; i < count; ++i) {
            addPancake(pancakeWith("dark chocolate", "whipped cream", "hazelnuts"),
                       orders.stream().filter(o -> o.getId().equals(orderId)).findFirst().get());
        }
    }

    public void addMilkChocolatePancake(UUID orderId, int count) {
        for (int i = 0; i < count; ++i) {
            addPancake(pancakeWith("milk chocolate"),
                       orders.stream().filter(o -> o.getId().equals(orderId)).findFirst().get());
        }
    }

    public void addMilkChocolateHazelnutsPancake(UUID orderId, int count) {
        for (int i = 0; i < count; ++i) {
            addPancake(pancakeWith("milk chocolate", "hazelnuts"),
                       orders.stream().filter(o -> o.getId().equals(orderId)).findFirst().get());
        }
    }

    public List<String> viewOrder(UUID orderId) {
        return pancakes.stream()
                       .filter(pancake -> pancake.getOrderId().equals(orderId))
                       .map(PancakeRecipe::description).toList();
    }

    private void addPancake(Pancake pancake, Order order) {
        pancake.setOrderId(order.getId());
        pancakes.add(pancake);

        OrderLog.logAddPancake(order, pancake.description(), pancakes);
    }

    public void removePancakes(String description, UUID orderId, int count) {
        final AtomicInteger removedCount = new AtomicInteger(0);
        pancakes.removeIf(pancake -> {
            return pancake.getOrderId().equals(orderId) &&
                   pancake.description().equals(description) &&
                   removedCount.getAndIncrement() < count;
        });

        Order order = orders.stream().filter(o -> o.getId().equals(orderId)).findFirst().get();
        OrderLog.logRemovePancakes(order, description, removedCount.get(), pancakes);
    }

    public void cancelOrder(UUID orderId) {
        Order order = orders.stream().filter(o -> o.getId().equals(orderId)).findFirst().get();
        OrderLog.logCancelOrder(order, this.pancakes);

        pancakes.removeIf(pancake -> pancake.getOrderId().equals(orderId));
        orders.removeIf(o -> o.getId().equals(orderId));
        completedOrders.removeIf(u -> u.equals(orderId));
        preparedOrders.removeIf(u -> u.equals(orderId));

        OrderLog.logCancelOrder(order,pancakes);
    }

    private Pancake pancakeWith(String... ingredients) {
        Pancake pancake = new Pancake();
        for (String ingredient : ingredients) {
            pancake.addIngredient(Ingredient.fromName(ingredient));
        }
        return pancake;
    }

    public void completeOrder(UUID orderId) {
        completedOrders.add(orderId);
    }

    public Set<UUID> listCompletedOrders() {
        return completedOrders;
    }

    public void prepareOrder(UUID orderId) {
        preparedOrders.add(orderId);
        completedOrders.removeIf(u -> u.equals(orderId));
    }

    public Set<UUID> listPreparedOrders() {
        return preparedOrders;
    }

    public Object[] deliverOrder(UUID orderId) {
        if (!preparedOrders.contains(orderId)) return null;

        Order order = orders.stream().filter(o -> o.getId().equals(orderId)).findFirst().get();
        List<String> pancakesToDeliver = viewOrder(orderId);
        OrderLog.logDeliverOrder(order, this.pancakes);

        pancakes.removeIf(pancake -> pancake.getOrderId().equals(orderId));
        orders.removeIf(o -> o.getId().equals(orderId));
        preparedOrders.removeIf(u -> u.equals(orderId));

        return new Object[] {order, pancakesToDeliver};
    }

    public UUID createPancake(UUID orderId) {
        Order order = orders.stream().filter(o -> o.getId().equals(orderId)).findFirst().get();
        Pancake pancake = new Pancake();
        addPancake(pancake, order);
        return pancake.getId();
    }

    public void addIngredient(UUID orderId, UUID pancakeId, String ingredient) {
        Pancake pancake = pancakes.stream()
                .filter(p -> p.getOrderId().equals(orderId) && p.getId().equals(pancakeId))
                .findFirst().get();
        pancake.addIngredient(Ingredient.fromName(ingredient));
    }
}
