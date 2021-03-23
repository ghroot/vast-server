package com.vast.order.handler.avatar;

import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.vast.component.Avatar;
import com.vast.component.Event;
import com.vast.component.Known;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.avatar.ChatOrderRequest;
import com.vast.order.request.OrderRequest;

public class ChatOrderHandler extends AbstractOrderHandler<ChatOrderRequest> {
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Avatar> avatarMapper;

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof ChatOrderRequest;
	}

	@Override
	public boolean isOrderComplete(int avatarEntity) {
		return true;
	}

	@Override
	public void cancelOrder(int avatarEntity) {
	}

	@Override
	public boolean startOrder(int avatarEntity, ChatOrderRequest chatOrderRequest) {
		IntBag knownByEntitiesBag = knownMapper.get(avatarEntity).knownByEntities;
		int[] knownByEntities = knownByEntitiesBag.getData();
		for (int i = 0, size = knownByEntitiesBag.size(); i < size; ++i) {
			int knownByEntity = knownByEntities[i];
			eventMapper.create(knownByEntity).addEntry("message")
					.setData(avatarMapper.get(avatarEntity).name + " says: " + chatOrderRequest.getMessage());
		}
		return true;
	}

	@Override
	public boolean modifyOrder(int avatarEntity, ChatOrderRequest chatOrderRequest) {
		return false;
	}
}
