package com.vast.order.request.avatar;

import com.vast.order.request.OrderRequest;

public class ChatOrderRequest extends OrderRequest {
    private String message;

    public ChatOrderRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
