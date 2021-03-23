package com.vast.order.handler.avatar;

import com.artemis.ComponentMapper;
import com.vast.component.Event;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.avatar.EmoteOrderRequest;
import com.vast.order.request.OrderRequest;

public class EmoteOrderHandler extends AbstractOrderHandler<EmoteOrderRequest> {
	private ComponentMapper<Event> eventMapper;

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof EmoteOrderRequest;
	}

	@Override
	public boolean isOrderComplete(int avatarEntity) {
		return true;
	}

	@Override
	public void cancelOrder(int avatarEntity) {
	}

	@Override
	public boolean startOrder(int avatarEntity, EmoteOrderRequest emoteOrderRequest) {
		eventMapper.create(avatarEntity).addEntry("emoted").setData(emoteOrderRequest.getType());
		return true;
	}

	@Override
	public boolean modifyOrder(int avatarEntity, EmoteOrderRequest emoteOrderRequest) {
		return false;
	}
}
