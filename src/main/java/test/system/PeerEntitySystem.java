package test.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.nhnent.haste.framework.ClientPeer;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.MessageCodes;
import test.MyPeer;
import test.component.*;

import javax.vecmath.Point2i;
import java.util.*;

public class PeerEntitySystem extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(PeerEntitySystem.class);

	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<SyncTransformComponent> syncTransformComponentMapper;
	private ComponentMapper<ActiveComponent> activeComponentMapper;
	private ComponentMapper<TypeComponent> typeComponentMapper;
	private ComponentMapper<SpatialComponent> spatialComponentMapper;

	private List<MyPeer> peers;
	private Map<String, Integer> entitiesByPeerName;
	private Map<Point2i, Set<Integer>> spatialHashes;

	private List<MyPeer> peersLastUpdate;
	private Map<String, Set<Integer>> knownEntities;
	private Set<Integer> reusableNearbyEntities;
	private Point2i reusableHash;
	private List<Integer> reusableRemovedEntities;
	private float[] reusablePosition;
	private Archetype peerEntityArchetype;

	public PeerEntitySystem(List<MyPeer> peers, Map<String, Integer> entitiesByPeerName, Map<Point2i, Set<Integer>> spatialHashes) {
		this.peers = peers;
		this.entitiesByPeerName = entitiesByPeerName;
		this.spatialHashes = spatialHashes;

		peersLastUpdate = new ArrayList<MyPeer>();
		knownEntities = new HashMap<String, Set<Integer>>();
		reusableNearbyEntities = new HashSet<Integer>();
		reusableHash = new Point2i();
		reusableRemovedEntities = new ArrayList<Integer>();
		reusablePosition = new float[2];
	}

	@Override
	protected void initialize() {
		peerEntityArchetype = new ArchetypeBuilder()
				.add(PeerComponent.class)
				.add(TransformComponent.class)
				.add(SpatialComponent.class)
				.add(CollisionComponent.class)
				.add(SyncTransformComponent.class)
				.build(world);
	}

	@Override
	protected void processSystem() {
		setupNewPeers();
		createEntitiesForNewPeers();
		checkAndNotifyAboutNewEntities();
		checkAndNotifyAboutActivatedPeerEntities();
		checkAndNotifyAboutDeactivatedPeerEntities();
		checkAndNotifyAboutRemovedEntities();

		peersLastUpdate.clear();
		peersLastUpdate.addAll(peers);
	}

	private void setupNewPeers() {
		for (MyPeer peer : peers) {
			if (!peersLastUpdate.contains(peer)) {
				knownEntities.put(peer.getName(), new HashSet<Integer>());
			}
		}
	}

	private void createEntitiesForNewPeers() {
		for (MyPeer peer : peers) {
			if (!peersLastUpdate.contains(peer)) {
				if (!entitiesByPeerName.containsKey(peer.getName())) {
					int entity = world.create(peerEntityArchetype);
					peerComponentMapper.get(entity).name = peer.getName();
					transformComponentMapper.get(entity).position.set(-1.0f + (float) Math.random() * 2.0f, -1.0f + (float) Math.random() * 2.0f);
					logger.info("Creating peer entity: {} for {} at {}", entity, peer.getName(), transformComponentMapper.get(entity).position);
					entitiesByPeerName.put(peer.getName(), entity);
				}
			}
		}
	}

	private void checkAndNotifyAboutNewEntities() {
		for (MyPeer peer : peers) {
			int peerEntity = entitiesByPeerName.get(peer.getName());
			Set<Integer> nearbyEntities = getNearbyEntities(peerEntity);
			for (int nearbyEntity : nearbyEntities) {
				if (!isEntityKnownByPeer(nearbyEntity, peer)) {
					logger.info("Notifying peer {} about new entity {}", peer.getName(), nearbyEntity);
					TransformComponent transformComponent = transformComponentMapper.get(nearbyEntity);
					reusablePosition[0] = transformComponent.position.x;
					reusablePosition[1] = transformComponent.position.y;
					if (peerComponentMapper.has(nearbyEntity)) {
						PeerComponent peerComponent = peerComponentMapper.get(nearbyEntity);
						boolean owner = peer.getName().equals(peerComponent.name);
						boolean active = activeComponentMapper.has(nearbyEntity);
						peer.send(new EventMessage(MessageCodes.PEER_ENTITY_CREATED, new DataObject()
										.set(MessageCodes.PEER_ENTITY_CREATED_ENTITY_ID, nearbyEntity)
										.set(MessageCodes.PEER_ENTITY_CREATED_OWNER, owner)
										.set(MessageCodes.PEER_ENTITY_CREATED_ACTIVE, active)
										.set(MessageCodes.PEER_ENTITY_CREATED_POSITION, reusablePosition)),
								SendOptions.ReliableSend);
					} else {
						TypeComponent typeComponent = typeComponentMapper.get(nearbyEntity);
						peer.send(new EventMessage(MessageCodes.ENTITY_CREATED, new DataObject()
										.set(MessageCodes.ENTITY_CREATED_ENTITY_ID, nearbyEntity)
										.set(MessageCodes.ENTITY_CREATED_TYPE, typeComponent.type)
										.set(MessageCodes.ENTITY_CREATED_POSITION, reusablePosition)),
								SendOptions.ReliableSend);
					}
					markEntityAsKnownByPeer(nearbyEntity, peer);
				}
			}
		}
	}

	private void checkAndNotifyAboutRemovedEntities() {
		for (MyPeer peer : peers) {
			int peerEntity = entitiesByPeerName.get(peer.getName());
			Set<Integer> nearbyEntities = getNearbyEntities(peerEntity);
			if (knownEntities.containsKey(peer.getName())) {
				Set<Integer> entitiesKnownByPeer = knownEntities.get(peer.getName());
				reusableRemovedEntities.clear();
				for (int entityKnownByPeer : entitiesKnownByPeer) {
					if (!nearbyEntities.contains(entityKnownByPeer)) {
						logger.info("Notifying peer {} about removed entity {}", peer.getName(), entityKnownByPeer);
						peer.send(new EventMessage(MessageCodes.ENTITY_DESTROYED, new DataObject()
										.set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, entityKnownByPeer)),
								SendOptions.ReliableSend);
						reusableRemovedEntities.add(entityKnownByPeer);
					}
				}
				removeEntitiesAsKnownByPeer(reusableRemovedEntities, peer);
			}
		}
	}

	private void checkAndNotifyAboutActivatedPeerEntities() {
		for (MyPeer peer : peers) {
			if (!peersLastUpdate.contains(peer)) {
				String name = peer.getName();
				int peerEntity = entitiesByPeerName.get(name);
				if (!activeComponentMapper.has(peerEntity)) {
					logger.info("Activating peer entity: {} for {}", peerEntity, name);
					activeComponentMapper.create(peerEntity);
					for (ClientPeer peerToSendTo : peers) {
						peerToSendTo.send(new EventMessage(MessageCodes.PEER_ENTITY_ACTIVATED, new DataObject()
										.set(MessageCodes.PEER_ENTITY_ACTIVATED_ENTITY_ID, peerEntity)),
								SendOptions.ReliableSend);
					}
				}
			}
		}
	}

	private void checkAndNotifyAboutDeactivatedPeerEntities() {
		for (MyPeer peer : peersLastUpdate) {
			if (!peers.contains(peer)) {
				String name = peer.getName();
				int peerEntity = entitiesByPeerName.get(name);
				if (activeComponentMapper.has(peerEntity)) {
					logger.info("Deactivating peer entity: {} for {}", peerEntity, name);
					activeComponentMapper.remove(peerEntity);
					for (ClientPeer peerToSendTo : peers) {
						peerToSendTo.send(new EventMessage(MessageCodes.PEER_ENTITY_DEACTIVATED, new DataObject()
										.set(MessageCodes.PEER_ENTITY_DEACTIVATED_ENTITY_ID, peerEntity)),
								SendOptions.ReliableSend);
					}
				}
			}
		}
	}

	private void markEntityAsKnownByPeer(int entity, MyPeer peer) {
		Set<Integer> entities;
		if (knownEntities.containsKey(peer.getName())) {
			entities = knownEntities.get(peer.getName());
		} else {
			entities = new HashSet<Integer>();
			knownEntities.put(peer.getName(), entities);
		}
		if (!entities.contains(entity)) {
			entities.add(entity);
		}
	}

	private void removeEntitiesAsKnownByPeer(List<Integer> entities, MyPeer peer) {
		knownEntities.get(peer.getName()).removeAll(entities);
	}

	private boolean isEntityKnownByPeer(int entity, MyPeer peer) {
		if (knownEntities.containsKey(peer.getName())) {
			Set<Integer> entities = knownEntities.get(peer.getName());
			if (entities.contains(entity)) {
				return true;
			}
		}
		return false;
	}

	private Set<Integer> getNearbyEntities(int entity) {
		reusableNearbyEntities.clear();
		SpatialComponent spatialComponent = spatialComponentMapper.get(entity);
		if (spatialComponent.memberOfSpatialHash != null) {
			for (int x = spatialComponent.memberOfSpatialHash.x - 10; x < spatialComponent.memberOfSpatialHash.x + 10; x++) {
				for (int y = spatialComponent.memberOfSpatialHash.y - 10; y < spatialComponent.memberOfSpatialHash.y + 10; y++) {
					reusableHash.set(x, y);
					if (spatialHashes.containsKey(reusableHash)) {
						reusableNearbyEntities.addAll(spatialHashes.get(reusableHash));
					}
				}
			}
		}
		return reusableNearbyEntities;
	}
}
