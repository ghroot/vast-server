package com.vast.system;

import com.artemis.Aspect;
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
		IntBag activePlayerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Active.class)).getEntities();
		for (SyncHandler syncHandler : syncHandlers) {
			IntBag syncEntities = world.getAspectSubscriptionManager().get(syncHandler.getAspectBuilder()).getEntities();
			for (int i = 0; i < syncEntities.size(); i++) {
				int syncEntity = syncEntities.get(i);
				if (syncHandler.needsSync(syncEntity)) {
					reusableNearbyPeers.clear();
					for (int j = 0; j < activePlayerEntities.size(); j++) {
						int activePlayerEntity = activePlayerEntities.get(j);
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
					syncHandler.sync(syncEntity, reusableNearbyPeers);
				}
			}
		}
	}
}
