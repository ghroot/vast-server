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
	public boolean isOrderComplete(int avatarEntity) {
		return !pathMapper.has(avatarEntity);
	}

	@Override
	public void cancelOrder(int avatarEntity) {
		pathMapper.remove(avatarEntity);
	}

	@Override
	public boolean startOrder(int avatarEntity, MoveOrderRequest moveOrderRequest) {
		pathMapper.create(avatarEntity).targetPosition.set(moveOrderRequest.getPosition());
		return true;
	}

	@Override
	public boolean modifyOrder(int avatarEntity, MoveOrderRequest moveOrderRequest) {
		pathMapper.get(avatarEntity).targetPosition.set(moveOrderRequest.getPosition());
		return true;
	}
}
