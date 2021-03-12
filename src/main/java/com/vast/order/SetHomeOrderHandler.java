package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Event;
import com.vast.component.Home;
import com.vast.component.Sync;
import com.vast.component.Transform;
import com.vast.network.Properties;
import com.vast.network.MessageCodes;

public class SetHomeOrderHandler implements OrderHandler {
	private ComponentMapper<Home> homeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;

	public SetHomeOrderHandler() {
	}

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesMessageCode(short messageCode) {
		return messageCode == MessageCodes.SET_HOME;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return true;
	}

	@Override
	public void cancelOrder(int orderEntity) {
	}

	@Override
	public boolean startOrder(int orderEntity, short messageCode, DataObject dataObject) {
		homeMapper.create(orderEntity).position.set(transformMapper.get(orderEntity).position);
		syncMapper.create(orderEntity).markPropertyAsDirty(Properties.HOME);
		eventMapper.create(orderEntity).addEntry("message").setData("There is no place like home...").setOwnerPropagation();
		return true;
	}

	@Override
	public boolean modifyOrder(int orderEntity, short messageCode, DataObject dataObject) {
		return false;
	}
}
