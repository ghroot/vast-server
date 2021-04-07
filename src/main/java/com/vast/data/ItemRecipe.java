package com.vast.data;

import java.util.Set;

public class ItemRecipe extends Recipe {
    private int itemId = -1;
    private float duration;

    public ItemRecipe(int id, Set<Cost> costs, int itemId, float duration) {
        super(id, costs);
        this.itemId = itemId;
        this.duration = duration;
    }

    public int getItemId() {
        return itemId;
    }

    public float getDuration() {
        return duration;
    }
}
