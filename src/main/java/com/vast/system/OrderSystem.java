package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Avatar;
import com.vast.component.Order;
import com.vast.network.IncomingRequest;
import com.vast.order.OrderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class OrderSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(OrderSystem.class);

	private ComponentMapper<Order> orderMapper;
	private ComponentMapper<Avatar> avatarMapper;

	private OrderHandler[] orderHandlers;
	private Map<String, List<IncomingRequest>> incomingRequestsByPeer;

	public OrderSystem(OrderHandler[] orderHandlers, Map<String, List<IncomingRequest>> incomingRequestsByPeer) {
		super(Aspect.all(Avatar.class));
		this.orderHandlers = orderHandlers;
		this.incomingRequestsByPeer = incomingRequestsByPeer;
	}

	@Override
	protected void initialize() {
		for (OrderHandler orderHandler : orderHandlers) {
			world.inject(orderHandler);
			orderHandler.initialize();
		}
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int avatarEntity) {
		Avatar avatar = avatarMapper.get(avatarEntity);

		if (orderMapper.has(avatarEntity)) {
			Order order = orderMapper.get(avatarEntity);
			if (order.handler.isOrderComplete(avatarEntity)) {
				logger.debug("Order completed for entity {} with handler {}", avatarEntity, order.handler.getClass().getSimpleName());
				orderMapper.remove(avatarEntity);
			}
		}

		if (incomingRequestsByPeer.containsKey(avatar.name)) {
			List<IncomingRequest> incomingRequests = incomingRequestsByPeer.get(avatar.name);
			if (incomingRequests.size() > 0) {
				IncomingRequest lastIncomingRequest = incomingRequests.get(incomingRequests.size() - 1);
				if (orderMapper.has(avatarEntity)) {
					Order order = orderMapper.get(avatarEntity);
					if (order.handler.handlesMessageCode(lastIncomingRequest.getMessage().getCode()) &&
							order.handler.modifyOrder(avatarEntity, lastIncomingRequest.getMessage().getCode(), lastIncomingRequest.getMessage().getDataObject())) {
						incomingRequests.clear();
						logger.debug("Modifying order for entity {} with handler {}", avatarEntity, order.handler.getClass().getSimpleName());
					} else {
						cancelOrder(avatarEntity);
					}
				} else {
					incomingRequests.clear();
					OrderHandler handler = getOrderHandler(lastIncomingRequest.getMessage().getCode());
					if (handler != null) {
						startOrder(avatarEntity, handler, lastIncomingRequest.getMessage().getCode(), lastIncomingRequest.getMessage().getDataObject());
					} else {
						logger.warn("Could not find order handler for message code {}", lastIncomingRequest.getMessage().getCode());
					}
				}
			}
		}
	}

	private void cancelOrder(int playerEntity) {
		Order order = orderMapper.get(playerEntity);
		if (order != null) {
			if (order.handler != null) {
				logger.debug("Canceling order for entity {} with handler {}", playerEntity, order.handler.getClass().getSimpleName());
				order.handler.cancelOrder(playerEntity);
			} else {
				logger.debug("Canceling order for entity {}", playerEntity);
			}
			orderMapper.remove(playerEntity);
		}
	}

	private void startOrder(int playerEntity, OrderHandler handler, short messageCode, DataObject dataObject) {
		if (handler.startOrder(playerEntity, messageCode, dataObject)) {
			orderMapper.create(playerEntity).handler = handler;
			logger.debug("Starting order for entity {} with handler {}", playerEntity, handler.getClass().getSimpleName());
		} else {
			logger.debug("Could not start order for entity {} with handler {}", playerEntity, handler.getClass().getSimpleName());
		}
	}

	private OrderHandler getOrderHandler(short messageCode) {
		for (OrderHandler orderHandler : orderHandlers) {
			if (orderHandler.handlesMessageCode(messageCode)) {
				return orderHandler;
			}
		}
		return null;
	}
}
