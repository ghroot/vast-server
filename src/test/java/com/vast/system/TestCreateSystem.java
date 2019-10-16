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
import java.util.HashSet;
import java.util.Map;

public class TestCreateSystem {
	@Test
	public void removesCreateComponent() {
		World world = new World(new WorldConfigurationBuilder().with(
			new CreateSystem(new HashSet<PropertyHandler>())
		).build());

		ComponentMapper<Create> createMapper = world.getMapper(Create.class);
		ComponentMapper<Type> typeMapper = world.getMapper(Type.class);

		int entityToCreate = world.create();
		typeMapper.create(entityToCreate).type = "testType";
		createMapper.create(entityToCreate).reason = "testing";

		world.process();

		Assert.assertFalse(createMapper.has(entityToCreate));
	}

	@Test
	public void createdEntityIsKnownFromBothSides() {
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getId()).thenReturn(123L);
		World world = new World(new WorldConfigurationBuilder().with(
			new CreateSystem(new HashSet<PropertyHandler>())
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		ComponentMapper<Active> activeMapper = world.getMapper(Active.class);
		ComponentMapper<Create> createMapper = world.getMapper(Create.class);
		ComponentMapper<Known> knownMapper = world.getMapper(Known.class);
		ComponentMapper<Scan> scanMapper = world.getMapper(Scan.class);
		ComponentMapper<Type> typeMapper = world.getMapper(Type.class);

		int playerEntity = world.create();
		int entityToCreate = world.create();

		playerMapper.create(playerEntity).name = "TestName";
		activeMapper.create(playerEntity).peer = peer;
		scanMapper.create(playerEntity).nearbyEntities.add(entityToCreate);

		createMapper.create(entityToCreate).reason = "testing";
		typeMapper.create(entityToCreate).type = "testType";
		knownMapper.create(entityToCreate);

		world.process();

		Assert.assertTrue(activeMapper.get(playerEntity).knowEntities.contains(entityToCreate));
		Assert.assertTrue(knownMapper.get(entityToCreate).knownByEntities.contains(playerEntity));
		Mockito.verify(peer).send(Mockito.any());
	}
}
