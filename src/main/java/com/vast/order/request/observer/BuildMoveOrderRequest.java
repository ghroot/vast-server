package com.vast.order.request.observer;

public class BuildMoveOrderRequest extends BuildOrderRequest {
    private int direction;

    public BuildMoveOrderRequest(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }
}
