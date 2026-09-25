package org.pancakelab.model.pancakes;

public enum Ingredient {
    DARK_CHOCOLATE("dark chocolate"),
    MILK_CHOCOLATE("milk chocolate"),
    WHIPPED_CREAM("whipped cream"),
    HAZELNUTS("hazelnuts");

    private final String displayName;

    Ingredient(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static Ingredient fromName(String name) {
        for (Ingredient ingredient : values()) {
            if (ingredient.displayName.equals(name)) {
                return ingredient;
            }
        }
        throw new IllegalArgumentException("Unknown ingredient: " + name);
    }
}