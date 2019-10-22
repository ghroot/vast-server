package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Active;
import com.vast.component.Known;
import com.vast.component.Message;
import com.vast.component.Player;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;

public class ChatOrderHandler implements OrderHandler {
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Message> messageMapper;

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
			messageMapper.create(knownByEntity).text = playerMapper.get(orderEntity).name + " says: " + word;
		}
		return true;
	}
}
