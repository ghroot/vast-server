package com.vast.order.handler.observer;

import com.artemis.ComponentMapper;
import com.vast.component.Observer;
import com.vast.component.Parent;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.observer.AttachObserverOrderRequest;
import com.vast.order.request.OrderRequest;

public class AttachObserverOrderHandler extends AbstractOrderHandler<AttachObserverOrderRequest> {
	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Parent> parentMapper;

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof AttachObserverOrderRequest;
	}

	@Override
	public boolean isOrderComplete(int observerEntity) {
		return true;
	}

	@Override
	public void cancelOrder(int observerEntity) {
	}

	@Override
	public boolean startOrder(int observerEntity, AttachObserverOrderRequest attachObserverOrderRequest) {
		Observer observer = observerMapper.get(observerEntity);
		parentMapper.create(observerEntity).parentEntity = observer.observedEntity;
		return true;
	}

	@Override
	public boolean modifyOrder(int observerEntity, AttachObserverOrderRequest attachObserverOrderRequest) {
		return false;
	}
}
