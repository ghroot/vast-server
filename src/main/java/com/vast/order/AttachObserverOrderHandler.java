package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Observed;
import com.vast.component.Parent;
import com.vast.network.MessageCodes;

public class AttachObserverOrderHandler implements OrderHandler {
	private ComponentMapper<Observed> observedMapper;
	private ComponentMapper<Parent> parentMapper;

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesMessageCode(short messageCode) {
		return messageCode == MessageCodes.ATTACH_OBSERVER;
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
		int observerEntity = observedMapper.get(orderEntity).observerEntity;
		parentMapper.create(observerEntity).parentEntity = orderEntity;
		return true;
	}

	@Override
	public boolean modifyOrder(int orderEntity, short messageCode, DataObject dataObject) {
		return false;
	}
}
