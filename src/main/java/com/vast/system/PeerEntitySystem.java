package com.vast.system;

import com.artemis.*;
import com.artemis.annotations.Profile;
import com.artemis.utils.IntBag;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.VastPeer;
import com.vast.Profiler;
import com.vast.WorldDimensions;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2i;
import java.util.*;

@Profile(enabled = true, using = Profiler.class)
public class PeerEntitySystem extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(PeerEntitySystem.class);

	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<SyncTransformComponent> syncTransformComponentMapper;
	private ComponentMapper<ActiveComponent> activeComponentMapper;
	private ComponentMapper<TypeComponent> typeComponentMapper;
	private ComponentMapper<SpatialComponent> spatialComponentMapper;

	private Map<String, VastPeer> peers;
	private WorldDimensions worldDimensions;
	private Map<Point2i, Set<Integer>> spatialHashes;

	private Set<VastPeer> peersLastUpdate;
	private Map<String, Integer> entitiesByPeer;
	private Map<String, Set<Integer>> knownEntitiesByPeer;
	private Map<String, Set<Integer>> nearbyEntitiesByPeer;
	private Point2i reusableHash;
	private List<Integer> reusableRemovedEntities;
	private float[] reusablePosition;
	private Archetype peerEntityArchetype;

	public PeerEntitySystem(Map<String, VastPeer> peers, WorldDimensions worldDimensions, Map<Point2i, Set<Integer>> spatialHashes) {
		this.peers = peers;
		this.worldDimensions = worldDimensions;
		this.spatialHashes = spatialHashes;

		peersLastUpdate = new HashSet<VastPeer>();
		entitiesByPeer = new HashMap<String, Integer>();
		knownEntitiesByPeer = new HashMap<String, Set<Integer>>();
		nearbyEntitiesByPeer = new HashMap<String, Set<Integer>>();
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
		updateEntitiesByPeer();

		for (VastPeer peer : peers.values()) {
			if (isPeerNew(peer)) {
				peerJoined(peer);
			} else {
				updateNearbyEntities(peer);
				checkAndNotifyAboutNewEntities(peer);
				checkAndNotifyAboutActivatedPeerEntity(peer);
				checkAndNotifyAboutRemovedEntities(peer);
			}
		}
		for (VastPeer peer : peersLastUpdate) {
			if (hasPeerLeft(peer)) {
				peerLeft(peer);
			}
		}

		peersLastUpdate.clear();
		peersLastUpdate.addAll(peers.values());
	}

	private void updateEntitiesByPeer() {
		entitiesByPeer.clear();
		IntBag peerEntities = world.getAspectSubscriptionManager().get(Aspect.all(PeerComponent.class)).getEntities();
		for (int i = 0; i < peerEntities.size(); i++) {
			int peerEntity = peerEntities.get(i);
			String name = peerComponentMapper.get(peerEntity).name;
			if (!entitiesByPeer.containsKey(name)) {
				entitiesByPeer.put(name, peerEntity);
			}
		}
	}

	private boolean isPeerNew(VastPeer peer) {
		return !peersLastUpdate.contains(peer);
	}

	private boolean hasPeerLeft(VastPeer peer) {
		return !peers.containsValue(peer);
	}

	private void peerJoined(VastPeer peer) {
		knownEntitiesByPeer.put(peer.getName(), new HashSet<Integer>());
		nearbyEntitiesByPeer.put(peer.getName(), new HashSet<Integer>());

		if (!entitiesByPeer.containsKey(peer.getName())) {
			createPeerEntity(peer);
		}
	}

	private void peerLeft(VastPeer peer) {
		checkAndNotifyAboutDeactivatedPeerEntity(peer);
	}

	private void createPeerEntity(VastPeer peer) {
		int peerEntity = world.create(peerEntityArchetype);
		peerComponentMapper.get(peerEntity).name = peer.getName();
		transformComponentMapper.get(peerEntity).position.set(-worldDimensions.width / 2 + (float) Math.random() * worldDimensions.width, -worldDimensions.height / 2 + (float) Math.random() * worldDimensions.height);
		entitiesByPeer.put(peer.getName(), peerEntity);
		logger.info("Creating peer entity: {} for {} at {}", peerEntity, peer.getName(), transformComponentMapper.get(peerEntity).position);
	}

	private void updateNearbyEntities(VastPeer peer) {
		Set<Integer> nearbyEntities = nearbyEntitiesByPeer.get(peer.getName());
		nearbyEntities.clear();
		int peerEntity = entitiesByPeer.get(peer.getName());
		SpatialComponent spatialComponent = spatialComponentMapper.get(peerEntity);
		if (spatialComponent.memberOfSpatialHash != null) {
			for (int x = spatialComponent.memberOfSpatialHash.x - worldDimensions.sectionSize; x <= spatialComponent.memberOfSpatialHash.x + worldDimensions.sectionSize; x += worldDimensions.sectionSize) {
				for (int y = spatialComponent.memberOfSpatialHash.y - worldDimensions.sectionSize; y <= spatialComponent.memberOfSpatialHash.y + worldDimensions.sectionSize; y += worldDimensions.sectionSize) {
					reusableHash.set(x, y);
					if (spatialHashes.containsKey(reusableHash)) {
						nearbyEntities.addAll(spatialHashes.get(reusableHash));
					}
				}
			}
		}
	}

	private void checkAndNotifyAboutNewEntities(VastPeer peer) {
		Set<Integer> nearbyEntities = nearbyEntitiesByPeer.get(peer.getName());
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

	private void checkAndNotifyAboutRemovedEntities(VastPeer peer) {
		Set<Integer> nearbyEntities = nearbyEntitiesByPeer.get(peer.getName());
		Set<Integer> entitiesKnownByPeer = knownEntitiesByPeer.get(peer.getName());
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

	private void checkAndNotifyAboutActivatedPeerEntity(VastPeer peer) {
		int peerEntity = entitiesByPeer.get(peer.getName());
		if (!activeComponentMapper.has(peerEntity)) {
			String name = peerComponentMapper.get(peerEntity).name;
			if (peers.containsKey(name)) {
				logger.info("Activating peer entity: {} for {}", peerEntity, name);
				activeComponentMapper.create(peerEntity);
				for (VastPeer peerToSendTo : peers.values()) {
					if (isEntityKnownByPeer(peerEntity, peerToSendTo)) {
						peerToSendTo.send(new EventMessage(MessageCodes.PEER_ENTITY_ACTIVATED, new DataObject()
										.set(MessageCodes.PEER_ENTITY_ACTIVATED_ENTITY_ID, peerEntity)),
								SendOptions.ReliableSend);
					}
				}
			}
		}
	}

	private void checkAndNotifyAboutDeactivatedPeerEntity(VastPeer peer) {
		int peerEntity = entitiesByPeer.get(peer.getName());
		if (activeComponentMapper.has(peerEntity)) {
			String name = peerComponentMapper.get(peerEntity).name;
			if (!peers.containsKey(name)) {
				logger.info("Deactivating peer entity: {} for {}", peerEntity, name);
				activeComponentMapper.remove(peerEntity);
				for (VastPeer peerToSendTo : peers.values()) {
					if (isEntityKnownByPeer(peerEntity, peerToSendTo)) {
						peerToSendTo.send(new EventMessage(MessageCodes.PEER_ENTITY_DEACTIVATED, new DataObject()
										.set(MessageCodes.PEER_ENTITY_DEACTIVATED_ENTITY_ID, peerEntity)),
								SendOptions.ReliableSend);
					}
				}
			}
		}
	}

	private void markEntityAsKnownByPeer(int entity, VastPeer peer) {
		Set<Integer> entities;
		if (knownEntitiesByPeer.containsKey(peer.getName())) {
			entities = knownEntitiesByPeer.get(peer.getName());
		} else {
			entities = new HashSet<Integer>();
			knownEntitiesByPeer.put(peer.getName(), entities);
		}
		if (!entities.contains(entity)) {
			entities.add(entity);
		}
	}

	private void removeEntitiesAsKnownByPeer(List<Integer> entities, VastPeer peer) {
		knownEntitiesByPeer.get(peer.getName()).removeAll(entities);
	}

	private boolean isEntityKnownByPeer(int entity, VastPeer peer) {
		if (knownEntitiesByPeer.containsKey(peer.getName())) {
			Set<Integer> entities = knownEntitiesByPeer.get(peer.getName());
			if (entities.contains(entity)) {
				return true;
			}
		}
		return false;
	}
}
