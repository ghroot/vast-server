package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.*;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestCreateSystem {
	private World world;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;

	private void setupWorld(PropertyHandler[] propertyHandlers) {
		world = new World(new WorldConfigurationBuilder().with(
			new CreateSystem(propertyHandlers)
		).build());

		playerMapper = world.getMapper(Player.class);
		activeMapper = world.getMapper(Active.class);
		createMapper = world.getMapper(Create.class);
		knownMapper = world.getMapper(Known.class);
		scanMapper = world.getMapper(Scan.class);
		typeMapper = world.getMapper(Type.class);
		syncPropagationMapper = world.getMapper(SyncPropagation.class);
	}

	private void setupWorld() {
		setupWorld(new PropertyHandler[0]);
	}

	@Test
	public void removesCreateComponent() {
		setupWorld();

		int entityToCreate = world.create();
		typeMapper.create(entityToCreate).type = "testType";
		createMapper.create(entityToCreate).reason = "testing";

		world.process();

		assertFalse(createMapper.has(entityToCreate));
	}

	@Test
	public void createdEntityIsKnownFromBothSides() {
		VastPeer peer = mock(VastPeer.class);
		when(peer.getId()).thenReturn(123L);
		setupWorld();

		int playerEntity = world.create();
		int entityToCreate = world.create();

		playerMapper.create(playerEntity).name = "TestName";
		activeMapper.create(playerEntity).peer = peer;
		scanMapper.create(playerEntity).nearbyEntities.add(entityToCreate);

		createMapper.create(entityToCreate).reason = "testing";
		typeMapper.create(entityToCreate).type = "testType";
		knownMapper.create(entityToCreate);

		world.process();

		assertTrue(activeMapper.get(playerEntity).knowEntities.contains(entityToCreate));
		assertTrue(knownMapper.get(entityToCreate).knownByEntities.contains(playerEntity));
		verify(peer).send(any());
	}

	@Test
	public void onlyDecoratesFirstOfEachProperty() {
		VastPeer peer = mock(VastPeer.class);
		when(peer.getId()).thenReturn(123L);
		byte property = (byte) 1;
		PropertyHandler firstPropertyHandler = mock(PropertyHandler.class);
		when(firstPropertyHandler.getProperty()).thenReturn(property);
		when(firstPropertyHandler.isInterestedIn(anyInt())).thenReturn(true);
		when(firstPropertyHandler.decorateDataObject(anyInt(), any(), anyBoolean())).thenReturn(true);
		PropertyHandler secondPropertyHandler = mock(PropertyHandler.class);
		when(secondPropertyHandler.getProperty()).thenReturn(property);
		when(secondPropertyHandler.isInterestedIn(anyInt())).thenReturn(true);
		when(secondPropertyHandler.decorateDataObject(anyInt(), any(), anyBoolean())).thenReturn(true);
		setupWorld(new PropertyHandler[]{firstPropertyHandler, secondPropertyHandler});

		int playerEntity = world.create();
		int entityToCreate = world.create();

		playerMapper.create(playerEntity).name = "TestName";
		activeMapper.create(playerEntity).peer = peer;
		scanMapper.create(playerEntity).nearbyEntities.add(entityToCreate);

		createMapper.create(entityToCreate).reason = "testing";
		typeMapper.create(entityToCreate).type = "testType";
		knownMapper.create(entityToCreate);
		syncPropagationMapper.create(entityToCreate);

		world.process();

		verify(firstPropertyHandler, times(1)).decorateDataObject(anyInt(), any(), anyBoolean());
		verify(secondPropertyHandler, never()).decorateDataObject(anyInt(), any(), anyBoolean());
	}
}
