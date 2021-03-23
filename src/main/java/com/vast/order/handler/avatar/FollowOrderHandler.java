package com.vast.order.handler.avatar;

import com.artemis.ComponentMapper;
import com.vast.component.Event;
import com.vast.component.Follow;
import com.vast.component.Type;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.avatar.FollowOrderRequest;
import com.vast.order.request.OrderRequest;

public class FollowOrderHandler extends AbstractOrderHandler<FollowOrderRequest> {
	private ComponentMapper<Follow> followMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Event> eventMapper;

	public FollowOrderHandler() {
	}

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof FollowOrderRequest;
	}

	@Override
	public boolean isOrderComplete(int avatarEntity) {
		return false;
	}

	@Override
	public void cancelOrder(int avatarEntity) {
		followMapper.remove(avatarEntity);
	}

	@Override
	public boolean startOrder(int avatarEntity, FollowOrderRequest followOrderRequest) {
		followMapper.create(avatarEntity).entity = followOrderRequest.getEntity();
		if (typeMapper.has(followOrderRequest.getEntity()) && typeMapper.get(followOrderRequest.getEntity()).type.equals("animal")) {
			followMapper.get(avatarEntity).distance = 3.0f;
			eventMapper.create(avatarEntity).addEntry("message").setData("Easy, I'm not going to hurt you...").setOwnerPropagation();
		} else {
			eventMapper.create(avatarEntity).addEntry("message").setData("I wonder where they are going...").setOwnerPropagation();
		}
		return true;
	}

	@Override
	public boolean modifyOrder(int orderEntity, FollowOrderRequest followOrderRequest) {
		return false;
	}
}
