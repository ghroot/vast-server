package com.vast.order;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractOrderHandler implements OrderHandler {
	private static final Logger logger = LoggerFactory.getLogger(InteractOrderHandler.class);

	private World world;

	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Interactable> interactableMapper;
	private ComponentMapper<Path> pathMapper;

	@Override
	public void initialize() {
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.INTERACT;
	}

	@Override
	public Order.Type getOrderType() {
		return Order.Type.INTERACT;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return !interactMapper.has(orderEntity);
	}

	@Override
	public void cancelOrder(int orderEntity) {
		interactMapper.remove(orderEntity);
		pathMapper.remove(orderEntity);
	}

	@Override
	public boolean startOrder(int orderEntity, DataObject dataObject) {
		int otherEntity = (int) dataObject.get(MessageCodes.INTERACT_ENTITY_ID).value;
		if (interactableMapper.has(otherEntity)) {
			if (isInteractableEntityBeingInteractedWith(otherEntity)) {
				logger.debug("Player entity {} tried to interact with busy entity {}", orderEntity, otherEntity);
			} else {
				if (!interactMapper.has(orderEntity)) {
					interactMapper.create(orderEntity);
				}
				interactMapper.get(orderEntity).entity = otherEntity;
				return true;
			}
		} else {
			logger.debug("Player entity {} tried to interact with non-interactable entity {}", orderEntity, otherEntity);
		}
		return false;
	}

	private boolean isInteractableEntityBeingInteractedWith(int interactableEntity) {
		IntBag interactionEntities = world.getAspectSubscriptionManager().get(Aspect.one(Interact.class)).getEntities();
		for (int i = 0; i < interactionEntities.size(); i++) {
			int interactionEntity = interactionEntities.get(i);
			if (interactMapper.has(interactionEntity) && interactMapper.get(interactionEntity).entity == interactableEntity) {
				return true;
			}
		}
		return false;
	}
}
