package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class EventSystem extends AbstractNearbyPeerIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(EventSystem.class);

	private ComponentMapper<Event> eventMapper;

	private EventMessage reusableEventMessage;

	public EventSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Event.class), peers);

		reusableEventMessage = new EventMessage(MessageCodes.EVENT);
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int eventEntity, Set<VastPeer> nearbyPeers) {
		Event event = eventMapper.get(eventEntity);

		reusableEventMessage.getDataObject().clear();
		reusableEventMessage.getDataObject().set(MessageCodes.EVENT_ENTITY_ID, eventEntity);
		reusableEventMessage.getDataObject().set(MessageCodes.EVENT_EVENT, event.event);

		for (VastPeer nearbyPeer : nearbyPeers) {
			nearbyPeer.send(reusableEventMessage);
		}

		eventMapper.remove(eventEntity);
	}
}
