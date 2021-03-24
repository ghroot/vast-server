package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Delete;
import com.vast.component.Known;
import com.vast.component.Observer;
import com.vast.network.VastPeer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestDeleteSystem {
	@Test
	public void removesDeleteComponent() {
		World world = new World(new WorldConfigurationBuilder().with(
			new DeleteSystem()
		).build());

		ComponentMapper<Delete> deleteMapper = world.getMapper(Delete.class);

		int entityToDelete = world.create();
		deleteMapper.create(entityToDelete).reason = "testing";

		world.process();

		assertFalse(deleteMapper.has(entityToDelete));
	}

	@Test
	public void removesKnownEntity() {
		World world = new World(new WorldConfigurationBuilder().with(
			new DeleteSystem()
		).build());

		ComponentMapper<Observer> observerMapper = world.getMapper(Observer.class);
		ComponentMapper<Delete> deleteMapper = world.getMapper(Delete.class);
		ComponentMapper<Known> knownMapper = world.getMapper(Known.class);

		int entityToDelete = world.create();

		int observerEntity = world.create();
		observerMapper.create(observerEntity);
		VastPeer peer = mock(VastPeer.class);
		when(peer.getId()).thenReturn(123L);
		observerMapper.get(observerEntity).peer = peer;
		observerMapper.get(observerEntity).knowEntities.add(entityToDelete);

		deleteMapper.create(entityToDelete).reason = "testing";
		knownMapper.create(entityToDelete).knownByEntities.add(observerEntity);

		world.process();

		assertFalse(observerMapper.get(observerEntity).knowEntities.contains(entityToDelete));
		verify(peer).send(any());
	}
}
