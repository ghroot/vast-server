package com.vast.order.request.observer;

public class BuildRotateOrderRequest extends BuildOrderRequest {
    private int direction;

    public BuildRotateOrderRequest(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }
}
