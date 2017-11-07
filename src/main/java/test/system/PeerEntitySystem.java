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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerEntitySystem extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(PeerEntitySystem.class);

	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<SyncTransformComponent> syncTransformComponentMapper;
	private ComponentMapper<ActiveComponent> activeComponentMapper;
	private ComponentMapper<TypeComponent> typeComponentMapper;

	private List<MyPeer> peers;
	private Map<String, Integer> entitiesByPeerName;
	private Map<Integer, List<Integer>> nearbyEntitiesByEntity;

	private List<MyPeer> peersLastUpdate;
	private Map<String, List<Integer>> knownEntities;
	private List<Integer> reusableRemovedEntities;
	private float[] reusablePosition;
	private Archetype peerEntityArchetype;

	public PeerEntitySystem(List<MyPeer> peers, Map<String, Integer> entitiesByPeerName, Map<Integer, List<Integer>> nearbyEntitiesByEntity) {
		this.peers = peers;
		this.entitiesByPeerName = entitiesByPeerName;
		this.nearbyEntitiesByEntity = nearbyEntitiesByEntity;

		peersLastUpdate = new ArrayList<MyPeer>();
		knownEntities = new HashMap<String, List<Integer>>();
		reusableRemovedEntities = new ArrayList<Integer>();
		reusablePosition = new float[2];
	}

	@Override
	protected void initialize() {
		peerEntityArchetype = new ArchetypeBuilder()
				.add(PeerComponent.class)
				.add(TransformComponent.class)
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
				knownEntities.put(peer.getName(), new ArrayList<Integer>());
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
			if (nearbyEntitiesByEntity.containsKey(peerEntity)) {
				List<Integer> closeEntities = nearbyEntitiesByEntity.get(peerEntity);
				for (int closeEntity : closeEntities) {
					if (!isEntityKnownByPeer(closeEntity, peer)) {
						logger.info("Notifying peer {} about new entity {}", peer.getName(), closeEntity);
						TransformComponent transformComponent = transformComponentMapper.get(closeEntity);
						reusablePosition[0] = transformComponent.position.x;
						reusablePosition[1] = transformComponent.position.y;
						if (peerComponentMapper.has(closeEntity)) {
							PeerComponent peerComponent = peerComponentMapper.get(closeEntity);
							boolean owner = peer.getName().equals(peerComponent.name);
							boolean active = activeComponentMapper.has(closeEntity);
							peer.send(new EventMessage(MessageCodes.PEER_ENTITY_CREATED, new DataObject()
											.set(MessageCodes.PEER_ENTITY_CREATED_ENTITY_ID, closeEntity)
											.set(MessageCodes.PEER_ENTITY_CREATED_OWNER, owner)
											.set(MessageCodes.PEER_ENTITY_CREATED_ACTIVE, active)
											.set(MessageCodes.PEER_ENTITY_CREATED_POSITION, reusablePosition)),
									SendOptions.ReliableSend);
						} else {
							TypeComponent typeComponent = typeComponentMapper.get(closeEntity);
							peer.send(new EventMessage(MessageCodes.ENTITY_CREATED, new DataObject()
											.set(MessageCodes.ENTITY_CREATED_ENTITY_ID, closeEntity)
											.set(MessageCodes.ENTITY_CREATED_TYPE, typeComponent.type)
											.set(MessageCodes.ENTITY_CREATED_POSITION, reusablePosition)),
									SendOptions.ReliableSend);
						}
						markEntityAsKnownByPeer(closeEntity, peer);
					}
				}
			}
		}
	}

	private void checkAndNotifyAboutRemovedEntities() {
		for (MyPeer peer : peers) {
			int peerEntity = entitiesByPeerName.get(peer.getName());
			if (knownEntities.containsKey(peer.getName())) {
				List<Integer> entitiesKnownByPeer = knownEntities.get(peer.getName());
				reusableRemovedEntities.clear();
				for (int entityKnownByPeer : entitiesKnownByPeer) {
					if (!nearbyEntitiesByEntity.get(peerEntity).contains(entityKnownByPeer)) {
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
		List<Integer> entities;
		if (knownEntities.containsKey(peer.getName())) {
			entities = knownEntities.get(peer.getName());
		} else {
			entities = new ArrayList<Integer>();
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
			List<Integer> entities = knownEntities.get(peer.getName());
			if (entities.contains(entity)) {
				return true;
			}
		}
		return false;
	}
}
