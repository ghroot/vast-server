package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.vast.component.Active;
import com.vast.component.Order;
import com.vast.component.Player;
import com.vast.network.IncomingRequest;
import com.vast.network.VastPeer;
import com.vast.order.OrderHandler;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestOrderSystem {
	private List<IncomingRequest> incomingRequestsForPlayer;
	private World world;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Order> orderMapper;

	private void setupWorld(OrderHandler[] orderHandlers, IncomingRequest incomingRequest) {
		Map<String, List<IncomingRequest>> incomingRequests = new HashMap<>();
		incomingRequestsForPlayer = new ArrayList<>();
		if (incomingRequest != null) {
			incomingRequests.put(incomingRequest.getPeer().getName(), incomingRequestsForPlayer);
			incomingRequestsForPlayer.add(incomingRequest);
		}
		world = new World(new WorldConfigurationBuilder().with(
			new OrderSystem(orderHandlers, incomingRequests)
		).build());

		playerMapper = world.getMapper(Player.class);
		activeMapper = world.getMapper(Active.class);
		orderMapper = world.getMapper(Order.class);
	}

	private void setupWorld(OrderHandler[] orderHandlers) {
		setupWorld(orderHandlers, null);
	}

	private VastPeer createPeer(String name) {
		VastPeer peer = mock(VastPeer.class);
		when(peer.getName()).thenReturn(name);
		return peer;
	}

	@Test
	public void cancelsOrderIfNotActive() {
		OrderHandler orderHandler = mock(OrderHandler.class);
		setupWorld(new OrderHandler[] {orderHandler});

		int playerEntityId = world.create();
		playerMapper.create(playerEntityId);
		orderMapper.create(playerEntityId).handler = orderHandler;

		world.process();

		assertFalse(orderMapper.has(playerEntityId));
		verify(orderHandler).cancelOrder(playerEntityId);
	}

	@Test
	public void removesOrderIfComplete() {
		OrderHandler orderHandler = mock(OrderHandler.class);
		setupWorld(new OrderHandler[] {orderHandler});

		int playerEntityId = world.create();
		playerMapper.create(playerEntityId);
		activeMapper.create(playerEntityId);
		orderMapper.create(playerEntityId).handler = orderHandler;
		when(orderHandler.isOrderComplete(playerEntityId)).thenReturn(true);

		world.process();

		assertFalse(orderMapper.has(playerEntityId));
	}

	@Test
	public void startsOrderOnIncomingRequest() {
		VastPeer peer = createPeer("TestPeer");

		OrderHandler orderHandler = mock(OrderHandler.class);
		when(orderHandler.handlesMessageCode(anyShort())).thenReturn(true);
		when(orderHandler.startOrder(anyInt(), anyShort(), any(DataObject.class))).thenReturn(true);

		DataObject dataObject = new DataObject();
		RequestMessage message = new RequestMessage((short) 1, dataObject);
		IncomingRequest incomingRequest = new IncomingRequest(peer, message);

		setupWorld(new OrderHandler[] {orderHandler}, incomingRequest);

		int playerEntityId = world.create();
		playerMapper.create(playerEntityId).name = "TestPeer";
		activeMapper.create(playerEntityId).peer = peer;
		when(orderHandler.isOrderComplete(playerEntityId)).thenReturn(true);

		world.process();

		assertTrue(orderMapper.has(playerEntityId));
		assertEquals(orderHandler, orderMapper.get(playerEntityId).handler);
		verify(orderHandler).startOrder(playerEntityId, (short) 1, dataObject);
	}

	@Test
	public void modifiesOrder() {
		VastPeer peer = createPeer("TestPeer");

		OrderHandler orderHandler = mock(OrderHandler.class);
		when(orderHandler.handlesMessageCode(anyShort())).thenReturn(true);
		when(orderHandler.startOrder(anyInt(), anyShort(), any(DataObject.class))).thenReturn(true);
		when(orderHandler.isOrderComplete(anyInt())).thenReturn(false);
		when(orderHandler.modifyOrder(anyInt(), anyShort(), any(DataObject.class))).thenReturn(true);

		DataObject dataObject = new DataObject();
		RequestMessage message = new RequestMessage((short) 1, dataObject);
		IncomingRequest incomingRequest = new IncomingRequest(peer, message);

		setupWorld(new OrderHandler[] {orderHandler}, incomingRequest);

		int playerEntityId = world.create();
		playerMapper.create(playerEntityId).name = "TestPeer";
		activeMapper.create(playerEntityId).peer = peer;

		world.process();

		Order order = orderMapper.get(playerEntityId);

		DataObject dataObject2 = new DataObject();
		RequestMessage message2 = new RequestMessage((short) 1, dataObject2);
		IncomingRequest incomingRequest2 = new IncomingRequest(peer, message2);
		incomingRequestsForPlayer.add(incomingRequest2);

		world.process();

		assertTrue(orderMapper.has(playerEntityId));
		assertEquals(orderHandler, orderMapper.get(playerEntityId).handler);
		assertEquals(order, orderMapper.get(playerEntityId));
		verify(orderHandler).modifyOrder(playerEntityId, (short) 1, dataObject2);
	}
}
