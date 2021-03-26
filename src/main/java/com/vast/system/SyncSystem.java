package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.component.*;
import com.vast.data.Metrics;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class SyncSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SyncSystem.class);

	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;
	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Observed> observedMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Owner> ownerMapper;
	private ComponentMapper<Avatar> avatarMapper;

	private Map<String, VastPeer> peers;
	private PropertyHandler[] propertyHandlers;
	private Metrics metrics;

	private Set<Byte> reusableAlreadyInterestedProperties;
	private DataObject reusablePropertiesDataObject;
	private ChangedProperties reusableChangedProperties;
	private EventMessage reusableMessage;

	public SyncSystem(Map<String, VastPeer> peers, PropertyHandler[] propertyHandlers, Metrics metrics) {
		super(Aspect.all(Sync.class, SyncPropagation.class, Known.class));
		this.peers = peers;
		this.propertyHandlers = propertyHandlers;
		this.metrics = metrics;

		reusableAlreadyInterestedProperties = new UnifiedSet<>();
		reusablePropertiesDataObject = new DataObject();
		reusableChangedProperties = new ChangedProperties();
		reusableMessage = new EventMessage(MessageCodes.UPDATE_PROPERTIES);
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int syncEntity) {
		Sync sync = syncMapper.get(syncEntity);
		SyncPropagation syncPropagation = syncPropagationMapper.get(syncEntity);
		Known known = knownMapper.get(syncEntity);

		for (int i = 0; i < known.knownByEntities.size(); i++) {
			int knownByEntity = known.knownByEntities.get(i);
			Observer knownByObserver = observerMapper.get(knownByEntity);
			VastPeer knownByPeer = knownByObserver.peer;

			boolean reliable = false;
			boolean atLeastOnePropertySet = false;

			reusableMessage.getDataObject().clear();
			reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, syncEntity);
			reusableAlreadyInterestedProperties.clear();
			reusablePropertiesDataObject.clear();
			boolean isOwner = isOwner(knownByEntity, syncEntity);
			for (PropertyHandler propertyHandler : propertyHandlers) {
				byte property = propertyHandler.getProperty();
				if (sync.isPropertyDirty(property)) {
					if (propertyHandler.isInterestedIn(syncEntity) && !reusableAlreadyInterestedProperties.contains(property)) {
						if (!syncPropagation.isBlocked(property)) {
							if (syncPropagation.isNearbyPropagation(property) || isOwner) {
								if (propertyHandler.decorateDataObject(knownByEntity, syncEntity, reusablePropertiesDataObject, false)) {
									if (!reliable && syncPropagation.isReliable(property)) {
										reliable = true;
									}

									atLeastOnePropertySet = true;

									if (metrics != null) {
										metrics.incrementSyncedProperty(property);
									}
								}
							}
						}
						reusableAlreadyInterestedProperties.add(property);
					}
				}
			}
			reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_PROPERTIES, reusablePropertiesDataObject);

			if (atLeastOnePropertySet) {
				if (reliable) {
					knownByPeer.send(reusableMessage);
				} else {
					knownByPeer.sendUnreliable(reusableMessage);
				}
			}
		}

		syncMapper.remove(syncEntity);
	}

	private boolean isOwner(int potentialOwnerEntity, int entity) {
		if (entity == potentialOwnerEntity) {
			return true;
		}

		if (observerMapper.has(potentialOwnerEntity)) {
			if (ownerMapper.has(entity) && observerMapper.get(potentialOwnerEntity).peer.getName().equals(ownerMapper.get(entity).name)) {
				return true;
			}

			if (avatarMapper.has(entity) && observerMapper.get(potentialOwnerEntity).peer.getName().equals(avatarMapper.get(entity).name)) {
				return true;
			}

			if (observedMapper.has(entity) && observedMapper.get(entity).observerEntity == potentialOwnerEntity) {
				return true;
			}
		}

		return false;
	}

	private class ChangedProperties {
		private DataObject propertiesDataObject;
		private boolean reliable;
	}
}
