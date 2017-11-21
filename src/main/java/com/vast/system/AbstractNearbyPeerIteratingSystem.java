package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.VastPeer;
import com.vast.component.Active;
import com.vast.component.Known;
import com.vast.component.Player;
import com.vast.component.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractNearbyPeerIteratingSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AbstractNearbyPeerIteratingSystem.class);

	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private Map<String, VastPeer> peers;
	private Set<VastPeer> reusableNearbyPeers;

	public AbstractNearbyPeerIteratingSystem(Aspect.Builder builder, Map<String, VastPeer> peers) {
		super(builder);
		this.peers = peers;

		reusableNearbyPeers = new HashSet<VastPeer>();
	}

	@Override
	protected void process(int entity) {
		reusableNearbyPeers.clear();
		if (scanMapper.has(entity)) {
			for (int nearbyEntity : scanMapper.get(entity).nearbyEntities) {
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
					if (scan.nearbyEntities.contains(entity)) {
						VastPeer nearbyPeer = peers.get(playerMapper.get(activePlayerEntity).name);
						if (nearbyPeer != null) {
							reusableNearbyPeers.add(nearbyPeer);
						}
					}
				}
			}
		}
		process(entity, reusableNearbyPeers);
	}

	protected abstract void process(int entity, Set<VastPeer> nearbyPeers);
}
