package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.framework.SendOptions;
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
	private ComponentMapper<Building> buildingMapper;

	private Map<String, VastPeer> peers;

	private float[] reusablePosition;
	private List<Integer> reusableRemovedEntities;
	private EventMessage reusableDestroyedEventMessage;
	private EventMessage reusableCreatedEventMessage;

	public CullingSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Player.class, Active.class, Scan.class, Known.class));
		this.peers = peers;

		reusablePosition = new float[2];
		reusableRemovedEntities = new ArrayList<Integer>();
		reusableDestroyedEventMessage = new EventMessage(MessageCodes.ENTITY_DESTROYED);
		reusableCreatedEventMessage = new EventMessage(MessageCodes.ENTITY_CREATED);
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
		reusableDestroyedEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, deletedEntity);
		reusableDestroyedEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_REASON, "culling");
		peer.send(reusableDestroyedEventMessage, SendOptions.ReliableSend);
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
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_ENTITY_ID, newEntity);
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_REASON, "culling");
		// TODO: These properties should be handled by separate handlers (maybe the sync handlers?)
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.PROPERTY_TYPE, typeMapper.get(newEntity).type);
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.PROPERTY_POSITION, getDataObjectPosition(transformMapper.get(newEntity)));
		reusableCreatedEventMessage.getDataObject().set(MessageCodes.PROPERTY_INTERACTABLE, interactableMapper.has(newEntity));
		if (playerMapper.has(newEntity)) {
			reusableCreatedEventMessage.getDataObject().set(MessageCodes.PROPERTY_OWNER, peer.getName().equals(playerMapper.get(newEntity).name));
			reusableCreatedEventMessage.getDataObject().set(MessageCodes.PROPERTY_ACTIVE, activeMapper.has(newEntity));
		}
		if (buildingMapper.has(newEntity)) {
			reusableCreatedEventMessage.getDataObject().set(MessageCodes.PROPERTY_PROGRESS, buildingMapper.get(newEntity).progress);
		}
		peer.send(reusableCreatedEventMessage, SendOptions.ReliableSend);
	}

	private float[] getDataObjectPosition(Transform transform) {
		reusablePosition[0] = transform.position.x;
		reusablePosition[1] = transform.position.y;
		return reusablePosition;
	}
}
