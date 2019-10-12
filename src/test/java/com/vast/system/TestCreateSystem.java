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
			new CreateSystem(new HashMap<String, VastPeer>(), new HashSet<PropertyHandler>())
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
		Map<String, VastPeer> peers = new HashMap<>();
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getId()).thenReturn(123L);
		peers.put("TestName", peer);
		World world = new World(new WorldConfigurationBuilder().with(
			new CreateSystem(peers, new HashSet<PropertyHandler>())
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		ComponentMapper<Create> createMapper = world.getMapper(Create.class);
		ComponentMapper<Know> knowMapper = world.getMapper(Know.class);
		ComponentMapper<Known> knownMapper = world.getMapper(Known.class);
		ComponentMapper<Scan> scanMapper = world.getMapper(Scan.class);
		ComponentMapper<Type> typeMapper = world.getMapper(Type.class);

		int playerEntity = world.create();
		int nonPlayerEntity = world.create();
		int entityToCreate = world.create();

		playerMapper.create(playerEntity).name = "TestName";
		knowMapper.create(playerEntity);
		scanMapper.create(playerEntity).nearbyEntities.add(entityToCreate);

		knowMapper.create(nonPlayerEntity);
		scanMapper.create(nonPlayerEntity).nearbyEntities.add(entityToCreate);

		createMapper.create(entityToCreate).reason = "testing";
		typeMapper.create(entityToCreate).type = "testType";
		knownMapper.create(entityToCreate);

		world.process();

		Assert.assertTrue(knowMapper.get(playerEntity).knowEntities.contains(entityToCreate));
		Assert.assertTrue(knownMapper.get(entityToCreate).knownByEntities.contains(playerEntity));
		Mockito.verify(peer).send(Mockito.any());

		Assert.assertTrue(knowMapper.get(nonPlayerEntity).knowEntities.contains(entityToCreate));
		Assert.assertTrue(knownMapper.get(entityToCreate).knownByEntities.contains(nonPlayerEntity));
	}
}
