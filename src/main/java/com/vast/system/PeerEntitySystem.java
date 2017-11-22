package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.vast.FakePeer;
import com.vast.VastPeer;
import com.vast.component.Player;
import com.vast.component.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PeerEntitySystem extends AbstractProfiledBaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(PeerEntitySystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;

	private Map<String, VastPeer> peers;

	private Map<String, Integer> entitiesByPeer;
	private Set<VastPeer> peersLastUpdate;
	private CreationManager creationManager;

	public PeerEntitySystem(Map<String, VastPeer> peers, Map<String, Integer> entitiesByPeer) {
		this.peers = peers;
		this.entitiesByPeer = entitiesByPeer;

		peersLastUpdate = new HashSet<VastPeer>();
	}

	@Override
	protected void initialize() {
		super.initialize();

		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	protected void processSystem() {
		updateEntitiesByPeer();

		for (VastPeer peer : peers.values()) {
			if (isPeerNew(peer)) {
				peerJoined(peer);
			}
		}

		peersLastUpdate.clear();
		peersLastUpdate.addAll(peers.values());
	}

	private void updateEntitiesByPeer() {
		entitiesByPeer.clear();
		IntBag playerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class)).getEntities();
		for (int i = 0; i < playerEntities.size(); i++) {
			int playerEntity = playerEntities.get(i);
			if (playerMapper.has(playerEntity)) {
				String name = playerMapper.get(playerEntity).name;
				if (!entitiesByPeer.containsKey(name)) {
					entitiesByPeer.put(name, playerEntity);
				}
			}
		}
	}

	private boolean isPeerNew(VastPeer peer) {
		return !peersLastUpdate.contains(peer);
	}

	private void peerJoined(VastPeer peer) {
		if (!entitiesByPeer.containsKey(peer.getName())) {
			createPeerEntity(peer);
		}
	}

	private void createPeerEntity(VastPeer peer) {
		int playerEntity = creationManager.createPlayer(peer.getName(), peer instanceof FakePeer);
		entitiesByPeer.put(peer.getName(), playerEntity);
		logger.info("Creating peer entity: {} for {} at {}", playerEntity, peer.getName(), transformMapper.get(playerEntity).position);
	}
}
