package com.vast.order.request.avatar;

import com.vast.order.request.OrderRequest;

public class CraftOrderRequest extends OrderRequest {
    private int recipeId;

    public CraftOrderRequest(int recipeId) {
        this.recipeId = recipeId;
    }

    public int getRecipeId() {
        return recipeId;
    }
}
