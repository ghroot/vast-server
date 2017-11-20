package com.vast.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.vast.FakePeer;
import com.vast.VastPeer;
import com.vast.WorldConfiguration;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PeerEntitySystem extends ProfiledBaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(PeerEntitySystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Health> healthMapper;
	private ComponentMapper<AI> aiMapper;

	private Map<String, VastPeer> peers;
	private WorldConfiguration worldConfiguration;

	private Map<String, Integer> entitiesByPeer;
	private Set<VastPeer> peersLastUpdate;
	private Archetype playerEntityArchetype;

	public PeerEntitySystem(Map<String, VastPeer> peers, Map<String, Integer> entitiesByPeer, WorldConfiguration worldConfiguration) {
		this.peers = peers;
		this.entitiesByPeer = entitiesByPeer;
		this.worldConfiguration = worldConfiguration;

		peersLastUpdate = new HashSet<VastPeer>();
	}

	@Override
	protected void initialize() {
		super.initialize();

		playerEntityArchetype = new ArchetypeBuilder()
				.add(Player.class)
				.add(Type.class)
				.add(Inventory.class)
				.add(Health.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Scan.class)
				.add(Known.class)
				.add(Interactable.class)
				.add(Attack.class)
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
		int playerEntity = world.create(playerEntityArchetype);
		playerMapper.get(playerEntity).name = peer.getName();
		typeMapper.get(playerEntity).type = "player";
		transformMapper.get(playerEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
		collisionMapper.get(playerEntity).radius = 0.3f;
		healthMapper.get(playerEntity).maxHealth = 3;
		healthMapper.get(playerEntity).health = 3;
		if (peer instanceof FakePeer) {
			aiMapper.create(playerEntity);
		}
		entitiesByPeer.put(peer.getName(), playerEntity);
		logger.info("Creating peer entity: {} for {} at {}", playerEntity, peer.getName(), transformMapper.get(playerEntity).position);
	}
}
