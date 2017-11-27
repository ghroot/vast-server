package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.IncomingRequest;
import com.vast.component.Active;
import com.vast.component.Order;
import com.vast.component.Player;
import com.vast.order.OrderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(OrderSystem.class);

	private ComponentMapper<Order> orderMapper;
	private ComponentMapper<Player> playerMapper;

	private Set<OrderHandler> orderHandlers;
	private Map<String, List<IncomingRequest>> incomingRequestsByPeer;

	public OrderSystem(Set<OrderHandler> orderHandlers, Map<String, List<IncomingRequest>> incomingRequestsByPeer) {
		super(Aspect.all(Player.class, Active.class));
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
	protected void removed(int orderEntity) {
		cancelOrder(orderEntity);
	}

	@Override
	protected void process(int playerEntity) {
		Player player = playerMapper.get(playerEntity);

		if (orderMapper.has(playerEntity)) {
			Order order = orderMapper.get(playerEntity);
			if (order.handler.isOrderComplete(playerEntity)) {
				logger.debug("{} order completed for entity {}", order.type, playerEntity);
				orderMapper.remove(playerEntity);
			}
		}

		if (incomingRequestsByPeer.containsKey(player.name)) {
			List<IncomingRequest> incomingRequests = incomingRequestsByPeer.get(player.name);
			if (incomingRequests.size() > 0) {
				if (orderMapper.has(playerEntity)) {
					cancelOrder(playerEntity);
				} else {
					IncomingRequest lastIncomingRequest = incomingRequests.get(incomingRequests.size() - 1);
					incomingRequests.clear();
					OrderHandler handler = getOrderHandler(lastIncomingRequest.getMessage().getCode());
					if (handler != null) {
						startOrder(playerEntity, handler, lastIncomingRequest.getMessage().getDataObject());
					} else {
						logger.warn("Could not find order handler for message code {}", lastIncomingRequest.getMessage().getCode());
					}
				}
			}
		}
	}

	private void cancelOrder(int playerEntity) {
		if (orderMapper.has(playerEntity)) {
			Order order = orderMapper.get(playerEntity);
			if (order.handler != null) {
				logger.debug("Canceling {} order for entity {}", order.type, playerEntity);
				order.handler.cancelOrder(playerEntity);
				orderMapper.remove(playerEntity);
			}
		}
	}

	private void startOrder(int playerEntity, OrderHandler handler, DataObject dataObject) {
		if (handler.startOrder(playerEntity, dataObject)) {
			orderMapper.create(playerEntity).type = handler.getOrderType();
			orderMapper.get(playerEntity).handler = handler;
			logger.debug("Starting {} order for entity {}", handler.getOrderType(), playerEntity);
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
