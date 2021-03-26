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
	protected void removed(int orderEntity) {
		cancelOrder(orderEntity);
	}

	@Override
	protected void process(int orderEntity) {
		if (orderMapper.has(orderEntity)) {
			Order order = orderMapper.get(orderEntity);
			if (order.handler.isOrderComplete(orderEntity)) {
				logger.debug("Order completed for entity {} with handler {}", orderEntity, order.handler.getClass().getSimpleName());
				order.handler = null;
				orderMapper.remove(orderEntity);
				return;
			}
		}

		if (orderQueueMapper.has(orderEntity)) {
			OrderQueue orderQueue = orderQueueMapper.get(orderEntity);
			if (orderQueue.requests.size() > 0) {
				OrderRequest request = orderQueue.requests.peek();
				if (orderMapper.has(orderEntity)) {
					Order order = orderMapper.get(orderEntity);
					if (order.handler.handlesRequest(request) && order.handler.modifyOrder(orderEntity, request)) {
						orderQueue.requests.remove();
						logger.debug("Modifying order for entity {} with handler {}", orderEntity, order.handler.getClass().getSimpleName());
					}
				} else {
					orderQueue.requests.remove();
					OrderHandler handler = getOrderHandler(request);
					if (handler != null) {
						startOrder(orderEntity, handler, request);
					} else {
						logger.warn("Could not find order handler for request {}", request.toString());
					}
				}
			} else {
				orderQueueMapper.remove(orderEntity);
			}
		}
	}

	private void cancelOrder(int orderEntity) {
		Order order = orderMapper.get(orderEntity);
		if (order != null && order.handler != null) {
			logger.debug("Canceling order for entity {} with handler {}", orderEntity, order.handler.getClass().getSimpleName());
			order.handler.cancelOrder(orderEntity);
			order.handler = null;

			orderMapper.remove(orderEntity);
		}
	}

	private void startOrder(int orderEntity, OrderHandler handler, OrderRequest request) {
		if (handler.startOrder(orderEntity, request)) {
			orderMapper.create(orderEntity).handler = handler;
			logger.debug("Starting order for entity {} with handler {}", orderEntity, handler.getClass().getSimpleName());
		} else {
			logger.debug("Could not start order for entity {} with handler {}", orderEntity, handler.getClass().getSimpleName());
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
