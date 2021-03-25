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
		super(Aspect.all(Sync.class, SyncPropagation.class));
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

		syncNearbyPropagationProperties(syncEntity, sync);

		if (avatarMapper.has(syncEntity) || ownerMapper.has(syncEntity)) {
			syncOwnerPropagationProperties(syncEntity, sync);
		}

		syncMapper.remove(syncEntity);
	}

	private void syncNearbyPropagationProperties(int syncEntity, Sync sync) {
		Known known = knownMapper.get(syncEntity);
		if (known != null) {
			ChangedProperties changedProperties = getChangedProperties(syncEntity, sync, true);
			if (changedProperties != null) {
				reusableMessage.getDataObject().clear();
				reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, syncEntity);
				reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_PROPERTIES, changedProperties.propertiesDataObject);
				IntBag knownByEntitiesBag = known.knownByEntities;
				int[] knownByEntities = knownByEntitiesBag.getData();
				for (int i = 0, size = knownByEntitiesBag.size(); i < size; ++i) {
					int knownByEntity = knownByEntities[i];
					if (observerMapper.has(knownByEntity)) {
						VastPeer knownByPeer = observerMapper.get(knownByEntity).peer;
						if (changedProperties.reliable) {
							knownByPeer.send(reusableMessage);
						} else {
							knownByPeer.sendUnreliable(reusableMessage);
						}
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
			reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_PROPERTIES, changedProperties.propertiesDataObject);
			VastPeer ownerPeer = null;
			if (observerMapper.has(syncEntity)) {
				ownerPeer = observerMapper.get(syncEntity).peer;
			} else if (avatarMapper.has(syncEntity)) {
				ownerPeer = peers.get(avatarMapper.get(syncEntity).name);
			} else if (ownerMapper.has(syncEntity)) {
				ownerPeer = peers.get(ownerMapper.get(syncEntity).name);
			}
			if (ownerPeer != null) {
				if (changedProperties.reliable) {
					ownerPeer.send(reusableMessage);
				} else {
					ownerPeer.sendUnreliable(reusableMessage);
				}
			}
		}
	}

	private ChangedProperties getChangedProperties(int syncEntity, Sync sync, boolean nearbyPropagation) {
		boolean reliable = false;
		boolean atLeastOnePropertySet = false;
		reusableAlreadyInterestedProperties.clear();
		reusablePropertiesDataObject.clear();

		for (PropertyHandler propertyHandler : propertyHandlers) {
			byte property = propertyHandler.getProperty();
			if (sync.isPropertyDirty(property)) {
				SyncPropagation syncPropagation = syncPropagationMapper.get(syncEntity);
				if (!syncPropagation.isBlocked(property)) {
					if ((nearbyPropagation && syncPropagation.isNearbyPropagation(property)) ||
							(!nearbyPropagation && syncPropagation.isOwnerPropagation(property))) {
						if (!reusableAlreadyInterestedProperties.contains(property) && propertyHandler.isInterestedIn(syncEntity)) {
							if (propertyHandler.decorateDataObject(syncEntity, reusablePropertiesDataObject, false)) {
								if (!reliable && syncPropagation.isReliable(property)) {
									reliable = true;
								}

								atLeastOnePropertySet = true;

								if (metrics != null) {
									metrics.incrementSyncedProperty(property);
								}
							}
							reusableAlreadyInterestedProperties.add(property);
						}
					}
				}
			}
		}

		if (atLeastOnePropertySet) {
			reusableChangedProperties.propertiesDataObject = reusablePropertiesDataObject;
			reusableChangedProperties.reliable = reliable;
			return reusableChangedProperties;
		} else {
			return null;
		}
	}

	private class ChangedProperties {
		private DataObject propertiesDataObject;
		private boolean reliable;
	}
}
