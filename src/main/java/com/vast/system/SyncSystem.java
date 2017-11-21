package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Metrics;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.Active;
import com.vast.component.Player;
import com.vast.component.Sync;
import com.vast.property.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class SyncSystem extends AbstractNearbyEntityIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SyncSystem.class);

	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private Set<PropertyHandler> propertyHandlers;
	private Map<String, VastPeer> peers;
	private Metrics metrics;
	private EventMessage reusableEventMessage;

	public SyncSystem(Set<PropertyHandler> propertyHandlers, Map<String, VastPeer> peers, Metrics metrics) {
		super(Aspect.all(Sync.class));
		this.propertyHandlers = propertyHandlers;
		this.peers = peers;
		this.metrics = metrics;

		reusableEventMessage = new EventMessage(MessageCodes.UPDATE_PROPERTIES);
	}

	@Override
	protected void initialize() {
		super.initialize();

		for (PropertyHandler propertyHandler : propertyHandlers) {
			world.inject(propertyHandler);
		}
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int syncEntity, Set<Integer> nearbyEntities) {
		Sync sync = syncMapper.get(syncEntity);

		reusableEventMessage.getDataObject().clear();
		reusableEventMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, syncEntity);
		for (PropertyHandler syncHandler : propertyHandlers) {
			if (sync.isPropertyDirty(syncHandler.getProperty())) {
				syncHandler.decorateDataObject(syncEntity, reusableEventMessage.getDataObject());
				metrics.incrementSyncedProperty(syncHandler.getProperty());
			}
		}

		for (int nearbyEntity : nearbyEntities) {
			if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity)) {
				VastPeer nearbyPeer = peers.get(playerMapper.get(nearbyEntity).name);
				nearbyPeer.send(reusableEventMessage);
			}
		}

		syncMapper.remove(syncEntity);
	}
}
