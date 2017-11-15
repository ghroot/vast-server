package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Profile(enabled = true, using = Profiler.class)
public class CullingSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CullingSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Interactable> interactableMapper;

	private Map<String, VastPeer> peers;
	private Map<String, Integer> entitiesByPeer;
	private Map<String, Set<Integer>> knownEntitiesByPeer;
	private Map<Integer, Set<Integer>> nearbyEntitiesByEntity;

	private float[] reusablePosition;
	private List<Integer> reusableRemovedEntities;

	public CullingSystem(Map<String, VastPeer> peers, Map<String, Integer> entitiesByPeer, Map<String, Set<Integer>> knownEntitiesByPeer, Map<Integer, Set<Integer>> nearbyEntitiesByEntity) {
		super(Aspect.all(Player.class, Active.class));
		this.peers = peers;
		this.entitiesByPeer = entitiesByPeer;
		this.knownEntitiesByPeer = knownEntitiesByPeer;
		this.nearbyEntitiesByEntity = nearbyEntitiesByEntity;

		reusablePosition = new float[2];
		reusableRemovedEntities = new ArrayList<Integer>();
	}

	@Override
	protected void inserted(int activePlayerEntity) {
		Player player = playerMapper.get(activePlayerEntity);
		knownEntitiesByPeer.put(player.name, new HashSet<Integer>());
	}

	@Override
	protected void removed(int activePlayerEntity) {
		Player player = playerMapper.get(activePlayerEntity);
		knownEntitiesByPeer.remove(player.name);
	}

	@Override
	protected void process(int activePlayerEntity) {
		Player player = playerMapper.get(activePlayerEntity);

		if (peers.containsKey(player.name)) {
			VastPeer peer = peers.get(player.name);
			notifyAboutRemovedEntities(peer);
			notifyAboutNewEntities(peer);
		}
	}

	private void notifyAboutRemovedEntities(VastPeer peer) {
		int playerEntity = entitiesByPeer.get(peer.getName());
		Set<Integer> nearbyEntities = nearbyEntitiesByEntity.get(playerEntity);
		reusableRemovedEntities.clear();
		for (int knownEntity : knownEntitiesByPeer.get(peer.getName())) {
			if (!nearbyEntities.contains(knownEntity)) {
				notifyAboutRemovedEntity(peers.get(peer.getName()), knownEntity);
				reusableRemovedEntities.add(knownEntity);
			}
		}
		knownEntitiesByPeer.get(peer.getName()).removeAll(reusableRemovedEntities);
	}

	private void notifyAboutRemovedEntity(VastPeer peer, int deletedEntity) {
		logger.debug("Notifying peer {} about removed entity {} (culling)", peer.getName(), deletedEntity);
		peer.send(new EventMessage(MessageCodes.ENTITY_DESTROYED, new DataObject()
						.set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, deletedEntity)
						.set(MessageCodes.ENTITY_DESTROYED_REASON, "culling")),
				SendOptions.ReliableSend);
	}

	private void notifyAboutNewEntities(VastPeer peer) {
		int playerEntity = entitiesByPeer.get(peer.getName());
		Set<Integer> nearbyEntities = nearbyEntitiesByEntity.get(playerEntity);
		for (int nearbyEntity : nearbyEntities) {
			if (!knownEntitiesByPeer.get(peer.getName()).contains(nearbyEntity)) {
				notifyAboutNewEntity(peers.get(peer.getName()), nearbyEntity);
				knownEntitiesByPeer.get(peer.getName()).add(nearbyEntity);
			}
		}
	}

	private void notifyAboutNewEntity(VastPeer peer, int newEntity) {
		logger.debug("Notifying peer {} about new entity {} (culling)", peer.getName(), newEntity);
		Transform transform = transformMapper.get(newEntity);
		reusablePosition[0] = transform.position.x;
		reusablePosition[1] = transform.position.y;
		if (playerMapper.has(newEntity)) {
			Player player = playerMapper.get(newEntity);
			boolean owner = peer.getName().equals(player.name);
			boolean active = activeMapper.has(newEntity);
			peer.send(new EventMessage(MessageCodes.PEER_ENTITY_CREATED, new DataObject()
							.set(MessageCodes.PEER_ENTITY_CREATED_ENTITY_ID, newEntity)
							.set(MessageCodes.PEER_ENTITY_CREATED_OWNER, owner)
							.set(MessageCodes.PEER_ENTITY_CREATED_ACTIVE, active)
							.set(MessageCodes.PEER_ENTITY_CREATED_POSITION, reusablePosition)
							.set(MessageCodes.PEER_ENTITY_CREATED_REASON, "culling")),
					SendOptions.ReliableSend);
		} else {
			Type type = typeMapper.get(newEntity);
			peer.send(new EventMessage(MessageCodes.ENTITY_CREATED, new DataObject()
							.set(MessageCodes.ENTITY_CREATED_ENTITY_ID, newEntity)
							.set(MessageCodes.ENTITY_CREATED_TYPE, type.type)
							.set(MessageCodes.ENTITY_CREATED_POSITION, reusablePosition)
							.set(MessageCodes.ENTITY_CREATED_REASON, "culling")
							.set(MessageCodes.ENTITY_CREATED_INTERACTABLE, interactableMapper.has(newEntity))),
					SendOptions.ReliableSend);
		}
	}
}
