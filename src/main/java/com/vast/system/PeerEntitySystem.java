package com.vast.system;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
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
				createPeerEntity(peer);
			}
		}
	}

	private void createPeerEntity(VastPeer peer) {
		int playerEntity = creationManager.createPlayer(peer.getName(), Math.abs(peer.getName().hashCode()) % 2, peer instanceof FakePeer);
		entitiesByPeer.put(peer.getName(), playerEntity);
		logger.info("Creating peer entity: {} for {}", playerEntity, peer.getName());
	}
}
