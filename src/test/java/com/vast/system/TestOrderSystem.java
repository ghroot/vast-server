package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.vast.component.*;
import com.vast.network.IncomingRequest;
import com.vast.network.VastPeer;
import com.vast.order.OrderHandler;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

public class TestOrderSystem {
	private World world;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Order> orderMapper;

	private void setupWorld(OrderHandler[] orderHandlers, IncomingRequest incomingRequest) {
		Map<String, List<IncomingRequest>> incomingRequests = new HashMap<>();
		if (incomingRequest != null) {
			incomingRequests.put(incomingRequest.getPeer().getName(), new ArrayList<>(Arrays.asList(incomingRequest)));
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
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getName()).thenReturn(name);
		return peer;
	}

	@Test
	public void cancelsOrderIfNotActive() {
		OrderHandler orderHandler = Mockito.mock(OrderHandler.class);
		setupWorld(new OrderHandler[] {orderHandler});

		int playerEntityId = world.create();
		playerMapper.create(playerEntityId);
		orderMapper.create(playerEntityId).handler = orderHandler;

		world.process();

		Assert.assertFalse(orderMapper.has(playerEntityId));
		Mockito.verify(orderHandler).cancelOrder(playerEntityId);
	}

	@Test
	public void removesOrderIfComplete() {
		OrderHandler orderHandler = Mockito.mock(OrderHandler.class);
		setupWorld(new OrderHandler[] {orderHandler});

		int playerEntityId = world.create();
		playerMapper.create(playerEntityId);
		activeMapper.create(playerEntityId);
		orderMapper.create(playerEntityId).handler = orderHandler;
		Mockito.when(orderHandler.isOrderComplete(playerEntityId)).thenReturn(true);

		world.process();

		Assert.assertFalse(orderMapper.has(playerEntityId));
	}

	@Test
	public void startsOrderOnIncomingRequest() {
		VastPeer peer = createPeer("TestPeer");

		OrderHandler orderHandler = Mockito.mock(OrderHandler.class);
		Mockito.when(orderHandler.getMessageCode()).thenReturn((short) 1);
		Mockito.when(orderHandler.startOrder(Mockito.anyInt(), Mockito.any(DataObject.class))).thenReturn(true);

		DataObject dataObject = new DataObject();
		RequestMessage message = new RequestMessage((short) 1, dataObject);
		IncomingRequest incomingRequest = new IncomingRequest(peer, message);

		setupWorld(new OrderHandler[] {orderHandler}, incomingRequest);

		int playerEntityId = world.create();
		playerMapper.create(playerEntityId).name = "TestPeer";
		activeMapper.create(playerEntityId).peer = peer;
		Mockito.when(orderHandler.isOrderComplete(playerEntityId)).thenReturn(true);

		world.process();

		Assert.assertTrue(orderMapper.has(playerEntityId));
		Assert.assertEquals(orderHandler, orderMapper.get(playerEntityId).handler);
		Mockito.verify(orderHandler).startOrder(playerEntityId, dataObject);
	}
}
