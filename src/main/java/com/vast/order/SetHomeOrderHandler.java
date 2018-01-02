package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Home;
import com.vast.component.Message;
import com.vast.component.Sync;
import com.vast.component.Transform;
import com.vast.data.Properties;
import com.vast.network.MessageCodes;

public class SetHomeOrderHandler implements OrderHandler {
	private ComponentMapper<Home> homeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Message> messageMapper;

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
		messageMapper.create(orderEntity).text = "There is no place like home...";
		messageMapper.get(orderEntity).type = 1;
		return true;
	}
}
