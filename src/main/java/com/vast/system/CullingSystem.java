package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class CullingSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CullingSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Interactable> interactableMapper;

	private Map<String, VastPeer> peers;

	private float[] reusablePosition;
	private List<Integer> reusableRemovedEntities;

	public CullingSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Player.class, Active.class, Scan.class, Known.class));
		this.peers = peers;

		reusablePosition = new float[2];
		reusableRemovedEntities = new ArrayList<Integer>();
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int activePlayerEntity) {
		Player player = playerMapper.get(activePlayerEntity);
		Scan scan = scanMapper.get(activePlayerEntity);
		Known known = knownMapper.get(activePlayerEntity);

		if (peers.containsKey(player.name)) {
			VastPeer peer = peers.get(player.name);
			notifyAboutRemovedEntities(peer, scan.nearbyEntities, known.knownEntities);
			notifyAboutNewEntities(peer, scan.nearbyEntities, known.knownEntities);
		}
	}

	private void notifyAboutRemovedEntities(VastPeer peer, Set<Integer> nearbyEntities, Set<Integer> knownEntities) {
		reusableRemovedEntities.clear();
		for (int knownEntity : knownEntities) {
			if (!nearbyEntities.contains(knownEntity)) {
				notifyAboutRemovedEntity(peer, knownEntity);
				reusableRemovedEntities.add(knownEntity);
			}
		}
		knownEntities.removeAll(reusableRemovedEntities);
	}

	private void notifyAboutRemovedEntity(VastPeer peer, int deletedEntity) {
		logger.debug("Notifying peer {} about removed entity {} (culling)", peer.getName(), deletedEntity);
		peer.send(new EventMessage(MessageCodes.ENTITY_DESTROYED, new DataObject()
						.set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, deletedEntity)
						.set(MessageCodes.ENTITY_DESTROYED_REASON, "culling")),
				SendOptions.ReliableSend);
	}

	private void notifyAboutNewEntities(VastPeer peer, Set<Integer> nearbyEntities, Set<Integer> knownEntities) {
		for (int nearbyEntity : nearbyEntities) {
			if (!knownEntities.contains(nearbyEntity)) {
				notifyAboutNewEntity(peer, nearbyEntity);
				knownEntities.add(nearbyEntity);
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
