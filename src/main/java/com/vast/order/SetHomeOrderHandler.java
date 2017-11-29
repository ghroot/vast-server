package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Home;
import com.vast.component.Order;
import com.vast.component.Sync;
import com.vast.component.Transform;

public class SetHomeOrderHandler implements OrderHandler {
	private ComponentMapper<Home> homeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Sync> syncMapper;

	public SetHomeOrderHandler() {
	}

	@Override
	public void initialize() {
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.SET_HOME;
	}

	@Override
	public Order.Type getOrderType() {
		return Order.Type.SET_HOME;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return true;
	}

	@Override
	public void cancelOrder(int orderEntity) {
	}

	@Override
	public boolean startOrder(int orderEntity, DataObject dataObject) {
		homeMapper.create(orderEntity).position.set(transformMapper.get(orderEntity).position);
		syncMapper.create(orderEntity).markPropertyAsDirty(Properties.HOME);
		return true;
	}
}
