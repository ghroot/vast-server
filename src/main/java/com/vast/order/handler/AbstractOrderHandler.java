package com.vast.order.handler;

import com.vast.order.request.OrderRequest;

public abstract class AbstractOrderHandler<TRequest> implements OrderHandler {
    @Override
    @SuppressWarnings("unchecked")
    public final boolean startOrder(int orderEntity, OrderRequest request) {
        return startOrder(orderEntity, (TRequest) request);
    }

    protected abstract boolean startOrder(int orderEntity, TRequest request);

    @Override
    @SuppressWarnings("unchecked")
    public final boolean modifyOrder(int orderEntity, OrderRequest request) {
        return modifyOrder(orderEntity, (TRequest) request);
    }

    public abstract boolean modifyOrder(int orderEntity, TRequest request);
}
