package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.component.Event;

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
	public boolean isOrderComplete(int orderEntity) {
		return true;
	}

	@Override
	public void cancelOrder(int orderEntity) {
	}

	@Override
	public boolean startOrder(int orderEntity, DataObject dataObject) {
		int emoteType = (byte) dataObject.get(MessageCodes.EMOTE_TYPE).value;
		eventMapper.create(orderEntity).name = "emoted";
		return true;
	}
}
