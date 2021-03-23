package com.vast.order.request.avatar;

import com.vast.order.request.OrderRequest;

public class FollowOrderRequest extends OrderRequest {
    private int entity;

    public FollowOrderRequest(int entity) {
        this.entity = entity;
    }

    public int getEntity() {
        return entity;
    }
}
