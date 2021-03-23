package com.vast.order.handler.avatar;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.vast.component.Interact;
import com.vast.component.Path;
import com.vast.interact.InteractionHandler;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.avatar.InteractOrderRequest;
import com.vast.order.request.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractOrderHandler extends AbstractOrderHandler<InteractOrderRequest> {
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
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof InteractOrderRequest;
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
	public boolean startOrder(int orderEntity, InteractOrderRequest interactOrderRequest) {
		if (canInteract(orderEntity, interactOrderRequest.getEntity())) {
			interactMapper.create(orderEntity).entity = interactOrderRequest.getEntity();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean modifyOrder(int orderEntity, InteractOrderRequest interactOrderRequest) {
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
