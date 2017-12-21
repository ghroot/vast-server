package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;
import com.vast.component.Active;
import com.vast.component.Message;
import com.vast.component.Player;

import java.util.Map;

public class MessageSystem extends IteratingSystem {
	private ComponentMapper<Message> messageMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private Map<String, VastPeer> peers;

	private EventMessage reusableMessage;

	public MessageSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Message.class));
		this.peers = peers;

		reusableMessage = new EventMessage(MessageCodes.MESSAGE);
	}

	@Override

	protected void process(int entity) {
		Message message = messageMapper.get(entity);

		if (playerMapper.has(entity) && activeMapper.has(entity)) {
			VastPeer peer = peers.get(playerMapper.get(entity).name);
			reusableMessage.getDataObject().clear();
			reusableMessage.getDataObject().set(MessageCodes.MESSAGE_TEXT, message.text);
			reusableMessage.getDataObject().set(MessageCodes.MESSAGE_TYPE, (byte) message.type);
			peer.send(reusableMessage);
		}

		messageMapper.remove(entity);
	}
}
