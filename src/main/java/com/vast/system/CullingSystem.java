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
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class CullingSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CullingSystem.class);

	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Avatar> avatarMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;
	private ComponentMapper<Invisible> invisibleMapper;

	private PropertyHandler[] propertyHandlers;

	private IntBag reusableRemovedEntities;
	private EventMessage reusableDestroyedEventMessage;
	private EventMessage reusableCreatedEventMessage;
	private Set<Byte> reusableAlreadyInterestedProperties;
	private DataObject reusablePropertiesDataObject;

	public CullingSystem(PropertyHandler[] propertyHandlers) {
		super(Aspect.all(Observer.class, Scan.class));
		this.propertyHandlers = propertyHandlers;

		reusableRemovedEntities = new IntBag();
		reusableDestroyedEventMessage = new EventMessage(MessageCodes.ENTITY_DESTROYED);
		reusableDestroyedEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_REASON, "culling");
		reusableCreatedEventMessage = new EventMessage(MessageCodes.ENTITY_CREATED);
		reusableAlreadyInterestedProperties = new UnifiedSet<>();
		reusablePropertiesDataObject = new DataObject();
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int entity) {
		Observer observer = observerMapper.get(entity);
		Scan scan = scanMapper.get(entity);

		notifyAboutRemovedEntities(entity, scan, observer);
		notifyAboutNewEntities(entity, scan, observer);
	}

	private void notifyAboutRemovedEntities(int entity, Scan scan, Observer observer) {
		reusableRemovedEntities.clear();
		int[] knowEntities = observer.knowEntities.getData();
		for (int i = 0, size = observer.knowEntities.size(); i < size; ++i) {
			int knowEntity = knowEntities[i];
			if (!scan.nearbyEntities.contains(knowEntity)) {
				notifyAboutRemovedEntity(observer.peer, knowEntity);
				reusableRemovedEntities.add(knowEntity);
				if (knownMapper.has(knowEntity)) {
					knownMapper.get(knowEntity).knownByEntities.removeValue(entity);
				}
			}
		}
		int[] entitiesToRemove = reusableRemovedEntities.getData();
		for (int i = 0, size = reusableRemovedEntities.size(); i < size; ++i) {
			int entityToRemove = entitiesToRemove[i];
			observer.knowEntities.removeValue(entityToRemove);
		}
	}

	private void notifyAboutRemovedEntity(VastPeer peer, int deletedEntity) {
		reusableDestroyedEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, deletedEntity);
		peer.send(reusableDestroyedEventMessage);
	}

	private void notifyAboutNewEntities(int entity, Scan scan, Observer observer) {
		int[] nearbyEntities = scan.nearbyEntities.getData();
		for (int i = 0, size = scan.nearbyEntities.size(); i < size; ++i) {
			int nearbyEntity = nearbyEntities[i];
			if (!invisibleMapper.has(nearbyEntity) && !observer.knowEntities.contains(nearbyEntity)) {
				notifyAboutNewEntity(observer.peer, entity, nearbyEntity);
				observer.knowEntities.add(nearbyEntity);
				if (knownMapper.has(nearbyEntity)) {
					knownMapper.get(nearbyEntity).knownByEntities.add(entity);
				}
			}
		}
	}

	private void notifyAboutNewEntity(VastPeer peer, int entity, int newEntity) {
		SyncPropagation syncPropagation = syncPropagationMapper.get(newEntity);
		reusableCreatedEventMessage.getDataObject().clear();
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_ENTITY_ID, newEntity);
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_TYPE, typeMapper.get(newEntity).type);
		if (subTypeMapper.has(newEntity)) {
			reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_SUB_TYPE, subTypeMapper.get(newEntity).subType);
		}
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_REASON, "culling");
		if (avatarMapper.has(newEntity)) {
			reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_OWNER, peer.getName().equals(avatarMapper.get(newEntity).name));
		}
		reusableAlreadyInterestedProperties.clear();
		reusablePropertiesDataObject.clear();
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_PROPERTIES, reusablePropertiesDataObject);
		for (PropertyHandler propertyHandler : propertyHandlers) {
			byte property = propertyHandler.getProperty();
			if (newEntity == entity || syncPropagation.isNearbyPropagation(propertyHandler.getProperty())) {
				if (!reusableAlreadyInterestedProperties.contains(property) && propertyHandler.isInterestedIn(newEntity)) {
					propertyHandler.decorateDataObject(newEntity, reusablePropertiesDataObject, true);
					reusableAlreadyInterestedProperties.add(property);
				}
			}
		}
		peer.send(reusableCreatedEventMessage);
	}
}
