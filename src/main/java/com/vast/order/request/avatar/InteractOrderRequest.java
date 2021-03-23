package com.vast.order.request.avatar;

import com.vast.order.request.OrderRequest;

public class InteractOrderRequest extends OrderRequest {
    private int entity;

    public InteractOrderRequest(int entity) {
        this.entity = entity;
    }

    public int getEntity() {
        return entity;
    }
}
