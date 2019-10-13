package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.Metrics;
import com.vast.component.*;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class SyncSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SyncSystem.class);

	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Known> knownMapper;

	private Set<PropertyHandler> propertyHandlers;
	private Metrics metrics;
	private EventMessage reusableMessage;

	public SyncSystem(Set<PropertyHandler> propertyHandlers, Metrics metrics) {
		super(Aspect.all(Sync.class, SyncPropagation.class, Known.class));
		this.propertyHandlers = propertyHandlers;
		this.metrics = metrics;

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

		syncNearbyPropagationProperties(syncEntity, sync);

		if (playerMapper.has(syncEntity) && activeMapper.has(syncEntity)) {
			syncOwnerPropagationProperties(syncEntity, sync);
		}

		syncMapper.remove(syncEntity);
	}

	private void syncNearbyPropagationProperties(int syncEntity, Sync sync) {
		ChangedProperties changedProperties = getChangedProperties(syncEntity, sync, true);
		if (changedProperties != null) {
			reusableMessage.getDataObject().clear();
			reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, syncEntity);
			reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_PROPERTIES, changedProperties.dataObject);
			for (int knownByEntity : knownMapper.get(syncEntity).knownByEntities) {
				if (playerMapper.has(knownByEntity) && activeMapper.has(knownByEntity)) {
					VastPeer knownByPeer = activeMapper.get(knownByEntity).peer;
					if (changedProperties.reliable) {
						knownByPeer.send(reusableMessage);
					} else {
						knownByPeer.sendUnreliable(reusableMessage);
					}
				}
			}
		}
	}

	private void syncOwnerPropagationProperties(int syncEntity, Sync sync) {
		ChangedProperties changedProperties = getChangedProperties(syncEntity, sync, false);
		if (changedProperties != null) {
			reusableMessage.getDataObject().clear();
			reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, syncEntity);
			reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_PROPERTIES, changedProperties.dataObject);
			VastPeer ownerPeer = activeMapper.get(syncEntity).peer;
			if (changedProperties.reliable) {
				ownerPeer.send(reusableMessage);
			} else {
				ownerPeer.sendUnreliable(reusableMessage);
			}
		}
	}

	private ChangedProperties getChangedProperties(int syncEntity, Sync sync, boolean nearbyPropagation) {
		boolean reliable = false;
		boolean atLeastOnePropertySet = false;
		DataObject propertiesDataObject = new DataObject();

		for (PropertyHandler syncHandler : propertyHandlers) {
			byte property = syncHandler.getProperty();
			if (sync.isPropertyDirty(property)) {
				SyncPropagation syncPropagation = syncPropagationMapper.get(syncEntity);
				if ((nearbyPropagation && syncPropagation.isNearbyPropagation(property)) ||
					(!nearbyPropagation && syncPropagation.isOwnerPropagation(property))) {
					if (syncHandler.decorateDataObject(syncEntity, propertiesDataObject, false)) {
						atLeastOnePropertySet = true;
						if (metrics != null) {
							metrics.incrementSyncedProperty(property);
						}
					}
				}
				if (!reliable && syncPropagation.isReliable(property)) {
					reliable = true;
				}
			}
		}

		if (atLeastOnePropertySet) {
			return new ChangedProperties(propertiesDataObject, reliable);
		} else {
			return null;
		}
	}

	class ChangedProperties {
		private DataObject dataObject;
		private boolean reliable;

		public ChangedProperties(DataObject dataObject, boolean reliable) {
			this.dataObject = dataObject;
			this.reliable = reliable;
		}
	}
}
