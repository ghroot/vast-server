package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.component.*;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CullingSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CullingSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Know> knowMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;

	private Map<String, VastPeer> peers;
	private Set<PropertyHandler> propertyHandlers;

	private List<Integer> reusableRemovedEntities;
	private EventMessage reusableDestroyedEventMessage;
	private EventMessage reusableCreatedEventMessage;

	public CullingSystem(Map<String, VastPeer> peers, Set<PropertyHandler> propertyHandlers) {
		super(Aspect.all(Scan.class, Know.class));
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
	protected void process(int entity) {
		Player player = playerMapper.get(entity);
		Scan scan = scanMapper.get(entity);
		Know know = knowMapper.get(entity);

		VastPeer peer = null;
		if (player != null && peers.containsKey(player.name)) {
			peer = peers.get(player.name);
		}

		notifyAboutRemovedEntities(peer, entity, scan, know);
		notifyAboutNewEntities(peer, entity, scan, know);
	}

	private void notifyAboutRemovedEntities(VastPeer peer, int entity, Scan scan, Know know) {
		reusableRemovedEntities.clear();
		for (int knowEntity : know.knowEntities) {
			if (!scan.nearbyEntities.contains(knowEntity)) {
				if (peer != null) {
					notifyAboutRemovedEntity(peer, knowEntity);
				}
				reusableRemovedEntities.add(knowEntity);
				if (knownMapper.has(knowEntity)) {
					knownMapper.get(knowEntity).knownByEntities.remove(entity);
				}
			}
		}
		know.knowEntities.removeAll(reusableRemovedEntities);
	}

	private void notifyAboutRemovedEntity(VastPeer peer, int deletedEntity) {
		logger.debug("Notifying peer {} about removed entity {} (culling)", peer.getName(), deletedEntity);
		reusableDestroyedEventMessage.getDataObject().clear();
		reusableDestroyedEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, deletedEntity);
		reusableDestroyedEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_REASON, "culling");
		peer.send(reusableDestroyedEventMessage);
	}

	private void notifyAboutNewEntities(VastPeer peer, int entity, Scan scan, Know know) {
		for (int nearbyEntity : scan.nearbyEntities) {
			if (!know.knowEntities.contains(nearbyEntity) && typeMapper.has(nearbyEntity)) {
				if (peer != null) {
					notifyAboutNewEntity(peer, entity, nearbyEntity);
				}
				know.knowEntities.add(nearbyEntity);
				if (knownMapper.has(nearbyEntity)) {
					knownMapper.get(nearbyEntity).knownByEntities.add(entity);
				}
			}
		}
	}

	private void notifyAboutNewEntity(VastPeer peer, int entity, int newEntity) {
		logger.debug("Notifying peer {} about new entity {} (culling)", peer.getName(), newEntity);
		SyncPropagation syncPropagation = syncPropagationMapper.get(newEntity);
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
		DataObject propertiesDataObject = new DataObject();
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_PROPERTIES, propertiesDataObject);
		for (PropertyHandler propertyHandler : propertyHandlers) {
			if (newEntity == entity || syncPropagation.isNearbyPropagation(propertyHandler.getProperty())) {
				propertyHandler.decorateDataObject(newEntity, propertiesDataObject, true);
			}
		}
		peer.send(reusableCreatedEventMessage);
	}
}
