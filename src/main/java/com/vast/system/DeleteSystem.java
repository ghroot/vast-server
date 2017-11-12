package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.VastPeer;
import com.vast.component.Delete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class DeleteSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeleteSystem.class);

	private ComponentMapper<Delete> deleteMapper;

	private Map<String, VastPeer> peers;
	private Map<String, Set<Integer>> knownEntitiesByPeer;

	public DeleteSystem(Map<String, VastPeer> peers, Map<String, Set<Integer>> knownEntitiesByPeer) {
		super(Aspect.one(Delete.class));
		this.peers = peers;
		this.knownEntitiesByPeer = knownEntitiesByPeer;
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int deleteEntity) {
		for (VastPeer peer : peers.values()) {
			if (knownEntitiesByPeer.containsKey(peer.getName()) && knownEntitiesByPeer.get(peer.getName()).contains(deleteEntity)) {
				notifyAboutRemovedEntity(peer, deleteEntity);
				knownEntitiesByPeer.get(peer.getName()).remove(deleteEntity);
				world.delete(deleteEntity);
			}
		}
	}

	private void notifyAboutRemovedEntity(VastPeer peer, int deleteEntity) {
		String reason = deleteMapper.get(deleteEntity).reason;
		logger.debug("Notifying peer {} about removed entity {} ({})", peer.getName(), deleteEntity, reason);
		peer.send(new EventMessage(MessageCodes.ENTITY_DESTROYED, new DataObject()
						.set(MessageCodes.ENTITY_DESTROYED_ENTITY_ID, deleteEntity)
						.set(MessageCodes.ENTITY_DESTROYED_REASON, reason)),
				SendOptions.ReliableSend);
	}
}
