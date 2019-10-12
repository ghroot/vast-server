package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.component.*;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeleteSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeleteSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Know> knowMapper;
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
	protected void process(int deleteEntity) {
		String reason = deleteMapper.get(deleteEntity).reason;
		logger.debug("Deleting entity {} ({})", deleteEntity, reason);

		if (knownMapper.has(deleteEntity)) {
			Set<Integer> knownByEntities = knownMapper.get(deleteEntity).knownByEntities;
			for (int entityToNotify : knownByEntities) {
				VastPeer peer = peers.get(playerMapper.get(entityToNotify).name);
				if (peer != null) {
					notifyAboutRemovedEntity(peer, deleteEntity, reason);
				}
				knowMapper.get(entityToNotify).knowEntities.remove(deleteEntity);
			}
		}

		world.delete(deleteEntity);
	}

	private void notifyAboutRemovedEntity(VastPeer peer, int deleteEntity, String reason) {
		logger.debug("Notifying peer {} about removed entity {} ({})", peer.getName(), deleteEntity, reason);
		reusableEventMessage.getDataObject().clear();
		reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, deleteEntity);
		reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_DESTROYED_REASON, reason);
		peer.send(reusableEventMessage);
	}
}
