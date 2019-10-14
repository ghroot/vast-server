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

import java.util.HashSet;

public class TestCullingSystem {
	@Test
	public void newEntityIsKnownFromBothSides() {
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getId()).thenReturn(123L);

		World world = new World(new WorldConfigurationBuilder().with(
			new CullingSystem(new HashSet<PropertyHandler>())
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		ComponentMapper<Active> activeMapper = world.getMapper(Active.class);
		ComponentMapper<Know> knowMapper = world.getMapper(Know.class);
		ComponentMapper<Known> knownMapper = world.getMapper(Known.class);
		ComponentMapper<Scan> scanMapper = world.getMapper(Scan.class);
		ComponentMapper<Type> typeMapper = world.getMapper(Type.class);

		int playerEntity = world.create();
		int nonPlayerEntity = world.create();
		int entityToCreate = world.create();

		playerMapper.create(playerEntity).name = "TestName";
		activeMapper.create(playerEntity).peer = peer;
		knowMapper.create(playerEntity);
		scanMapper.create(playerEntity).nearbyEntities.add(entityToCreate);

		knowMapper.create(nonPlayerEntity);
		scanMapper.create(nonPlayerEntity).nearbyEntities.add(entityToCreate);

		typeMapper.create(entityToCreate).type = "testType";
		knownMapper.create(entityToCreate);

		world.process();

		Assert.assertTrue(knowMapper.get(playerEntity).knowEntities.contains(entityToCreate));
		Assert.assertTrue(knownMapper.get(entityToCreate).knownByEntities.contains(playerEntity));
		Mockito.verify(peer).send(Mockito.any());

		Assert.assertTrue(knowMapper.get(nonPlayerEntity).knowEntities.contains(entityToCreate));
		Assert.assertTrue(knownMapper.get(entityToCreate).knownByEntities.contains(nonPlayerEntity));
	}

	@Test
	public void outOfRangeEntityIsRemovedBothSides() {
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getId()).thenReturn(123L);

		World world = new World(new WorldConfigurationBuilder().with(
			new CullingSystem(new HashSet<PropertyHandler>())
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		ComponentMapper<Active> activeMapper = world.getMapper(Active.class);
		ComponentMapper<Know> knowMapper = world.getMapper(Know.class);
		ComponentMapper<Known> knownMapper = world.getMapper(Known.class);
		ComponentMapper<Scan> scanMapper = world.getMapper(Scan.class);
		ComponentMapper<Type> typeMapper = world.getMapper(Type.class);

		int playerEntity = world.create();
		int nonPlayerEntity = world.create();
		int outOfRangeEntity = world.create();

		playerMapper.create(playerEntity).name = "TestName";
		activeMapper.create(playerEntity).peer = peer;
		knowMapper.create(playerEntity).knowEntities.add(outOfRangeEntity);
		scanMapper.create(playerEntity);

		knowMapper.create(nonPlayerEntity).knowEntities.add(outOfRangeEntity);
		scanMapper.create(nonPlayerEntity);

		typeMapper.create(outOfRangeEntity).type = "testType";
		knownMapper.create(outOfRangeEntity);
		knownMapper.get(outOfRangeEntity).knownByEntities.add(playerEntity);
		knownMapper.get(outOfRangeEntity).knownByEntities.add(nonPlayerEntity);

		world.process();

		Assert.assertFalse(knowMapper.get(playerEntity).knowEntities.contains(outOfRangeEntity));
		Assert.assertFalse(knownMapper.get(outOfRangeEntity).knownByEntities.contains(playerEntity));
		Mockito.verify(peer).send(Mockito.any());

		Assert.assertFalse(knowMapper.get(nonPlayerEntity).knowEntities.contains(outOfRangeEntity));
		Assert.assertFalse(knownMapper.get(outOfRangeEntity).knownByEntities.contains(nonPlayerEntity));
	}
}
