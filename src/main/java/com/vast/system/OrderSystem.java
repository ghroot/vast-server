package com.vast.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.IncomingRequest;
import com.vast.component.Order;
import com.vast.order.OrderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderSystem extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(OrderSystem.class);

	private ComponentMapper<Order> orderMapper;

	private Set<OrderHandler> orderHandlers;
	private List<IncomingRequest> incomingRequests;
	private Map<String, Integer> entitiesByPeer;

	public OrderSystem(Set<OrderHandler> orderHandlers, List<IncomingRequest> incomingRequests, Map<String, Integer> entitiesByPeer) {
		this.orderHandlers = orderHandlers;
		this.incomingRequests = incomingRequests;
		this.entitiesByPeer = entitiesByPeer;
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
		completeOrders();

		for (Iterator<IncomingRequest> iterator = incomingRequests.iterator(); iterator.hasNext();) {
			IncomingRequest request = iterator.next();
			int playerEntity = entitiesByPeer.get(request.getPeer().getName());
			if (orderMapper.has(playerEntity)) {
				cancelOrder(playerEntity);
			} else {
				OrderHandler handler = getOrderHandler(request.getMessage().getCode());
				if (handler != null) {
					startOrder(playerEntity, handler, request.getMessage().getDataObject());
				} else {
					logger.warn("Could not find order handler for message code {}", request.getMessage().getCode());
				}
				iterator.remove();
			}
		}
	}

	private void completeOrders() {
		IntBag orderEntities = world.getAspectSubscriptionManager().get(Aspect.all(Order.class)).getEntities();
		for (int i = orderEntities.size() - 1; i >= 0; i--) {
			int orderEntity = orderEntities.get(i);
			if (orderMapper.has(orderEntity)) {
				Order order = orderMapper.get(orderEntity);
				if (order.handler.isOrderComplete(orderEntity)) {
					logger.debug("{} order completed for entity {}", order.type, orderEntity);
					orderMapper.remove(orderEntity);
				}
			}
		}
	}

	private void cancelOrder(int orderEntity) {
		Order order = orderMapper.get(orderEntity);
		logger.debug("Canceling {} order for entity {}", order.type, orderEntity);
		order.handler.cancelOrder(orderEntity);
		orderMapper.remove(orderEntity);
	}

	private void startOrder(int orderEntity, OrderHandler handler, DataObject dataObject) {
		if (handler.startOrder(orderEntity, dataObject)) {
			orderMapper.create(orderEntity).type = handler.getOrderType();
			orderMapper.get(orderEntity).handler = handler;
			logger.debug("Starting {} order for entity {}", handler.getOrderType(), orderEntity);
		}
	}

	private OrderHandler getOrderHandler(short messageCode) {
		for (OrderHandler orderHandler : orderHandlers) {
			if (orderHandler.getMessageCode() == messageCode) {
				return orderHandler;
			}
		}
		return null;
	}
}
