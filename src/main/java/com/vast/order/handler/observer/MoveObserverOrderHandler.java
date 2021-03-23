package com.vast.order.handler.observer;

import com.artemis.ComponentMapper;
import com.vast.component.Parent;
import com.vast.component.Transform;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.observer.MoveObserverOrderRequest;
import com.vast.order.request.OrderRequest;

public class MoveObserverOrderHandler extends AbstractOrderHandler<MoveObserverOrderRequest> {
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Parent> parentMapper;

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof MoveObserverOrderRequest;
	}

	@Override
	public boolean isOrderComplete(int observerEntity) {
		return true;
	}

	@Override
	public void cancelOrder(int observerEntity) {
	}

	@Override
	public boolean startOrder(int observerEntity, MoveObserverOrderRequest moveObserverOrderRequest) {
		parentMapper.remove(observerEntity);
		transformMapper.get(observerEntity).position.set(moveObserverOrderRequest.getPosition());
		return true;
	}

	@Override
	public boolean modifyOrder(int observerEntity, MoveObserverOrderRequest moveObserverOrderRequest) {
		return false;
	}
}
