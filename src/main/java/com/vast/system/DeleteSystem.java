package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.VastPeer;
import com.vast.component.Active;
import com.vast.component.Delete;
import com.vast.component.Known;
import com.vast.component.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class DeleteSystem extends AbstractNearbyEntityIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeleteSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Known> knownMapper;

	private Map<String, VastPeer> peers;

	private EventMessage reusableEventMessage;

	public DeleteSystem(Map<String, VastPeer> peers) {
		super(Aspect.one(Delete.class));
		this.peers = peers;

		reusableEventMessage = new EventMessage(MessageCodes.ENTITY_DESTROYED);
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int deleteEntity, Set<Integer> nearbyEntities) {
		String reason = deleteMapper.get(deleteEntity).reason;
		logger.debug("Deleting entity {} ({})", deleteEntity, reason);
		for (int nearbyEntity : nearbyEntities) {
			if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity) && knownMapper.has(nearbyEntity)) {
				Set<Integer> knownEntities = knownMapper.get(nearbyEntity).knownEntities;
				if (knownEntities.contains(deleteEntity)) {
					VastPeer peer = peers.get(playerMapper.get(nearbyEntity).name);
					notifyAboutRemovedEntity(peer, deleteEntity, reason);
					knownEntities.remove(deleteEntity);
				}
			}
		}
		world.delete(deleteEntity);
	}

	private void notifyAboutRemovedEntity(VastPeer peer, int deleteEntity, String reason) {
		logger.debug("Notifying peer {} about removed entity {} ({})", peer.getName(), deleteEntity, reason);
		reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, deleteEntity);
		reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_REASON, reason);
		peer.send(reusableEventMessage);
	}
}
