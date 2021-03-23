package com.vast.order.handler.avatar;

import com.artemis.ComponentMapper;
import com.vast.component.Event;
import com.vast.component.Home;
import com.vast.component.Sync;
import com.vast.component.Transform;
import com.vast.network.Properties;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.OrderRequest;
import com.vast.order.request.avatar.SetHomeOrderRequest;

public class SetHomeOrderHandler extends AbstractOrderHandler<SetHomeOrderRequest> {
	private ComponentMapper<Home> homeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;

	public SetHomeOrderHandler() {
	}

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof SetHomeOrderRequest;
	}

	@Override
	public boolean isOrderComplete(int avatarEntity) {
		return true;
	}

	@Override
	public void cancelOrder(int avatarEntity) {
	}

	@Override
	public boolean startOrder(int avatarEntity, SetHomeOrderRequest setHomeOrderRequest) {
		homeMapper.create(avatarEntity).position.set(transformMapper.get(avatarEntity).position);
		syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.HOME);
		eventMapper.create(avatarEntity).addEntry("message").setData("There is no place like home...").setOwnerPropagation();
		return true;
	}

	@Override
	public boolean modifyOrder(int avatarEntity, SetHomeOrderRequest setHomeOrderRequest) {
		return false;
	}
}
