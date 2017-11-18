package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.vast.VastPeer;
import com.vast.component.Active;
import com.vast.component.Player;
import com.vast.component.Scan;
import com.vast.sync.SyncHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SyncSystem extends ProfiledBaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(SyncSystem.class);

	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private Set<SyncHandler> syncHandlers;
	private Map<String, VastPeer> peers;
	private Set<VastPeer> reusableNearbyPeers;

	public SyncSystem(Set<SyncHandler> syncHandlers, Map<String, VastPeer> peers) {
		this.syncHandlers = syncHandlers;
		this.peers = peers;

		reusableNearbyPeers = new HashSet<VastPeer>();
	}

	@Override
	protected void initialize() {
		super.initialize();

		for (SyncHandler syncHandler : syncHandlers) {
			world.inject(syncHandler);
			world.getAspectSubscriptionManager().get(syncHandler.getAspectBuilder()).addSubscriptionListener(new EntitySubscription.SubscriptionListener() {
				@Override
				public void inserted(IntBag entities) {
					for (int i = 0; i < entities.size(); i++) {
						syncHandler.inserted(entities.get(i));
					}
				}

				@Override
				public void removed(IntBag entities) {
					for (int i = 0; i < entities.size(); i++) {
						syncHandler.removed(entities.get(i));
					}
				}
			});
		}
	}

	@Override
	protected void processSystem() {
		for (SyncHandler syncHandler : syncHandlers) {
			IntBag syncEntities = world.getAspectSubscriptionManager().get(syncHandler.getAspectBuilder().all(Scan.class)).getEntities();
			for (int i = 0; i < syncEntities.size(); i++) {
				int syncEntity = syncEntities.get(i);
				reusableNearbyPeers.clear();
				if (scanMapper.has(syncEntity)) {
					Scan scan = scanMapper.get(syncEntity);
					for (int nearbyEntity : scan.nearbyEntities) {
						if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity)) {
							VastPeer nearbyPeer = peers.get(playerMapper.get(nearbyEntity).name);
							if (nearbyPeer != null) {
								reusableNearbyPeers.add(nearbyPeer);
							}
						}
					}
					syncHandler.sync(syncEntity, reusableNearbyPeers);
				}
			}
		}
	}
}
