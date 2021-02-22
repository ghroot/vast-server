package com.vast.system;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.utils.IntBag;
import com.vast.component.Player;
import com.vast.network.FakePeer;
import com.vast.network.VastPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PeerEntitySystem extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(PeerEntitySystem.class);

	private ComponentMapper<Player> playerMapper;
	private CreationManager creationManager;

	@All(Player.class)
	private EntitySubscription playerSubscription;

	private Map<String, VastPeer> peers;

	private Map<String, Integer> entitiesByPeer;

	public PeerEntitySystem(Map<String, VastPeer> peers, Map<String, Integer> entitiesByPeer) {
		this.peers = peers;
		this.entitiesByPeer = entitiesByPeer;
	}

	@Override
	protected void processSystem() {
		for (VastPeer peer : peers.values()) {
			if (!entitiesByPeer.containsKey(peer.getName())) {
				if (!tryConnectingToExistingPlayerEntity(peer)) {
					createNewPeerEntity(peer);
				}
			}
		}
	}

	private boolean tryConnectingToExistingPlayerEntity(VastPeer peer) {
		IntBag playerEntities = playerSubscription.getEntities();
		for (int i = 0; i < playerEntities.size(); i++) {
			int playerEntity = playerEntities.get(i);
			Player player = playerMapper.get(playerEntity);
			if (player.name.equals(peer.getName())) {
				entitiesByPeer.put(peer.getName(), playerEntity);
				logger.info("Connecting existing peer entity: {} for {}", playerEntity, peer.getName());
				return true;
			}
		}

		return false;
	}

	private void createNewPeerEntity(VastPeer peer) {
		int playerEntity = creationManager.createPlayer(peer.getName(), Math.abs(peer.getName().hashCode()) % 2, peer instanceof FakePeer);
		entitiesByPeer.put(peer.getName(), playerEntity);
		logger.info("Creating new peer entity: {} for {}", playerEntity, peer.getName());
	}
}
