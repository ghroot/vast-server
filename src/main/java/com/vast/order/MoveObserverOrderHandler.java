package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.*;
import com.vast.network.MessageCodes;

public class MoveObserverOrderHandler implements OrderHandler {
	private ComponentMapper<Observed> observedMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Parent> parentMapper;

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesMessageCode(short messageCode) {
		return messageCode == MessageCodes.MOVE_OBSERVER;
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
		float[] position = (float[]) dataObject.get(MessageCodes.MOVE_OBSERVER_POSITION).value;
		int observerEntity = observedMapper.get(orderEntity).observerEntity;
		parentMapper.remove(observerEntity);
		transformMapper.get(observerEntity).position.set(position[0], position[1]);
		return true;
	}

	@Override
	public boolean modifyOrder(int orderEntity, short messageCode, DataObject dataObject) {
		return false;
	}
}
