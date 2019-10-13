package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.component.Active;
import com.vast.component.Event;
import com.vast.component.Know;
import com.vast.component.Player;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EventSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(EventSystem.class);

	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Know> knowMapper;

	@All({Player.class, Active.class, Know.class})
	private EntitySubscription interestedSubscription;

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
	protected void process(int eventEntity) {
		Event event = eventMapper.get(eventEntity);

		reusableEventMessage.getDataObject().clear();
		reusableEventMessage.getDataObject().set(MessageCodes.EVENT_ENTITY_ID, eventEntity);
		reusableEventMessage.getDataObject().set(MessageCodes.EVENT_NAME, event.name);

		if (event.ownerOnly) {
			if (playerMapper.has(eventEntity) && activeMapper.has(eventEntity)) {
				VastPeer ownerPeer = peers.get(playerMapper.get(eventEntity).name);
				ownerPeer.send(reusableEventMessage);
			}
		} else {
			IntBag interestedEntities = interestedSubscription.getEntities();
			for (int i = 0; i < interestedEntities.size(); i++) {
				int interestedEntity = interestedEntities.get(i);
				Know interestedKnow = knowMapper.get(interestedEntity);
				if (interestedKnow.knowEntities.contains(eventEntity)) {
					VastPeer nearbyPeer = peers.get(playerMapper.get(interestedEntity).name);
					nearbyPeer.send(reusableEventMessage);
				}
			}
		}

		eventMapper.remove(eventEntity);
	}
}
