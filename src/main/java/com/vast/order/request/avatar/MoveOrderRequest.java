package com.vast.order.request.avatar;

import com.vast.order.request.OrderRequest;

import javax.vecmath.Point2f;

public class MoveOrderRequest extends OrderRequest {
    private Point2f position;

    public MoveOrderRequest(Point2f position) {
        this.position = position;
    }

    public Point2f getPosition() {
        return position;
    }
}
