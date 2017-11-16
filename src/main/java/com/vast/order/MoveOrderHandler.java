package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.component.Order;
import com.vast.component.Path;

import javax.vecmath.Point2f;

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
	public Order.Type getOrderType() {
		return Order.Type.MOVE;
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
		if (!pathMapper.has(orderEntity)) {
			pathMapper.create(orderEntity);
		}
		float[] position = (float[]) dataObject.get(MessageCodes.MOVE_POSITION).value;
		pathMapper.get(orderEntity).targetPosition = new Point2f(position[0], position[1]);
		return true;
	}
}
