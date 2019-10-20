package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.component.Active;
import com.vast.component.Message;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;

public class MessageSystem extends IteratingSystem {
	private ComponentMapper<Message> messageMapper;
	private ComponentMapper<Active> activeMapper;

	private EventMessage reusableMessage;

	public MessageSystem() {
		super(Aspect.all(Message.class));

		reusableMessage = new EventMessage(MessageCodes.MESSAGE);
	}

	@Override
	protected void process(int entity) {
		Message message = messageMapper.get(entity);

		if (activeMapper.has(entity)) {
			VastPeer peer = activeMapper.get(entity).peer;
			reusableMessage.getDataObject().clear();
			reusableMessage.getDataObject().set(MessageCodes.MESSAGE_TEXT, message.text);
			reusableMessage.getDataObject().set(MessageCodes.MESSAGE_TYPE, (byte) message.type);
			peer.send(reusableMessage);
		}

		messageMapper.remove(entity);
	}
}
