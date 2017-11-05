package test.system;

import com.artemis.*;
import com.artemis.utils.IntBag;
import com.nhnent.haste.framework.ClientPeer;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.MessageCodes;
import test.MyPeer;
import test.component.ActiveComponent;
import test.component.PeerComponent;
import test.component.SyncTransformComponent;
import test.component.TransformComponent;

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

	private List<MyPeer> peers;
	private Map<String, Integer> entitiesByName;

	private List<MyPeer> peersLastUpdate;
	private Map<String, List<Integer>> knownEntities;
	private Archetype archetype;

	public PeerEntitySystem(List<MyPeer> peers, Map<String, Integer> entitiesByName) {
		this.peers = peers;
		this.entitiesByName = entitiesByName;

		peersLastUpdate = new ArrayList<MyPeer>();
		knownEntities = new HashMap<String, List<Integer>>();
	}

	@Override
	protected void initialize() {
		archetype = new ArchetypeBuilder()
				.add(PeerComponent.class)
				.add(TransformComponent.class)
				.add(SyncTransformComponent.class)
				.build(world);
	}

	@Override
	protected void processSystem() {
		createEntitiesForNewPeers();
		checkAndNotifyAboutNewEntities();
		checkAndNotifyAboutActivatedEntities();
		checkAndNotifyAboutDeactivatedEntities();

		peersLastUpdate.clear();
		peersLastUpdate.addAll(peers);
	}

	private void createEntitiesForNewPeers() {
		for (MyPeer peer : peers) {
			if (!peersLastUpdate.contains(peer)) {
				if (!entitiesByName.containsKey(peer.getName())) {
					int entity = world.create(archetype);
					logger.info("Creating entity: {} for {}", entity, peer.getName());
					peerComponentMapper.get(entity).name = peer.getName();
					entitiesByName.put(peer.getName(), entity);
				}
				clearEntitiesKnownByPeer(peer);
			}
		}
	}

	private void checkAndNotifyAboutNewEntities() {
		IntBag transformEntities = world.getAspectSubscriptionManager().get(Aspect.all(TransformComponent.class)).getEntities();
		for (MyPeer peer : peers) {
			for (int i = 0; i < transformEntities.size(); i++) {
				int transformEntity = transformEntities.get(i);
				if (!isEntityKnownByPeer(transformEntity, peer)) {
					boolean owner = false;
					boolean active = true;
					if (peerComponentMapper.has(transformEntity)) {
						PeerComponent peerComponent = peerComponentMapper.get(transformEntity);
						owner = peer.getName().equals(peerComponent.name);
						active = activeComponentMapper.has(transformEntity);
					}
					TransformComponent transformComponent = transformComponentMapper.get(transformEntity);
					logger.info("Notifying peer {} about new entity {}", peer.getName(), transformEntity);
					peer.send(new EventMessage(MessageCodes.ENTITY_CREATED, new DataObject()
									.set(MessageCodes.ENTITY_CREATED_ENTITY_ID, transformEntity)
									.set(MessageCodes.ENTITY_CREATED_OWNER, owner)
									.set(MessageCodes.ENTITY_CREATED_ACTIVE, active)
									.set(MessageCodes.ENTITY_CREATED_POSITION, new float[] {transformComponent.position.x, transformComponent.position.y})),
							SendOptions.ReliableSend);
					markEntityAsKnownByPeer(transformEntity, peer);
				}
			}
		}
	}

	private void checkAndNotifyAboutActivatedEntities() {
		for (MyPeer peer : peers) {
			if (!peersLastUpdate.contains(peer)) {
				String name = peer.getName();
				int entity = entitiesByName.get(name);
				if (!activeComponentMapper.has(entity)) {
					logger.info("Activating entity: {} for {}", entity, name);
					activeComponentMapper.create(entity);
					for (ClientPeer peerToSendTo : peers) {
						peerToSendTo.send(new EventMessage(MessageCodes.ENTITY_ACTIVATED, new DataObject()
										.set(MessageCodes.ENTITY_ACTIVATED_ENTITY_ID, entity)),
								SendOptions.ReliableSend);
					}
				}
			}
		}
	}

	private void checkAndNotifyAboutDeactivatedEntities() {
		for (MyPeer peer : peersLastUpdate) {
			if (!peers.contains(peer)) {
				String name = peer.getName();
				int entity = entitiesByName.get(name);
				if (activeComponentMapper.has(entity)) {
					logger.info("Deactivating entity: {} for {}", entity, name);
					activeComponentMapper.remove(entity);
					for (ClientPeer peerToSendTo : peers) {
						peerToSendTo.send(new EventMessage(MessageCodes.ENTITY_DEACTIVATED, new DataObject()
										.set(MessageCodes.ENTITY_DEACTIVATED_ENTITY_ID, entity)),
								SendOptions.ReliableSend);
					}
				}
			}
		}
	}

	private void markEntityAsKnownByPeer(int entity, MyPeer peer) {
		List<Integer> entities = null;
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

	private void clearEntitiesKnownByPeer(MyPeer peer) {
		if (knownEntities.containsKey(peer.getName())) {
			List<Integer> entities = knownEntities.get(peer.getName());
			entities.clear();
		}
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
