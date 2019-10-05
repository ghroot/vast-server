package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Active;
import com.vast.component.Order;
import com.vast.component.Player;
import com.vast.component.Sync;
import com.vast.data.Properties;
import com.vast.network.IncomingRequest;
import com.vast.network.VastPeer;
import com.vast.order.OrderHandler;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

public class TestOrderSystem {
	@Test
	public void givenNotActive_cancelsOrder() {
		Set<OrderHandler> orderHandlers = new HashSet<>();
		Map<String, List<IncomingRequest>> incomingRequestsByPeer = new HashMap<>();
		World world = new World(new WorldConfigurationBuilder().with(
			new OrderSystem(orderHandlers, incomingRequestsByPeer)
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		ComponentMapper<Order> orderMapper = world.getMapper(Order.class);
		int entityId = world.create();
		playerMapper.create(entityId).name = "TestName";
		OrderHandler orderHandler = Mockito.mock(OrderHandler.class);
		orderMapper.create(entityId).handler = orderHandler;
		IncomingRequest incomingRequest = Mockito.mock(IncomingRequest.class);
		List<IncomingRequest> incomingRequests = new ArrayList<>();
		incomingRequests.add(incomingRequest);
		incomingRequestsByPeer.put("TestName", incomingRequests);

		world.process();

		Assert.assertFalse(orderMapper.has(entityId));
		Mockito.verify(orderHandler).cancelOrder(entityId);
		Assert.assertEquals(0, incomingRequestsByPeer.get("TestName").size());
	}
}
