package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Path;
import com.vast.network.MessageCodes;

public class MoveOrderHandler implements OrderHandler {
	private ComponentMapper<Path> pathMapper;

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesMessageCode(short messageCode) {
		return messageCode == MessageCodes.MOVE;
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
	public boolean startOrder(int orderEntity, short messageCode, DataObject dataObject) {
		float[] position = (float[]) dataObject.get(MessageCodes.MOVE_POSITION).value;
		pathMapper.create(orderEntity).targetPosition.set(position[0], position[1]);
		return true;
	}

	@Override
	public boolean modifyOrder(int orderEntity, short messageCode, DataObject dataObject) {
		float[] position = (float[]) dataObject.get(MessageCodes.MOVE_POSITION).value;
		pathMapper.get(orderEntity).targetPosition.set(position[0], position[1]);
		return true;
	}
}
