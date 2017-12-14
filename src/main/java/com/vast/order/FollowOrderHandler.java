package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.component.Follow;

public class FollowOrderHandler implements OrderHandler {
	private ComponentMapper<Follow> followMapper;

	public FollowOrderHandler() {
	}

	@Override
	public void initialize() {
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.FOLLOW;
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
	public boolean startOrder(int orderEntity, DataObject dataObject) {
		int followEntity = (int) dataObject.get(MessageCodes.FOLLOW_ENTITY_ID).value;
		followMapper.create(orderEntity).entity = followEntity;
		return true;
	}
}
