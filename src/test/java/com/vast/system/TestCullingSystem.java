package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.*;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class TestCullingSystem {
	private World world;
	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;

	private void setupWorld(PropertyHandler[] propertyHandlers) {
		world = new World(new WorldConfigurationBuilder().with(
			new CullingSystem(propertyHandlers)
		).build());

		observerMapper = world.getMapper(Observer.class);
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

		int observerEntity = world.create();
		int newEntity = world.create();

		observerMapper.create(observerEntity).peer = peer;
		scanMapper.create(observerEntity).nearbyEntities.add(newEntity);

		typeMapper.create(newEntity).type = "testType";
		knownMapper.create(newEntity);

		world.process();

		assertTrue(observerMapper.get(observerEntity).knowEntities.contains(newEntity));
		assertTrue(knownMapper.get(newEntity).knownByEntities.contains(observerEntity));
		verify(peer).send(any());
	}

	@Test
	public void outOfRangeEntityIsRemovedBothSides() {
		VastPeer peer = mock(VastPeer.class);
		when(peer.getId()).thenReturn(123L);
		setupWorld();

		int observerEntity = world.create();
		int outOfRangeEntity = world.create();

		observerMapper.create(observerEntity).peer = peer;
		observerMapper.get(observerEntity).knowEntities.add(outOfRangeEntity);
		scanMapper.create(observerEntity);

		typeMapper.create(outOfRangeEntity).type = "testType";
		knownMapper.create(outOfRangeEntity);
		knownMapper.get(outOfRangeEntity).knownByEntities.add(observerEntity);

		world.process();

		assertFalse(observerMapper.get(observerEntity).knowEntities.contains(outOfRangeEntity));
		assertFalse(knownMapper.get(outOfRangeEntity).knownByEntities.contains(observerEntity));
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

		int observerEntity = world.create();
		int newEntity = world.create();

		observerMapper.create(observerEntity).peer = peer;
		scanMapper.create(observerEntity).nearbyEntities.add(newEntity);

		typeMapper.create(newEntity).type = "testType";
		knownMapper.create(newEntity);
		syncPropagationMapper.create(newEntity);

		world.process();

		verify(firstPropertyHandler, times(1)).decorateDataObject(anyInt(), any(), anyBoolean());
		verify(secondPropertyHandler, never()).decorateDataObject(anyInt(), any(), anyBoolean());
	}
}
