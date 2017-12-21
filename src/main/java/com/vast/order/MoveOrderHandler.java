package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.network.MessageCodes;
import com.vast.component.Path;

public class MoveOrderHandler implements OrderHandler {
	private ComponentMapper<Path> pathMapper;

	@Override
	public void initialize() {
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.MOVE;
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
	public boolean startOrder(int orderEntity, DataObject dataObject) {
		float[] position = (float[]) dataObject.get(MessageCodes.MOVE_POSITION).value;
		pathMapper.create(orderEntity).targetPosition.set(position[0], position[1]);
		return true;
	}
}
