package com.vast.system;

import com.artemis.*;
import com.artemis.annotations.Profile;
import com.artemis.utils.IntBag;
import com.vast.FakePeer;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.WorldDimensions;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class PeerEntitySystem extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(PeerEntitySystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<AI> aiMapper;

	private Map<String, VastPeer> peers;
	private WorldDimensions worldDimensions;

	private Set<VastPeer> peersLastUpdate;
	private Map<String, Integer> entitiesByPeer;
	private Archetype playerEntityArchetype;

	public PeerEntitySystem(Map<String, VastPeer> peers, WorldDimensions worldDimensions) {
		this.peers = peers;
		this.worldDimensions = worldDimensions;

		peersLastUpdate = new HashSet<VastPeer>();
		entitiesByPeer = new HashMap<String, Integer>();
	}

	@Override
	protected void initialize() {
		playerEntityArchetype = new ArchetypeBuilder()
				.add(Player.class)
				.add(Inventory.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(SyncTransform.class)
				.build(world);
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
			String name = playerMapper.get(playerEntity).name;
			if (!entitiesByPeer.containsKey(name)) {
				entitiesByPeer.put(name, playerEntity);
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
		int playerEntity = world.create(playerEntityArchetype);
		playerMapper.get(playerEntity).name = peer.getName();
		transformMapper.get(playerEntity).position.set(-worldDimensions.width / 2 + (float) Math.random() * worldDimensions.width, -worldDimensions.height / 2 + (float) Math.random() * worldDimensions.height);
		if (peer instanceof FakePeer) {
			aiMapper.create(playerEntity);
		}
		entitiesByPeer.put(peer.getName(), playerEntity);
		logger.info("Creating peer entity: {} for {} at {}", playerEntity, peer.getName(), transformMapper.get(playerEntity).position);
	}
}
