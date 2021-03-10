package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Interact;
import com.vast.component.Path;
import com.vast.interact.InteractionHandler;
import com.vast.network.MessageCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractOrderHandler implements OrderHandler {
	private static final Logger logger = LoggerFactory.getLogger(InteractOrderHandler.class);

	private World world;

	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;

	private InteractionHandler[] interactionHandlers;

	public InteractOrderHandler(InteractionHandler[] interactionHandlers) {
		this.interactionHandlers = interactionHandlers;
	}

	@Override
	public void initialize() {
	}

	@Override
	public boolean handlesMessageCode(short messageCode) {
		return messageCode == MessageCodes.INTERACT;
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
		if (canInteract(orderEntity, otherEntity)) {
			interactMapper.create(orderEntity).entity = otherEntity;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean modifyOrder(int orderEntity, DataObject dataObject) {
		return false;
	}

	private boolean canInteract(int entity, int otherEntity) {
		for (InteractionHandler interactionHandler : interactionHandlers) {
			if (interactionHandler.getAspect1().isInterested(world.getEntity(entity)) &&
				interactionHandler.getAspect2().isInterested(world.getEntity(otherEntity)) &&
				interactionHandler.canInteract(entity, otherEntity)) {
				return true;
			}
		}
		return false;
	}
}
