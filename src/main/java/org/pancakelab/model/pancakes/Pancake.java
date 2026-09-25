package org.pancakelab.model.pancakes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pancake implements PancakeRecipe {
    private UUID orderId;
    private final List<String> ingredients = new ArrayList<>();

    public void addIngredient(String ingredient) {
        ingredients.add(ingredient);
    }

    @Override
    public UUID getOrderId() {
        return orderId;
    }

    @Override
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    @Override
    public List<String> ingredients() {
        return List.copyOf(ingredients);
    }
}