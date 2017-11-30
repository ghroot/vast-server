package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.VastPeer;
import com.vast.component.*;
import com.vast.property.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CullingSystem extends AbstractNearbyEntityIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CullingSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SubType> subTypeMapper;

	private Map<String, VastPeer> peers;
	private Set<PropertyHandler> propertyHandlers;

	private List<Integer> reusableRemovedEntities;
	private EventMessage reusableDestroyedEventMessage;
	private EventMessage reusableCreatedEventMessage;

	public CullingSystem(Map<String, VastPeer> peers, Set<PropertyHandler> propertyHandlers) {
		super(Aspect.all(Player.class, Active.class, Known.class));
		this.peers = peers;
		this.propertyHandlers = propertyHandlers;

		reusableRemovedEntities = new ArrayList<Integer>();
		reusableDestroyedEventMessage = new EventMessage(MessageCodes.ENTITY_DESTROYED);
		reusableCreatedEventMessage = new EventMessage(MessageCodes.ENTITY_CREATED);
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int activePlayerEntity, Set<Integer> nearbyEntities) {
		Player player = playerMapper.get(activePlayerEntity);
		Known known = knownMapper.get(activePlayerEntity);
		if (peers.containsKey(player.name)) {
			VastPeer peer = peers.get(player.name);
			notifyAboutRemovedEntities(peer, nearbyEntities, known.knownEntities);
			notifyAboutNewEntities(peer, nearbyEntities, known.knownEntities);
		}
	}

	private void notifyAboutRemovedEntities(VastPeer peer, Set<Integer> nearbyEntities, Set<Integer> knownEntities) {
		reusableRemovedEntities.clear();
		for (int knownEntity : knownEntities) {
			if (!nearbyEntities.contains(knownEntity)) {
				notifyAboutRemovedEntity(peer, knownEntity);
				reusableRemovedEntities.add(knownEntity);
			}
		}
		knownEntities.removeAll(reusableRemovedEntities);
	}

	private void notifyAboutRemovedEntity(VastPeer peer, int deletedEntity) {
		logger.debug("Notifying peer {} about removed entity {} (culling)", peer.getName(), deletedEntity);
		reusableDestroyedEventMessage.getDataObject().clear();
		reusableDestroyedEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, deletedEntity);
		reusableDestroyedEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_REASON, "culling");
		peer.send(reusableDestroyedEventMessage);
	}

	private void notifyAboutNewEntities(VastPeer peer, Set<Integer> nearbyEntities, Set<Integer> knownEntities) {
		for (int nearbyEntity : nearbyEntities) {
			if (!knownEntities.contains(nearbyEntity) && typeMapper.has(nearbyEntity)) {
				notifyAboutNewEntity(peer, nearbyEntity);
				knownEntities.add(nearbyEntity);
			}
		}
	}

	private void notifyAboutNewEntity(VastPeer peer, int newEntity) {
		logger.debug("Notifying peer {} about new entity {} (culling)", peer.getName(), newEntity);
		reusableCreatedEventMessage.getDataObject().clear();
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_ENTITY_ID, newEntity);
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_TYPE, typeMapper.get(newEntity).type);
		if (subTypeMapper.has(newEntity)) {
			reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_SUB_TYPE, subTypeMapper.get(newEntity).subType);
		}
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_REASON, "culling");
		if (playerMapper.has(newEntity)) {
			reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_OWNER, peer.getName().equals(playerMapper.get(newEntity).name));
		}
		for (PropertyHandler propertyHandler : propertyHandlers) {
			propertyHandler.decorateDataObject(newEntity, reusableCreatedEventMessage.getDataObject(), true);
		}
		peer.send(reusableCreatedEventMessage);
	}
}
