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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestCullingSystem {
	private World world;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;

	private void setupWorld(PropertyHandler[] propertyHandlers) {
		world = new World(new WorldConfigurationBuilder().with(
			new CullingSystem(propertyHandlers)
		).build());

		playerMapper = world.getMapper(Player.class);
		activeMapper = world.getMapper(Active.class);
		knownMapper = world.getMapper(Known.class);
		scanMapper = world.getMapper(Scan.class);
		typeMapper = world.getMapper(Type.class);
		syncPropagationMapper = world.getMapper(SyncPropagation.class);
	}

	private void setupWorld() {
		setupWorld(new PropertyHandler[0]);
	}

	@Test
	public void newEntityIsKnownFromBothSides() {
		VastPeer peer = mock(VastPeer.class);
		when(peer.getId()).thenReturn(123L);
		setupWorld();

		int playerEntity = world.create();
		int newEntity = world.create();

		playerMapper.create(playerEntity).name = "TestName";
		activeMapper.create(playerEntity).peer = peer;
		scanMapper.create(playerEntity).nearbyEntities.add(newEntity);

		typeMapper.create(newEntity).type = "testType";
		knownMapper.create(newEntity);

		world.process();

		assertTrue(activeMapper.get(playerEntity).knowEntities.contains(newEntity));
		assertTrue(knownMapper.get(newEntity).knownByEntities.contains(playerEntity));
		verify(peer).send(any());
	}

	@Test
	public void outOfRangeEntityIsRemovedBothSides() {
		VastPeer peer = mock(VastPeer.class);
		when(peer.getId()).thenReturn(123L);
		setupWorld();

		int playerEntity = world.create();
		int outOfRangeEntity = world.create();

		playerMapper.create(playerEntity).name = "TestName";
		activeMapper.create(playerEntity).peer = peer;
		activeMapper.get(playerEntity).knowEntities.add(outOfRangeEntity);
		scanMapper.create(playerEntity);

		typeMapper.create(outOfRangeEntity).type = "testType";
		knownMapper.create(outOfRangeEntity);
		knownMapper.get(outOfRangeEntity).knownByEntities.add(playerEntity);

		world.process();

		assertFalse(activeMapper.get(playerEntity).knowEntities.contains(outOfRangeEntity));
		assertFalse(knownMapper.get(outOfRangeEntity).knownByEntities.contains(playerEntity));
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
		int newEntity = world.create();

		playerMapper.create(playerEntity).name = "TestName";
		activeMapper.create(playerEntity).peer = peer;
		scanMapper.create(playerEntity).nearbyEntities.add(newEntity);

		typeMapper.create(newEntity).type = "testType";
		knownMapper.create(newEntity);
		syncPropagationMapper.create(newEntity);

		world.process();

		verify(firstPropertyHandler, times(1)).decorateDataObject(anyInt(), any(), anyBoolean());
		verify(secondPropertyHandler, never()).decorateDataObject(anyInt(), any(), anyBoolean());
	}
}
