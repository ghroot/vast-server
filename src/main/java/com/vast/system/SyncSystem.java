package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.Active;
import com.vast.component.Player;
import com.vast.component.Scan;
import com.vast.component.Sync;
import com.vast.property.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class SyncSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SyncSystem.class);

	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Sync> syncMapper;

	private Set<PropertyHandler> propertyHandlers;
	private Map<String, VastPeer> peers;
	private Set<VastPeer> reusableNearbyPeers;
	private EventMessage reusableEventMessage;

	public SyncSystem(Set<PropertyHandler> propertyHandlers, Map<String, VastPeer> peers) {
		super(Aspect.all(Sync.class));
		this.propertyHandlers = propertyHandlers;
		this.peers = peers;

		reusableNearbyPeers = new HashSet<VastPeer>();
		reusableEventMessage = new EventMessage(MessageCodes.UPDATE_PROPERTIES);
	}

	@Override
	protected void initialize() {
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
	protected void process(int syncEntity) {
		Sync sync = syncMapper.get(syncEntity);

		reusableEventMessage.getDataObject().clear();
		reusableEventMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, syncEntity);
		for (PropertyHandler syncHandler : propertyHandlers) {
			if (sync.isPropertyDirty(syncHandler.getProperty())) {
				syncHandler.decorateDataObject(syncEntity, reusableEventMessage.getDataObject());
			}
		}

		reusableNearbyPeers.clear();
		if (scanMapper.has(syncEntity)) {
			for (int nearbyEntity : scanMapper.get(syncEntity).nearbyEntities) {
				if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity)) {
					VastPeer nearbyPeer = peers.get(playerMapper.get(nearbyEntity).name);
					if (nearbyPeer != null) {
						reusableNearbyPeers.add(nearbyPeer);
					}
				}
			}
		} else {
			IntBag activePlayerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Active.class)).getEntities();
			for (int i = 0; i < activePlayerEntities.size(); i++) {
				int activePlayerEntity = activePlayerEntities.get(i);
				if (playerMapper.has(activePlayerEntity) && activeMapper.has(activePlayerEntity)) {
					Scan scan = scanMapper.get(activePlayerEntity);
					if (scan.nearbyEntities.contains(syncEntity)) {
						VastPeer nearbyPeer = peers.get(playerMapper.get(activePlayerEntity).name);
						if (nearbyPeer != null) {
							reusableNearbyPeers.add(nearbyPeer);
						}
					}
				}
			}
		}

		for (VastPeer nearbyPeer : reusableNearbyPeers) {
			nearbyPeer.send(reusableEventMessage, SendOptions.ReliableSend);
		}

		syncMapper.remove(syncEntity);
	}
}
