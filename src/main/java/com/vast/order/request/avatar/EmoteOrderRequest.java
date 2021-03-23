package com.vast.order.request.avatar;

import com.vast.order.request.OrderRequest;

public class EmoteOrderRequest extends OrderRequest {
    private int type;

    public EmoteOrderRequest(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
