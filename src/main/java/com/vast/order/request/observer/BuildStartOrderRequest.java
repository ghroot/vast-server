package com.vast.order.request.observer;

public class BuildStartOrderRequest extends BuildOrderRequest {
    private int recipeId;

    public BuildStartOrderRequest(int recipeId) {
        this.recipeId = recipeId;
    }

    public int getRecipeId() {
        return recipeId;
    }
}
