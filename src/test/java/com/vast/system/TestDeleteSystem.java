package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.*;
import com.vast.network.VastPeer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

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

		Assert.assertFalse(deleteMapper.has(entityToDelete));
	}

	@Test
	public void removesKnownEntity() {
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getId()).thenReturn(123L);

		World world = new World(new WorldConfigurationBuilder().with(
			new DeleteSystem()
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		ComponentMapper<Active> activeMapper = world.getMapper(Active.class);
		ComponentMapper<Delete> deleteMapper = world.getMapper(Delete.class);
		ComponentMapper<Know> knowMapper = world.getMapper(Know.class);
		ComponentMapper<Known> knownMapper = world.getMapper(Known.class);

		int entityToDelete = world.create();
		int playerEntity = world.create();

		deleteMapper.create(entityToDelete).reason = "testing";
		knownMapper.create(entityToDelete).knownByEntities.add(playerEntity);

		playerMapper.create(playerEntity).name = "TestName";
		activeMapper.create(playerEntity).peer = peer;
		knowMapper.create(playerEntity).knowEntities.add(entityToDelete);

		world.process();

		Assert.assertFalse(knowMapper.get(playerEntity).knowEntities.contains(entityToDelete));
		Mockito.verify(peer).send(Mockito.any());
	}
}
