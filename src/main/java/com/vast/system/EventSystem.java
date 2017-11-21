package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.Active;
import com.vast.component.Event;
import com.vast.component.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class EventSystem extends AbstractNearbyEntityIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(EventSystem.class);

	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private Map<String, VastPeer> peers;
	private EventMessage reusableEventMessage;

	public EventSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Event.class));
		this.peers = peers;

		reusableEventMessage = new EventMessage(MessageCodes.EVENT);
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int eventEntity, Set<Integer> nearbyEntities) {
		Event event = eventMapper.get(eventEntity);

		reusableEventMessage.getDataObject().clear();
		reusableEventMessage.getDataObject().set(MessageCodes.EVENT_ENTITY_ID, eventEntity);
		reusableEventMessage.getDataObject().set(MessageCodes.EVENT_NAME, event.name);

		for (int nearbyEntity : nearbyEntities) {
			if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity)) {
				VastPeer nearbyPeer = peers.get(playerMapper.get(nearbyEntity).name);
				nearbyPeer.send(reusableEventMessage);
			}
		}

		eventMapper.remove(eventEntity);
	}
}
