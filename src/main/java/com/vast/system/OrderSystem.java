package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Order;
import com.vast.component.OrderQueue;
import com.vast.order.handler.OrderHandler;
import com.vast.order.request.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(OrderSystem.class);

	private ComponentMapper<Order> orderMapper;
	private ComponentMapper<OrderQueue> orderQueueMapper;

	private OrderHandler[] orderHandlers;

	public OrderSystem(OrderHandler[] orderHandlers) {
		super(Aspect.one(OrderQueue.class, Order.class));
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
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int orderQueueEntity) {
		if (orderMapper.has(orderQueueEntity)) {
			Order order = orderMapper.get(orderQueueEntity);
			if (order.handler.isOrderComplete(orderQueueEntity)) {
				logger.debug("Order completed for entity {} with handler {}", orderQueueEntity, order.handler.getClass().getSimpleName());
				orderMapper.remove(orderQueueEntity);
			}
		}

		if (orderQueueMapper.has(orderQueueEntity)) {
			OrderQueue orderQueue = orderQueueMapper.get(orderQueueEntity);
			if (orderQueue.requests.size() > 0) {
				OrderRequest request = orderQueue.requests.get(orderQueue.requests.size() - 1);
				if (orderMapper.has(orderQueueEntity)) {
					Order order = orderMapper.get(orderQueueEntity);
					if (order.handler.handlesRequest(request) && order.handler.modifyOrder(orderQueueEntity, request)) {
						orderQueueMapper.remove(orderQueueEntity);
						logger.debug("Modifying order for entity {} with handler {}", orderQueueEntity, order.handler.getClass().getSimpleName());
					} else {
						cancelOrder(orderQueueEntity);
					}
				} else {
					orderQueueMapper.remove(orderQueueEntity);
					OrderHandler handler = getOrderHandler(request);
					if (handler != null) {
						startOrder(orderQueueEntity, handler, request);
					} else {
						logger.warn("Could not find order handler for request {}", request.toString());
					}
				}
			}
		}
	}

	private void cancelOrder(int orderQueueEntity) {
		Order order = orderMapper.get(orderQueueEntity);
		if (order != null) {
			if (order.handler != null) {
				logger.debug("Canceling order for entity {} with handler {}", orderQueueEntity, order.handler.getClass().getSimpleName());
				order.handler.cancelOrder(orderQueueEntity);
			} else {
				logger.debug("Canceling order for entity {}", orderQueueEntity);
			}
			orderMapper.remove(orderQueueEntity);
		}
	}

	private void startOrder(int orderQueueEntity, OrderHandler handler, OrderRequest request) {
		if (handler.startOrder(orderQueueEntity, request)) {
			orderMapper.create(orderQueueEntity).handler = handler;
			logger.debug("Starting order for entity {} with handler {}", orderQueueEntity, handler.getClass().getSimpleName());
		} else {
			logger.debug("Could not start order for entity {} with handler {}", orderQueueEntity, handler.getClass().getSimpleName());
		}
	}

	private OrderHandler getOrderHandler(OrderRequest request) {
		for (OrderHandler orderHandler : orderHandlers) {
			if (orderHandler.handlesRequest(request)) {
				return orderHandler;
			}
		}
		return null;
	}
}
