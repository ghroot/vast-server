package com.vast.data;

import java.util.Set;

public class EntityRecipe extends Recipe {
    private String entityType;

    public EntityRecipe(int id, Set<Cost> costs, String entityType) {
        super(id, costs);
        this.entityType = entityType;
    }

    public String getEntityType() {
        return entityType;
    }
}
