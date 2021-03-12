package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Event;
import com.vast.component.Follow;
import com.vast.component.Type;
import com.vast.network.MessageCodes;

public class FollowOrderHandler implements OrderHandler {
	private ComponentMapper<Follow> followMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Event> eventMapper;

	public FollowOrderHandler() {
	}

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesMessageCode(short messageCode) {
		return messageCode == MessageCodes.FOLLOW;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return false;
	}

	@Override
	public void cancelOrder(int orderEntity) {
		followMapper.remove(orderEntity);
	}

	@Override
	public boolean startOrder(int orderEntity, short messageCode, DataObject dataObject) {
		int followEntity = (int) dataObject.get(MessageCodes.FOLLOW_ENTITY_ID).value;
		followMapper.create(orderEntity).entity = followEntity;
		if (typeMapper.has(followEntity) && typeMapper.get(followEntity).type.equals("animal")) {
			followMapper.get(orderEntity).distance = 3.0f;
			eventMapper.create(orderEntity).addEntry("message").setData("Easy, I'm not going to hurt you...").setOwnerPropagation();
		} else {
			eventMapper.create(orderEntity).addEntry("message").setData("I wonder where they are going...").setOwnerPropagation();
		}
		return true;
	}

	@Override
	public boolean modifyOrder(int orderEntity, short messageCode, DataObject dataObject) {
		return false;
	}
}
