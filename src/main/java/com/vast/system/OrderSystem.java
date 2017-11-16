package com.vast.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.vast.IncomingRequest;
import com.vast.component.Order;
import com.vast.component.Player;
import com.vast.order.OrderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class OrderSystem extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(OrderSystem.class);

	private ComponentMapper<Order> orderMapper;
	private ComponentMapper<Player> playerMapper;

	private List<IncomingRequest> incomingRequests;
	private Set<OrderHandler> orderHandlers;

	public OrderSystem(List<IncomingRequest> incomingRequests, Set<OrderHandler> orderHandlers) {
		this.incomingRequests = incomingRequests;
		this.orderHandlers = orderHandlers;
	}

	@Override
	protected void initialize() {
		for (OrderHandler orderHandler : orderHandlers) {
			world.inject(orderHandler);
			orderHandler.initialize();
		}
	}

	@Override
	protected void processSystem() {
		checkCompleteOrders();

		for (Iterator<IncomingRequest> iterator = incomingRequests.iterator(); iterator.hasNext();) {
			IncomingRequest request = iterator.next();
			int playerEntity = getEntityWithPeerName(request.getPeer().getName());

			if (orderMapper.has(playerEntity)) {
				Order order = orderMapper.get(playerEntity);
				logger.debug("Canceling {} order for entity {}", playerEntity, order.type);
				OrderHandler orderHandler = getOrderHandler(order.type);
				if (orderHandler != null) {
					orderHandler.cancelOrder(playerEntity);
				} else {
					logger.warn("No order handler found for type {}", order.type);
				}
				orderMapper.remove(playerEntity);
			}

			OrderHandler orderHandler = getOrderHandler(request.getMessage().getCode());
			if (orderHandler != null) {
				if (orderHandler.startOrder(playerEntity, request.getMessage().getDataObject())) {
					orderMapper.create(playerEntity).type = orderHandler.getOrderType();
					logger.debug("Starting {} order for entity {}", orderHandler.getOrderType(), playerEntity);
				}
				iterator.remove();
			}
		}
	}

	private void checkCompleteOrders() {
		IntBag orderEntities = world.getAspectSubscriptionManager().get(Aspect.all(Order.class)).getEntities();
		for (int i = orderEntities.size() - 1; i >= 0; i--) {
			int orderEntity = orderEntities.get(i);
			Order order = orderMapper.get(orderEntity);
			OrderHandler orderHandler = getOrderHandler(order.type);
			if (orderHandler != null) {
				if (orderHandler.isOrderComplete(orderEntity)) {
					logger.debug("{} order completed for entity {}", order.type, orderEntity);
					orderMapper.remove(orderEntity);
				}
			} else {
				logger.warn("No order handler found for type {}", order.type);
			}
		}
	}

	private OrderHandler getOrderHandler(Order.Type type) {
		for (OrderHandler orderHandler : orderHandlers) {
			if (orderHandler.getOrderType() == type) {
				return orderHandler;
			}
		}
		return null;
	}

	private OrderHandler getOrderHandler(short messageCode) {
		for (OrderHandler orderHandler : orderHandlers) {
			if (orderHandler.getMessageCode() == messageCode) {
				return orderHandler;
			}
		}
		return null;
	}

	private int getEntityWithPeerName(String name) {
		IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class)).getEntities();
		for (int i = 0; i < entities.size(); i++) {
			int entity = entities.get(i);
			if (playerMapper.get(entity).name.equals(name)) {
				return entity;
			}
		}
		return -1;
	}
}
