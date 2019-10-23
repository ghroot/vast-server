package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Event;
import com.vast.component.Known;
import com.vast.component.Player;
import com.vast.network.MessageCodes;

public class ChatOrderHandler implements OrderHandler {
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Event> eventMapper;

	@Override
	public void initialize() {
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.CHAT;
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
		String word = (String) dataObject.get(MessageCodes.CHAT_WORD).value;
		IntBag knownByEntitiesBag = knownMapper.get(orderEntity).knownByEntities;
		int[] knownByEntities = knownByEntitiesBag.getData();
		for (int i = 0, size = knownByEntitiesBag.size(); i < size; ++i) {
			int knownByEntity = knownByEntities[i];
			eventMapper.create(knownByEntity).setType("message").setData(playerMapper.get(orderEntity).name + " says: " + word);
		}
		return true;
	}
}