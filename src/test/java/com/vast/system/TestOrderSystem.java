package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Order;
import com.vast.component.OrderQueue;
import com.vast.network.IncomingRequest;
import com.vast.network.VastPeer;
import com.vast.order.handler.OrderHandler;
import com.vast.order.request.OrderRequest;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestOrderSystem {
	private World world;
	private ComponentMapper<OrderQueue> orderQueueMapper;
	private ComponentMapper<Order> orderMapper;

	private void setupWorld(OrderHandler[] orderHandlers, IncomingRequest incomingRequest) {
		world = new World(new WorldConfigurationBuilder().with(
			new OrderSystem(orderHandlers)
		).build());

		orderQueueMapper = world.getMapper(OrderQueue.class);
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
	public void removesOrderQueueIfEmpty() {
		OrderHandler orderHandler = mock(OrderHandler.class);
		setupWorld(new OrderHandler[] {orderHandler});

		int orderEntity = world.create();
		orderQueueMapper.create(orderEntity);

		world.process();

		assertFalse(orderQueueMapper.has(orderEntity));
	}

	@Test
	public void removesOrderIfComplete() {
		OrderHandler orderHandler = mock(OrderHandler.class);
		setupWorld(new OrderHandler[] {orderHandler});

		int orderEntity = world.create();
		orderMapper.create(orderEntity).handler = orderHandler;
		when(orderHandler.isOrderComplete(orderEntity)).thenReturn(true);

		world.process();

		assertFalse(orderMapper.has(orderEntity));
	}

	@Test
	public void startsOrderWhenQueueHasRequest() {
		VastPeer peer = createPeer("TestPeer");

		OrderHandler orderHandler = mock(OrderHandler.class);
		when(orderHandler.handlesRequest(any())).thenReturn(true);
		when(orderHandler.startOrder(anyInt(), any())).thenReturn(true);
		when(orderHandler.isOrderComplete(anyInt())).thenReturn(false);

		setupWorld(new OrderHandler[] {orderHandler});

		OrderRequest orderRequest = mock(OrderRequest.class);

		int orderEntity = world.create();
		orderQueueMapper.create(orderEntity).requests.add(orderRequest);

		world.process();

		assertTrue(orderMapper.has(orderEntity));
		assertEquals(orderHandler, orderMapper.get(orderEntity).handler);
		verify(orderHandler).startOrder(orderEntity, orderRequest);
	}

	@Test
	public void keepsOrderWhenQueueHasRequest() {
		VastPeer peer = createPeer("TestPeer");

		OrderHandler orderHandler = mock(OrderHandler.class);
		when(orderHandler.handlesRequest(any())).thenReturn(true);
		when(orderHandler.startOrder(anyInt(), any())).thenReturn(true);
		when(orderHandler.isOrderComplete(anyInt())).thenReturn(false);

		setupWorld(new OrderHandler[] {orderHandler});

		OrderRequest orderRequest = mock(OrderRequest.class);

		int orderEntity = world.create();
		orderQueueMapper.create(orderEntity).requests.add(orderRequest);
		orderMapper.create(orderEntity).handler = orderHandler;

		world.process();

		assertEquals(1, orderQueueMapper.get(orderEntity).requests.size());
	}

	@Test
	public void modifiesOrder() {
		VastPeer peer = createPeer("TestPeer");

		OrderHandler orderHandler = mock(OrderHandler.class);
		when(orderHandler.handlesRequest(any())).thenReturn(true);
		when(orderHandler.startOrder(anyInt(), any())).thenReturn(true);
		when(orderHandler.isOrderComplete(anyInt())).thenReturn(false);
		when(orderHandler.modifyOrder(anyInt(), any())).thenReturn(true);

		setupWorld(new OrderHandler[] {orderHandler});

		OrderRequest orderRequest = mock(OrderRequest.class);

		int orderEntity = world.create();
		orderQueueMapper.create(orderEntity).requests.add(orderRequest);
		orderMapper.create(orderEntity).handler = orderHandler;

		world.process();

		assertEquals(0, orderQueueMapper.get(orderEntity).requests.size());
		verify(orderHandler).modifyOrder(orderEntity, orderRequest);
	}
}
