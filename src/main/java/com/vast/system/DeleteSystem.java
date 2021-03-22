package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.component.Delete;
import com.vast.component.Invisible;
import com.vast.component.Known;
import com.vast.component.Observer;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DeleteSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeleteSystem.class);

	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Known> knownMapper;

	private Map<String, VastPeer> peers;

	private EventMessage reusableEventMessage;

	public DeleteSystem() {
		super(Aspect.one(Delete.class).exclude(Invisible.class));

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
			IntBag knownByEntitiesBag = knownMapper.get(deleteEntity).knownByEntities;
			int[] knownByEntities = knownByEntitiesBag.getData();
			for (int i = 0, size = knownByEntitiesBag.size(); i < size; ++i) {
				int entityToNotify = knownByEntities[i];
				if (observerMapper.has(entityToNotify)) {
					Observer observer = observerMapper.get(entityToNotify);
					notifyAboutRemovedEntity(observer.peer, deleteEntity, reason);
					observer.knowEntities.removeValue(deleteEntity);
				}
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
