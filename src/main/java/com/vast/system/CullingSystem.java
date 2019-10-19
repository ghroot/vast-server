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

import java.util.Set;

public class CullingSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CullingSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;

	private Set<PropertyHandler> propertyHandlers;

	private IntBag reusableRemovedEntities;
	private EventMessage reusableDestroyedEventMessage;
	private EventMessage reusableCreatedEventMessage;

	public CullingSystem(Set<PropertyHandler> propertyHandlers) {
		super(Aspect.all(Scan.class, Active.class));
		this.propertyHandlers = propertyHandlers;

		reusableRemovedEntities = new IntBag();
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
		Scan scan = scanMapper.get(entity);
		Active active = activeMapper.get(entity);

		notifyAboutRemovedEntities(active.peer, entity, scan, active);
		notifyAboutNewEntities(active.peer, entity, scan, active);
	}

	private void notifyAboutRemovedEntities(VastPeer peer, int entity, Scan scan, Active active) {
		reusableRemovedEntities.clear();
		int[] knowEntities = active.knowEntities.getData();
		for (int i = 0, size = active.knowEntities.size(); i < size; ++i) {
			int knowEntity = knowEntities[i];
			if (!scan.nearbyEntities.contains(knowEntity)) {
				notifyAboutRemovedEntity(peer, knowEntity);
				reusableRemovedEntities.add(knowEntity);
				if (knownMapper.has(knowEntity)) {
					knownMapper.get(knowEntity).knownByEntities.removeValue(entity);
				}
			}
		}
		int[] entitiesToRemove = reusableRemovedEntities.getData();
		for (int i = 0, size = reusableRemovedEntities.size(); i < size; ++i) {
			int entityToRemove = entitiesToRemove[i];
			active.knowEntities.removeValue(entityToRemove);
		}
	}

	private void notifyAboutRemovedEntity(VastPeer peer, int deletedEntity) {
		logger.debug("Notifying peer {} about removed entity {} (culling)", peer.getName(), deletedEntity);
		reusableDestroyedEventMessage.getDataObject().clear();
		reusableDestroyedEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, deletedEntity);
		reusableDestroyedEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_REASON, "culling");
		peer.send(reusableDestroyedEventMessage);
	}

	private void notifyAboutNewEntities(VastPeer peer, int entity, Scan scan, Active active) {
		int[] nearbyEntities = scan.nearbyEntities.getData();
		for (int i = 0, size = scan.nearbyEntities.size(); i < size; ++i) {
			int nearbyEntity = nearbyEntities[i];
			if (!active.knowEntities.contains(nearbyEntity)) {
				notifyAboutNewEntity(peer, entity, nearbyEntity);
				active.knowEntities.add(nearbyEntity);
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
