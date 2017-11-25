package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.component.Event;
import com.vast.component.Order;

public class EmoteOrderHandler implements OrderHandler {
	private ComponentMapper<Event> eventMapper;

	@Override
	public void initialize() {
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.EMOTE;
	}

	@Override
	public Order.Type getOrderType() {
		return Order.Type.EMOTE;
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
		int emoteType = (byte) dataObject.get(MessageCodes.EMOTE_TYPE).value;
		eventMapper.create(orderEntity).name = "emote" + emoteType;
		return true;
	}
}
