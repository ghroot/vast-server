package com.vast.order.handler.avatar;

import com.artemis.ComponentMapper;
import com.vast.component.Path;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.avatar.MoveOrderRequest;
import com.vast.order.request.OrderRequest;

public class MoveOrderHandler extends AbstractOrderHandler<MoveOrderRequest> {
	private ComponentMapper<Path> pathMapper;

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof MoveOrderRequest;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return !pathMapper.has(orderEntity);
	}

	@Override
	public void cancelOrder(int orderEntity) {
		pathMapper.remove(orderEntity);
	}

	@Override
	public boolean startOrder(int orderEntity, MoveOrderRequest moveOrderRequest) {
		pathMapper.create(orderEntity).targetPosition.set(moveOrderRequest.getPosition());
		return true;
	}

	@Override
	public boolean modifyOrder(int orderEntity, MoveOrderRequest moveOrderRequest) {
		pathMapper.get(orderEntity).targetPosition.set(moveOrderRequest.getPosition());
		return true;
	}
}
