package com.vast.order.handler;

import com.vast.order.request.OrderRequest;

public interface OrderHandler {
	void initialize();
	boolean handlesRequest(OrderRequest request);
	boolean isOrderComplete(int orderEntity);
	void cancelOrder(int orderEntity);
	boolean startOrder(int orderEntity, OrderRequest request);
	boolean modifyOrder(int orderEntity, OrderRequest request);
}
