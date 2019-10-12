package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.*;
import com.vast.network.VastPeer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TestDeleteSystem {
	@Test
	public void removesDeleteComponent() {
		World world = new World(new WorldConfigurationBuilder().with(
			new DeleteSystem(new HashMap<String, VastPeer>())
		).build());

		ComponentMapper<Delete> deleteMapper = world.getMapper(Delete.class);

		int entityToDelete = world.create();
		deleteMapper.create(entityToDelete).reason = "testing";

		world.process();

		Assert.assertFalse(deleteMapper.has(entityToDelete));
	}

	@Test
	public void removesKnownEntityOnBothSides() {
		Map<String, VastPeer> peers = new HashMap<>();
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getId()).thenReturn(123L);
		peers.put("TestName", peer);
		World world = new World(new WorldConfigurationBuilder().with(
			new DeleteSystem(peers)
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		ComponentMapper<Delete> deleteMapper = world.getMapper(Delete.class);
		ComponentMapper<Know> knowMapper = world.getMapper(Know.class);
		ComponentMapper<Known> knownMapper = world.getMapper(Known.class);

		int entityToDelete = world.create();
		int playerEntity = world.create();

		deleteMapper.create(entityToDelete).reason = "testing";
		knownMapper.create(entityToDelete).knownByEntities.add(playerEntity);

		playerMapper.create(playerEntity).name = "TestName";
		knowMapper.create(playerEntity).knowEntities.add(entityToDelete);

		world.process();

		Assert.assertFalse(knowMapper.get(playerEntity).knowEntities.contains(entityToDelete));
		Mockito.verify(peer).send(Mockito.any());
	}
}
