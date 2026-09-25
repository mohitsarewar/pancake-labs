package org.pancakelab.model.pancakes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pancake implements PancakeRecipe {
    private final UUID id = UUID.randomUUID();
    private UUID orderId;
    private final List<Ingredient> ingredients = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void addIngredient(Ingredient ingredient) {
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
        return ingredients.stream().map(Ingredient::displayName).toList();
    }

}