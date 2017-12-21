package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.network.MessageCodes;
import com.vast.component.Follow;
import com.vast.component.Message;

public class FollowOrderHandler implements OrderHandler {
	private ComponentMapper<Follow> followMapper;
	private ComponentMapper<Message> messageMapper;

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
		messageMapper.create(orderEntity).text = "I wonder where they are going...";
		messageMapper.get(orderEntity).type = 1;
		return true;
	}
}
