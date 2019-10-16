package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.component.Active;
import com.vast.component.Event;
import com.vast.component.Known;
import com.vast.component.Player;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(EventSystem.class);

	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Known> knownMapper;

	private EventMessage reusableEventMessage;

	public EventSystem() {
		super(Aspect.all(Event.class));

		reusableEventMessage = new EventMessage(MessageCodes.EVENT);
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int eventEntity) {
		Event event = eventMapper.get(eventEntity);

		reusableEventMessage.getDataObject().clear();
		reusableEventMessage.getDataObject().set(MessageCodes.EVENT_ENTITY_ID, eventEntity);
		reusableEventMessage.getDataObject().set(MessageCodes.EVENT_NAME, event.name);

		if (event.ownerOnly) {
			if (playerMapper.has(eventEntity) && activeMapper.has(eventEntity)) {
				VastPeer ownerPeer = activeMapper.get(eventEntity).peer;
				ownerPeer.send(reusableEventMessage);
			}
		} else if (knownMapper.has(eventEntity)) {
			IntBag knownByEntitiesBag = knownMapper.get(eventEntity).knownByEntities;
			int[] knownByEntities = knownByEntitiesBag.getData();
			for (int i = 0, size = knownByEntitiesBag.size(); i < size; ++i) {
				int knownByEntity = knownByEntities[i];
				if (activeMapper.has(knownByEntity)) {
					VastPeer knownByPeer = activeMapper.get(knownByEntity).peer;
					knownByPeer.send(reusableEventMessage);
				}
			}
		}

		eventMapper.remove(eventEntity);
	}
}
