package com.vast.order.request.observer;

import com.vast.order.request.OrderRequest;

import javax.vecmath.Point2f;

public class MoveObserverOrderRequest extends OrderRequest {
    private Point2f position;

    public MoveObserverOrderRequest(Point2f position) {
        this.position = position;
    }

    public Point2f getPosition() {
        return position;
    }
}
